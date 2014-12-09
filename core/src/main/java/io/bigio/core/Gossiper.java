/*
 * Copyright (c) 2014, Archarithms Inc.
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

import io.bigio.Parameters;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberHolder;
import io.bigio.core.member.MemberKey;
import io.bigio.core.member.RemoteMember;
import io.bigio.util.TimeUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the gossip protocol implementation.
 * 
 * @author Andy Trimble
 */
public class Gossiper {

    private static final String GOSSIP_INTERVAL_PROPERTY = "io.bigio.gossip.interval";
    private static final String CLEANUP_INTERVAL_PROPERTY = "io.bigio.gossip.cleanup";
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

    /**
     * Constructor.
     * 
     * @param me the current BigIO member.
     * @param memberHolder the member container.
     * @param registry the listener container.
     */
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

    /**
     * Shutdown the gossiping thread.
     */
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
            memberList.setPublicKey(me.getPublicKey());
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

            registry.getAllRegistrations().stream().forEach((registration) -> {
                String key = MemberKey.getKey(registration.getMember());
                if(memberList.getListeners().get(key) == null) {
                    memberList.getListeners().put(key, new ArrayList<>());
                }
                memberList.getListeners().get(key).add(registration.getTopic());
            });

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
