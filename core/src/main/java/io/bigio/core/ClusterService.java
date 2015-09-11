/*
 * Copyright (c) 2015, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio.core;

import io.bigio.Component;
import io.bigio.DeliveryType;
import io.bigio.Inject;
import io.bigio.Interceptor;
import io.bigio.MessageListener;
import io.bigio.Parameters;
import io.bigio.core.codec.GenericCodec;
import io.bigio.core.member.AbstractMember;
import io.bigio.core.member.MeMember;
import io.bigio.core.member.MeMemberTCP;
import io.bigio.core.member.MeMemberUDP;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberHolder;
import io.bigio.core.member.MemberKey;
import io.bigio.core.member.MemberStatus;
import io.bigio.core.member.RemoteMemberTCP;
import io.bigio.core.member.RemoteMemberUDP;
import io.bigio.util.NetworkUtil;
import io.bigio.util.TimeUtil;
import io.bigio.util.TopicUtils;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles communication between nodes in a cluster.
 * 
 * @author Andy Trimble
 */
@Component
public class ClusterService {

    public static final String PROTOCOL_PROPERTY = "io.bigio.protocol";
    public static final String DEFAULT_PROTOCOL = "tcp";
    private static final String GOSSIP_PORT_PROPERTY = "io.bigio.port.gossip";
    private static final String DATA_PORT_PROPERTY = "io.bigio.port.data";

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

    /**
     * Default constructor.
     */
    public ClusterService() {
        
    }

    /**
     * Set the multicast discovery object. Only used in bootstrapping.
     * 
     * @param multicast the multicast discovery object.
     */
    public void setMulticastDiscovery(MCDiscovery multicast) {
        this.multicast = multicast;
    }

    /**
     * Set the member container. Only used in bootstrapping.
     * 
     * @param memberHolder the member container.
     */
    public void setMemberHolder(MemberHolder memberHolder) {
        this.memberHolder = memberHolder;
    }

    /**
     * Set the listener registry. Only used in bootstrapping.
     * 
     * @param registry the listener registry.
     */
    public void setRegistry(ListenerRegistry registry) {
        this.registry = registry;
    }

    /**
     * Get the listener registry.
     * 
     * @return the listener registry.
     */
    public ListenerRegistry getRegistry() {
        return registry;
    }

    /**
     * Set the delivery method.
     * 
     * @param topic a topic.
     * @param type the type of method delivery.
     */
    public void setDeliveryType(String topic, DeliveryType type) {
        deliveries.put(topic, type);
        if(type == DeliveryType.ROUND_ROBIN) {
            roundRobinIndex.put(topic, 0);
        }
    }

    /**
     * Add an interceptor to a topic.
     * 
     * @param topic a topic.
     * @param interceptor an interceptor.
     */
    public void addInterceptor(String topic, Interceptor interceptor) {
        registry.addInterceptor(topic, interceptor);
    }

    /**
     * Add a topic/partition listener.
     * 
     * @param <T> the message type.
     * @param topic a topic.
     * @param partition a partition.
     * @param consumer a listener.
     */
    public <T> void addListener(String topic, String partition, MessageListener<T> consumer) {
        registry.registerMemberForTopic(topic, partition, me);
        registry.addLocalListener(topic, partition, consumer);
    }

    /**
     * This method is no longer supported. Remove all listeners on a topic.
     * 
     * @param topic a topic.
     * @param partition
     */
    public void removeAllListeners(String topic, String partition) {
        throw new UnsupportedOperationException("Listener removal not supported at this time.");
        //registry.removeAllLocalListeners(topic, partition);
    }

    /**
     * Setn a message.
     * 
     * @param <T> a message type.
     * @param topic a topic.
     * @param partition a partition.
     * @param message a message.
     * @param offsetMilliseconds time offset of the message.
     * @throws IOException in case of error in delivery.
     */
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

