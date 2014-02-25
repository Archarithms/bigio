/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.sim.core;

import com.a2i.sim.util.TimeUtil;
import com.a2i.sim.Parameters;
import java.io.IOException;
import java.util.Iterator;
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

    private int gossipInterval = 1000;
    
    private int cleanupInterval = 10000; 

    private final Random random = new Random();

    private final Member me;

    private final MembershipGossiper thread = new MembershipGossiper();

    public Gossiper(Member me) {

        this.me = me;

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
            memberList.setSequence(me.getSequence().incrementAndGet());
            memberList.setIp(me.getIp());
            memberList.setGossipPort(me.getGossipPort());
            memberList.setDataPort(me.getDataPort());
            memberList.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
            memberList.getTags().putAll(me.getTags());

            for(Member m : MemberHolder.INSTANCE.getActiveMembers()) {
                memberList.getMembers().add(MemberKey.getKey(m));
            }

            for(String topic : ListenerRegistry.INSTANCE.getLocalTopicsOfInterest()) {
                memberList.getListeners().put(MemberKey.getKey(me), topic);
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

        if (MemberHolder.INSTANCE.getActiveMembers().size() > 1) {
            int tries = 10;
            do {
                int randomNeighborIndex = random.nextInt(MemberHolder.INSTANCE.getActiveMembers().size());
                Iterator<Member> iter = MemberHolder.INSTANCE.getActiveMembers().iterator();

                int i = -1;
                do {
                    chosenMember = iter.next();
                    ++i;
                } while(i != randomNeighborIndex && iter.hasNext());

                if (--tries <= 0) {
                    chosenMember = null;
                    break;
                }
            } while (me.equals(chosenMember));
        }

        return chosenMember;
    }

    private class MembershipGossiper extends Thread {

        private AtomicBoolean keepRunning;

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

            this.keepRunning = null;
        }
    }
}
