/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.core;

import com.a2i.dms.Component;
import com.a2i.dms.DeliveryType;
import com.a2i.dms.Inject;
import com.a2i.dms.Interceptor;
import com.a2i.dms.Parameters;
import com.a2i.dms.core.codec.GenericEncoder;
import com.a2i.dms.core.member.AbstractMember;
import com.a2i.dms.core.member.MeMember;
import com.a2i.dms.core.member.MeMemberTCP;
import com.a2i.dms.core.member.MeMemberUDP;
import com.a2i.dms.core.member.Member;
import com.a2i.dms.core.member.MemberHolder;
import com.a2i.dms.core.member.MemberKey;
import com.a2i.dms.core.member.MemberStatus;
import com.a2i.dms.core.member.RemoteMemberTCP;
import com.a2i.dms.core.member.RemoteMemberUDP;
import com.a2i.dms.util.NetworkUtil;
import com.a2i.dms.util.TimeUtil;
import com.a2i.dms.util.TopicUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class ClusterService {

    private static final String PROTOCOL_PROPERTY = "com.a2i.protocol";
    private static final String DEFAULT_PROTOCOL = "tcp";
    private static final String GOSSIP_PORT_PROPERTY = "com.a2i.port.gossip";
    private static final String DATA_PORT_PROPERTY = "com.a2i.port.data";

    @Inject
    private MCDiscovery multicast;

    @Inject
    private MemberHolder memberHolder;

    @Inject
    private ListenerRegistry registry;

    private MeMember me;

    private Gossiper gossiper;

    private static final Logger LOG = LoggerFactory.getLogger(ClusterService.class);

    private final Map<String, DeliveryType> deliveries = new ConcurrentHashMap<>();
    private final Map<String, Integer> roundRobinIndex = new ConcurrentHashMap<>();

    private boolean shuttingDown = false;

    public ClusterService() {
        
    }

    public void setMulticastDiscovery(MCDiscovery multicast) {
        this.multicast = multicast;
    }

    public void setMemberHolder(MemberHolder memberHolder) {
        this.memberHolder = memberHolder;
    }

    public void setRegistry(ListenerRegistry registry) {
        this.registry = registry;
    }

    public ListenerRegistry getRegistry() {
        return registry;
    }

    public void setDeliveryType(String topic, DeliveryType type) {
        deliveries.put(topic, type);
        if(type == DeliveryType.ROUND_ROBIN) {
            roundRobinIndex.put(topic, 0);
        }
    }

    public void addInterceptor(String topic, Interceptor interceptor) {
        registry.addInterceptor(topic, interceptor);
    }

    public <T> void addListener(String topic, String partition, MessageListener<T> consumer) {
        registry.registerMemberForTopic(topic, partition, me);
        registry.addLocalListener(topic, partition, consumer);
    }

    public void removeAllListeners(String topic) {
        registry.removeAllLocalListeners(topic);
    }

    public <T> void sendMessage(String topic, String partition, T message, int offsetMilliseconds) throws IOException {
        Envelope envelope = new Envelope();
        envelope.setDecoded(false);
        envelope.setExecuteTime(offsetMilliseconds);
        envelope.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
        envelope.setSenderKey(MemberKey.getKey(me));
        envelope.setTopic(topic);
        envelope.setPartition(partition);
        envelope.setClassName(message.getClass().getName());

        DeliveryType delivery = deliveries.get(topic);
        if(delivery == null) {
            delivery = DeliveryType.BROADCAST;
            deliveries.put(topic, delivery);
        }

        switch(delivery) {
            case ROUND_ROBIN:

                if(registry.getRegisteredMembers(topic).size() > 0) {

                    int index = (roundRobinIndex.get(topic) + 1) % 
                            registry.getRegisteredMembers(topic).size();
                    roundRobinIndex.put(topic, index);
                
                    Member member = registry.getRegisteredMembers(topic).get(index);

                    if(me.equals(member)) {
                        envelope.setMessage(message);
                        envelope.setDecoded(true);
                    } else {
                        envelope.setPayload(GenericEncoder.encode(message));
                        envelope.setDecoded(false);
                    }

                    member.send(envelope);
                }

                break;
            case RANDOM:

                if(registry.getRegisteredMembers(topic).size() > 0) {
                    int index = (int)(Math.random() *
                            registry.getRegisteredMembers(topic).size());

                    Member member = registry.getRegisteredMembers(topic).get(index);

                    if(me.equals(member)) {
                        envelope.setMessage(message);
                        envelope.setDecoded(true);
                    } else {
                        envelope.setPayload(GenericEncoder.encode(message));
                        envelope.setDecoded(false);
                    }

                    member.send(envelope);
                }
                
                break;
            case BROADCAST:
            default:
                for(Member member : registry.getRegisteredMembers(topic)) {

                    if(me.equals(member)) {
                        envelope.setMessage(message);
                        envelope.setDecoded(true);
                    } else {
                        envelope.setPayload(GenericEncoder.encode(message));
                        envelope.setDecoded(false);
                    }

                    member.send(envelope);
                }
        }
    }

    public <T> void sendMessage(String topic, String partition, T message) throws IOException {
        sendMessage(topic, partition, message, 0);
    }

    public Collection<Member> getAllMembers() {
        return memberHolder.getAllMembers();
    }
    
    public Collection<Member> getActiveMembers() {
        return memberHolder.getActiveMembers();
    }
    
    public Collection<Member> getDeadMembers() {
        return memberHolder.getDeadMembers();
    }

    public Member getMe() {
        return me;
    }

    public void initialize() {

        String protocol = Parameters.INSTANCE.getProperty(PROTOCOL_PROPERTY, DEFAULT_PROTOCOL);
        String gossipPort = Parameters.INSTANCE.getProperty(GOSSIP_PORT_PROPERTY);
        String dataPort = Parameters.INSTANCE.getProperty(DATA_PORT_PROPERTY);

        int gossipPortInt;
        int dataPortInt;

        if(gossipPort == null) {
            LOG.trace("Finding a random port for gossiping.");
            gossipPortInt = NetworkUtil.getFreePort();
        } else {
            gossipPortInt = Integer.parseInt(gossipPort);
        }

        if(dataPort == null) {
            LOG.trace("Finding a random port for data.");
            dataPortInt = NetworkUtil.getFreePort();
        } else {
            dataPortInt = Integer.parseInt(dataPort);
        }

        String myAddress = NetworkUtil.getIp();

        if(LOG.isDebugEnabled()) {
            StringBuilder greeting = new StringBuilder();
            LOG.trace(greeting
                    .append("Greetings. I am ")
                    .append(myAddress)
                    .append(":")
                    .append(gossipPortInt)
                    .append(":")
                    .append(dataPortInt)
                    .toString());
        }

        if(protocol.equalsIgnoreCase("udp")) {
            LOG.info("Running over UDP");
            me = new MeMemberUDP(myAddress, gossipPortInt, dataPortInt, memberHolder, registry);
        } else {
            LOG.info("Running over TCP");
            me = new MeMemberTCP(myAddress, gossipPortInt, dataPortInt, memberHolder, registry);
        }
        me.setStatus(MemberStatus.Alive);
        me.initialize();
        memberHolder.updateMemberStatus(me);

        me.addGossipConsumer(new GossipListener() {
            @Override
            public void accept(GossipMessage message) {
                handleGossipMessage(message);
            }
        });

        multicast.initialize(me);

        gossiper = new Gossiper(me, memberHolder, registry);

        registry.setMe(me);
    }

    public void join(String ip) {
        
    }

    public void leave() {
        
    }

    public void shutdown() {
        shuttingDown = true;
        
        gossiper.shutdown();

        try {
            multicast.shutdown();
        } catch (InterruptedException ex) {
            LOG.warn("Interrupted while shutting down multicast agent.", ex);
        }

        for(Member member : memberHolder.getAllMembers()) {
            ((AbstractMember)member).shutdown();
        }

        memberHolder.clear();
    }

    private void handleGossipMessage(GossipMessage message) {
        if(shuttingDown) {
            return;
        }

        String senderKey = MemberKey.getKey(message);
        boolean updateTags = false;

        for(int i = 0; i < message.getMembers().size(); ++i) {

            String key = message.getMembers().get(i);

            Member m = memberHolder.getMember(key);
            if(m == null) {
                String protocol = Parameters.INSTANCE.getProperty(PROTOCOL_PROPERTY, DEFAULT_PROTOCOL);
                if(protocol.equalsIgnoreCase("udp")) {
                    if(LOG.isTraceEnabled()) {
                        LOG.trace(new StringBuilder()
                                .append(MemberKey.getKey(me))
                                .append(" Discovered new UDP member through gossip: ")
                                .append(message.getIp())
                                .append(":")
                                .append(message.getGossipPort())
                                .append(":").append(message.getDataPort()).toString());
                    }
                    m = new RemoteMemberUDP(memberHolder);
                } else {
                    if(LOG.isTraceEnabled()) {
                        LOG.trace(new StringBuilder()
                                .append(MemberKey.getKey(me))
                                .append(" Discovered new TCP member through gossip: ")
                                .append(message.getIp())
                                .append(":")
                                .append(message.getGossipPort())
                                .append(":").append(message.getDataPort()).toString());
                    }
                    m = new RemoteMemberTCP(memberHolder);
                }
                String[] values = key.split(":");
                m.setIp(values[0]);
                m.setGossipPort(Integer.parseInt(values[1]));
                m.setDataPort(Integer.parseInt(values[2]));
                ((AbstractMember)m).initialize();
            }

            memberHolder.updateMemberStatus(m);

            int memberClock = message.getClock().get(i);
            int knownMemberClock = m.getSequence().get();

            if(memberClock > knownMemberClock) {
                if(key.equals(senderKey)) {
                    updateTags = true;
                }

                m.getSequence().set(memberClock);
                List<String> topics = message.getListeners().get(key);
                if(topics == null) {
                    topics = Collections.emptyList();
                }

                List<Registration> toRemove = new ArrayList<>();
                for(Registration reg : registry.getAllRegistrations()) {
                    if(reg.getMember().equals(m)) {
                        if(!topics.contains(TopicUtils.getTopicString(reg.getTopic(), reg.getPartition()))) {
                            toRemove.add(reg);
                        }
                    }
                }
                registry.removeRegistrations(toRemove);
                for(String topicString : topics) {
                    String topic = TopicUtils.getTopic(topicString);
                    String partition = TopicUtils.getPartition(topicString);
                    if(!registry.getRegisteredMembers(topic).contains(m)) {
                        registry.registerMemberForTopic(topic, partition, m);
                    }
                }
            }
        }

        if(updateTags) {
            Member m = memberHolder.getMember(senderKey);
            m.getTags().clear();
            m.getTags().putAll(message.getTags());
        }
    }
}
