/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio;

import io.bigio.Message;
import io.bigio.Starter;
import io.bigio.Speaker;
import io.bigio.core.ClusterService;
import io.bigio.core.member.Member;
import java.util.Collection;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class TestDiscoveryUDP {

    private static final Logger LOG = LoggerFactory.getLogger(TestDiscoveryUDP.class);

    private static final MyMessageListener listener = new MyMessageListener();
    private static final DelayedMessageListener delayedListener = new DelayedMessageListener();

    private static Speaker speaker1;
    private static Speaker speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        Parameters.INSTANCE.setProperty(ClusterService.PROTOCOL_PROPERTY, "udp");
        
        speaker1 = Starter.bootstrap();
        speaker2 = Starter.bootstrap();

        speaker2.addListener("MyTopic", listener);
        speaker2.addListener("DelayedTopic", delayedListener);
        
        Thread.sleep(2000l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();

        Parameters.INSTANCE.setProperty(ClusterService.PROTOCOL_PROPERTY, "tcp");
    }

    @Test
    public void testDiscovery() throws InterruptedException {

        Collection<Member> members1 = speaker1.listMembers();
        Collection<Member> members2 = speaker2.listMembers();

        assertTrue(members1.size() == members2.size());

        assertTrue(members1.containsAll(members2));
        assertTrue(members2.containsAll(members1));
        assertTrue(members2.contains(speaker1.getMe()));
        assertTrue(members1.contains(speaker2.getMe()));

        List<Member> regs = speaker1.getClusterService().getRegistry().getRegisteredMembers("MyTopic");
        /* System.out.println("Members in speaker 1");
        for(Member m : regs) {
            System.out.println(m.toString());
        } */
        assertTrue(regs.contains(speaker2.getMe()));
        assertFalse(regs.contains(speaker1.getMe()));

        regs = speaker2.getClusterService().getRegistry().getRegisteredMembers("MyTopic");
        assertTrue(regs.contains(speaker2.getMe()));
        assertFalse(regs.contains(speaker1.getMe()));

        regs = speaker1.getClusterService().getRegistry().getRegisteredMembers("DelayedTopic");
        assertTrue(regs.contains(speaker2.getMe()));
        assertFalse(regs.contains(speaker1.getMe()));

        regs = speaker2.getClusterService().getRegistry().getRegisteredMembers("DelayedTopic");
        assertTrue(regs.contains(speaker2.getMe()));
        assertFalse(regs.contains(speaker1.getMe()));
    }


    private static class MyMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a message " + message.getMessage());
        }
    }

    private static class DelayedMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a delayed message " + message.getMessage());
        }
    }

    @Message
    public static final class MyMessage {

        private String message;

        public MyMessage() {

        }

        public MyMessage(String message) {
            this.message = message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
