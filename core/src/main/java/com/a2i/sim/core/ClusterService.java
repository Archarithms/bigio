/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core;

import com.a2i.sim.Interceptor;
import com.a2i.sim.core.member.AbstractMember;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.core.member.MemberKey;
import com.a2i.sim.core.member.MemberHolder;
import com.a2i.sim.core.member.MeMember;
import com.a2i.sim.core.member.MemberStatus;
import com.a2i.sim.util.NetworkUtil;
import com.a2i.sim.util.TimeUtil;
import com.a2i.sim.core.codec.GenericEncoder;
import com.a2i.sim.Parameters;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class ClusterService {

    private static final String GOSSIP_PORT_PROPERTY = "com.a2i.port.gossip";
    private static final String DATA_PORT_PROPERTY = "com.a2i.port.data";

    @Autowired
    private MCDiscovery multicast;

    private MeMember me;

    private Gossiper gossiper;

    private static final Logger LOG = LoggerFactory.getLogger(ClusterService.class);

    public ClusterService() {
        
    }

    public void addInterceptor(Interceptor interceptor) {
        ListenerRegistry.INSTANCE.addInterceptor(interceptor);
    }

    public void addInterceptor(Interceptor interceptor, String topic) {
        ListenerRegistry.INSTANCE.addInterceptor(interceptor, topic);
    }

    public <T> void addListener(String topic, MessageListener<T> consumer) {
        ListenerRegistry.INSTANCE.registerMemberForTopic(topic, me);
        ListenerRegistry.INSTANCE.addLocalListener(topic, consumer);
    }

    public <T> void removeListener(MessageListener<T> consumer) {
        ListenerRegistry.INSTANCE.removeLocalListener(consumer);
    }

    public void removeAllListeners(String topic) {
        ListenerRegistry.INSTANCE.removeAllLocalListeners(topic);
    }

    public <T> void sendMessage(String topic, T message, int offsetMilliseconds) throws IOException {
        Envelope envelope = new Envelope();
        envelope.setDecoded(false);
        envelope.setExecuteTime(offsetMilliseconds);
        envelope.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
        envelope.setSenderKey(MemberKey.getKey(me));
        envelope.setTopic(topic);
        envelope.setClassName(message.getClass().getName());

        for(Member member : ListenerRegistry.INSTANCE.getRegisteredMembers(topic)) {
            
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

    public <T> void sendMessage(String topic, T message) throws IOException {
        sendMessage(topic, message, 0);
    }

    public Collection<Member> getAllMembers() {
        return MemberHolder.INSTANCE.getAllMembers();
    }
    
    public Collection<Member> getActiveMembers() {
        return MemberHolder.INSTANCE.getActiveMembers();
    }
    
    public Collection<Member> getDeadMembers() {
        return MemberHolder.INSTANCE.getDeadMembers();
    }

    public Member getMe() {
        return me;
    }

    public void initialize() {

        String gossipPort = Parameters.INSTANCE.getProperty(GOSSIP_PORT_PROPERTY);
        String dataPort = Parameters.INSTANCE.getProperty(DATA_PORT_PROPERTY);

        int gossipPortInt;
        int dataPortInt;

        if(gossipPort == null) {
            LOG.debug("Finding a random port for gossiping.");
            gossipPortInt = NetworkUtil.getFreePort();
        } else {
            gossipPortInt = Integer.parseInt(gossipPort);
        }

        if(dataPort == null) {
            LOG.debug("Finding a random port for data.");
            dataPortInt = NetworkUtil.getFreePort();
        } else {
            dataPortInt = Integer.parseInt(dataPort);
        }

        String myAddress = NetworkUtil.getIp();

        if(LOG.isDebugEnabled()) {
            StringBuilder greeting = new StringBuilder();
            LOG.debug(greeting
                    .append("Greetings. I am ")
                    .append(myAddress)
                    .append(":")
                    .append(gossipPortInt)
                    .append(":")
                    .append(dataPortInt)
                    .toString());
        }

        me = new MeMember(myAddress, gossipPortInt, dataPortInt);
        me.setStatus(MemberStatus.Alive);
        me.initialize();
        MemberHolder.INSTANCE.updateMemberStatus(me);

        me.addGossipConsumer(new GossipListener() {
            @Override
            public void accept(GossipMessage message) {
                handleGossipMessage(message);
            }
        });

        multicast.initialize(me);

        gossiper = new Gossiper(me);

        ListenerRegistry.INSTANCE.setMe(me);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public void members() {
        for(Member member : getAllMembers()) {
            LOG.info(member.toString());
        }
    }

    public void join(String ip) {
        
    }

    public void leave() {
        
    }

    public void shutdown() {
        try {
            multicast.shutdown();
        } catch (InterruptedException ex) {
            LOG.warn("Interrupted while shutting down multicast agent.", ex);
        }

        for(Member member : MemberHolder.INSTANCE.getAllMembers()) {
            ((AbstractMember)member).shutdown();
        }
    }

    private void handleGossipMessage(GossipMessage message) {

//        LOG.info("Received: " + message.toString());

        String senderKey = MemberKey.getKey(message);
        boolean updateTags = false;

        for(int i = 0; i < message.getMembers().size(); ++i) {

            String key = message.getMembers().get(i);

            Member m = MemberHolder.INSTANCE.getMember(key);
            if(m == null) {
                m = MemberKey.decode(key);
                ((AbstractMember)m).initialize();
            }

            MemberHolder.INSTANCE.updateMemberStatus(m);

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
                for(Registration reg : ListenerRegistry.INSTANCE.getAllRegistrations()) {
                    if(reg.getMember().equals(m)) {
                        if(!topics.contains(reg.getTopic())) {
                            toRemove.add(reg);
                        }
                    }
                }
                ListenerRegistry.INSTANCE.removeRegistrations(toRemove);
                for(String topic : topics) {
                    if(!ListenerRegistry.INSTANCE.getRegisteredMembers(topic).contains(m)) {
                        ListenerRegistry.INSTANCE.registerMemberForTopic(topic, m);
                    }
                }
            }
        }

        if(updateTags) {
            Member m = MemberHolder.INSTANCE.getMember(senderKey);
            m.getTags().clear();
            m.getTags().putAll(message.getTags());
        }
    }
}
