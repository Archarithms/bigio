/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.bigio.core;

import com.a2i.bigio.Parameters;
import com.a2i.bigio.core.member.Member;
import com.a2i.bigio.core.member.MemberHolder;
import com.a2i.bigio.core.member.MemberKey;
import com.a2i.bigio.core.member.RemoteMember;
import com.a2i.bigio.util.TimeUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class Gossiper {

    private static final String GOSSIP_INTERVAL_PROPERTY = "com.a2i.gossip.interval";
    private static final String CLEANUP_INTERVAL_PROPERTY = "com.a2i.gossip.cleanup";
    private static final String DEFAULT_GOSSIP_INTERVAL = "250";
    private static final String DEFAULT_CLEANUP_INTERVAL = "10000";
    
    private static final Logger LOG = LoggerFactory.getLogger(Gossiper.class);

    private int gossipInterval = 250;
    
    private int cleanupInterval = 10000; 

    private final Random random = new Random();

    private final Member me;

    private final MemberHolder memberHolder;
    private final ListenerRegistry registry;

    private final MembershipGossiper thread = new MembershipGossiper();

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public Gossiper(Member me, MemberHolder memberHolder, ListenerRegistry registry) {

        this.me = me;
        this.memberHolder = memberHolder;
        this.registry = registry;

        gossipInterval = Integer.parseInt(Parameters.INSTANCE.getProperty(
                GOSSIP_INTERVAL_PROPERTY, DEFAULT_GOSSIP_INTERVAL));
        cleanupInterval = Integer.parseInt(Parameters.INSTANCE.getProperty(
                CLEANUP_INTERVAL_PROPERTY, DEFAULT_CLEANUP_INTERVAL));

        thread.start();
    }

    public void shutdown() {
        thread.shutdown();
    }

    private void sendMembershipList() {
        
        Member member = getRandomMember();

        if (member != null) {
            GossipMessage memberList = new GossipMessage();
            memberList.setIp(me.getIp());
            memberList.setGossipPort(me.getGossipPort());
            memberList.setDataPort(me.getDataPort());
            memberList.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
            memberList.getTags().putAll(me.getTags());

            for(int i = 0; i < memberHolder.getActiveMembers().size(); ++i) {
                Member m = memberHolder.getActiveMembers().get(i);
                memberList.getMembers().add(MemberKey.getKey(m));

                if(m == me) {
                    memberList.getClock().add(i, m.getSequence().incrementAndGet());
                } else {
                    memberList.getClock().add(i, m.getSequence().get());
                }
            }

            for(Registration registration: registry.getAllRegistrations()) {
                String key = MemberKey.getKey(registration.getMember());
                if(memberList.getListeners().get(key) == null) {
                    memberList.getListeners().put(key, new ArrayList<String>());
                }
                memberList.getListeners().get(key).add(registration.getTopic());
            }

            try {
                ((RemoteMember)member).gossip(memberList);
            } catch(IOException ex) {
                LOG.error("Exception sending member list.", ex);
            }
        }
    }

    private Member getRandomMember() {
        Member chosenMember = null;

        if (memberHolder.getActiveMembers().size() > 1) {
            int tries = 10;
            do {
                int randomNeighborIndex = random.nextInt(memberHolder.getActiveMembers().size());
                chosenMember = memberHolder.getActiveMembers().get(randomNeighborIndex);

                if (--tries <= 0) {
                    chosenMember = null;
                    break;
                }
            } while (me.equals(chosenMember));
        }

        if(me.equals(chosenMember)) {
            return null;
        } else {
            return chosenMember;
        }
    }

    private class MembershipGossiper extends Thread {

        private final AtomicBoolean keepRunning;

        public MembershipGossiper() {
            this.keepRunning = new AtomicBoolean(true);
        }

        public void shutdown() {
            keepRunning.set(false);
        }

        @Override
        public void run() {
            while (this.keepRunning.get()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(gossipInterval);
                    sendMembershipList();
                } catch (InterruptedException ex) {
                    LOG.error("Interrupted.", ex);
                    keepRunning.set(false);
                }
            }
        }
    }
}
