/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms;

import com.a2i.dms.core.MessageListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class TestRemoteMessagesUDP {

    private static final Logger LOG = LoggerFactory.getLogger(TestRemoteMessagesUDP.class);

    private static final MyMessageListener listener = new MyMessageListener();
    private static final DelayedMessageListener delayedListener = new DelayedMessageListener();

    private static final String MESSAGE = "This is a test";

    private static final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);

    private static boolean failed = false;

    private static Speaker speaker1;
    private static Speaker speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        System.setProperty("com.a2i.protocol", "udp");

        speaker1 = Starter.bootstrap();
        speaker2 = Starter.bootstrap();

        speaker2.addListener("MyTopic", listener);
        speaker2.addListener("DelayedTopic", delayedListener);
        speaker2.addListener("AllPartitionTopic", ".*", listener);
        speaker2.addListener("SpecificPartitionTopic", "MyPartition", listener);
        
        Thread.sleep(1000l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();

        Thread.sleep(1000l);
        
        System.setProperty("com.a2i.protocol", "tcp");
    }

    @Test
    public void testMessage() throws Exception {
        failed = false;
        
        speaker1.send("MyTopic", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker1.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();

        speaker2.send("MyTopic", new MyMessage(MESSAGE + "1"));
        m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker2.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testAllPartitions() throws Exception {
        failed = false;
        
        speaker1.send("AllPartitionTopic", "MyPartition", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker1.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker2.send("AllPartitionTopic", "MyPartition", new MyMessage(MESSAGE + "1"));
        m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker2.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testSpecificPartitions() throws Exception {
        failed = false;
        
        speaker1.send("SpecificPartitionTopic", "MyPartition", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker1.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker1.send("SpecificPartitionTopic", "BadPartition", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker2.send("SpecificPartitionTopic", "MyPartition", new MyMessage(MESSAGE + "1"));
        m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker2.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker2.send("SpecificPartitionTopic", "BadPartition", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testDelay() throws Exception {
        failed = false;

        speaker1.send("DelayedTopic", new MyMessage(MESSAGE + "8"), 2000);
        MyMessage m = queue.poll(1000l, TimeUnit.MILLISECONDS);
        assertNull(m);

        failed = false;

        m = queue.poll(2500l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "8");

        if(failed) {
            fail();
        }

        queue.clear();
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