                if(!registry.getRegisteredMembers(topic).isEmpty()) {

                    int index = (roundRobinIndex.get(topic) + 1) % 
                            registry.getRegisteredMembers(topic).size();
                    roundRobinIndex.put(topic, index);
                
                    Member member = registry.getRegisteredMembers(topic).get(index);

                    if(me.equals(member)) {
                        envelope.setMessage(message);
                        envelope.setDecoded(true);
                    } else {
                        envelope.setPayload(GenericCodec.encode(message));
                        envelope.setDecoded(false);
                    }

                    member.send(envelope);
                }

                break;
            case RANDOM:

                if(!registry.getRegisteredMembers(topic).isEmpty()) {
                    int index = (int)(Math.random() *
                            registry.getRegisteredMembers(topic).size());

                    Member member = registry.getRegisteredMembers(topic).get(index);

                    if(me.equals(member)) {
                        envelope.setMessage(message);
                        envelope.setDecoded(true);
                    } else {
                        envelope.setPayload(GenericCodec.encode(message));
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
                        envelope.setPayload(GenericCodec.encode(message));
                        envelope.setDecoded(false);
                    }

                    member.send(envelope);
                }
        }
    }

    /**
     * Set a message on a topic/partition with no execution offset.
     * 
     * @param <T> a message type.
     * @param topic a topic.
     * @param partition a partition.
     * @param message a message.
     * @throws IOException in case of delivery error.
     */
    public <T> void sendMessage(String topic, String partition, T message) throws IOException {
        sendMessage(topic, partition, message, 0);
    }

    /**
     * Get the list of known members. Members returned by this method may be
     * either active or dead.
     * 
     * @return the list of known members.
     */
    public Collection<Member> getAllMembers() {
        return memberHolder.getAllMembers();
    }
    
    /**
     * Get the list of active members.
     * 
     * @return the list of active members.
     */
    public Collection<Member> getActiveMembers() {
        return memberHolder.getActiveMembers();
    }
    
    /**
     * Get the list of dead members.
     * 
     * @return the list of dead members.
     */
    public Collection<Member> getDeadMembers() {
        return memberHolder.getDeadMembers();
    }

    /**
     * Get the member representing this BigIO instance.
     * 
     * @return the current member.
     */
    public Member getMe() {
        return me;
    }

    /**
     * Initialize communication.
     * 
     * @throws java.net.SocketException in the even a free port cannot be found.
     */
    public void initialize() throws SocketException {

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

        if("udp".equalsIgnoreCase(protocol)) {
            LOG.debug("Running over UDP");
            me = new MeMemberUDP(myAddress, gossipPortInt, dataPortInt, memberHolder, registry);
        } else {
            LOG.debug("Running over TCP");
            me = new MeMemberTCP(myAddress, gossipPortInt, dataPortInt, memberHolder, registry);
        }
        me.setStatus(MemberStatus.Alive);
        me.initialize();
        memberHolder.updateMemberStatus(me);

        me.addGossipConsumer((GossipMessage message) -> {
            handleGossipMessage(message);
        });

        multicast.initialize(me);

        gossiper = new Gossiper(me, memberHolder, registry);

        registry.setMe(me);
    }

    /**
     * TODO: Implement manually joining a cluster.
     * 
     * @param ip an initial peer to which to connect.
     */
    public void join(String ip) {
        
    }

    /**
     * TODO: Implement manually leaving a cluster.
     */
    public void leave() {
        
    }

    /**
     * Shutdown communication objects.
     */
    public void shutdown() {
        shuttingDown = true;
        
        gossiper.shutdown();

        multicast.shutdown();

        memberHolder.getAllMembers().stream().forEach((member) -> {
            ((AbstractMember)member).shutdown();
        });

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
                if("udp".equalsIgnoreCase(protocol)) {
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
                if(message.getPublicKey() != null) {
                    m.setPublicKey(message.getPublicKey());
                }
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
