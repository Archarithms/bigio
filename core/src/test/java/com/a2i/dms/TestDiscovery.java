/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms;

import com.a2i.dms.core.MessageListener;
import com.a2i.dms.core.member.Member;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class TestDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(TestDiscovery.class);

    private static final MyMessageListener listener = new MyMessageListener();
    private static final DelayedMessageListener delayedListener = new DelayedMessageListener();

    private static final String MESSAGE = "This is a test";

    // I think there's a bug in reactor that's causing messages to come
    // through even after a listener has been removed. That's why this
    // is of size 2.
    //private final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);
    private static final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(2);

    private static boolean failed = false;

    private static Speaker speaker1;
    private static Speaker speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        speaker1 = Starter.bootstrap();
        speaker2 = Starter.bootstrap();

        speaker2.addListener("MyTopic", listener);
        speaker2.addListener("DelayedTopic", delayedListener);
        
        Thread.sleep(500l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();
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

//        speaker1.getClusterService().getAllMembers()
    }

    private static class MyMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a message " + message.getMessage());
            
            boolean success = queue.offer(message);

            if (!success) {
                failed = true;
            }
        }
    }

    private static class DelayedMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a delayed message " + message.getMessage());
            
            boolean success = queue.offer(message);

            if (!success) {
                failed = true;
            }
        }
    }

    @Message
    private static final class MyMessage {

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
