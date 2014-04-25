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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class TestRemoteMessagesTCP {

    private static final Logger LOG = LoggerFactory.getLogger(TestRemoteMessagesTCP.class);

    private static final MyMessageListener listener = new MyMessageListener();
    private static final VolumeListener volumeListener = new VolumeListener();
    private static final DelayedMessageListener delayedListener = new DelayedMessageListener();

    private static final String MESSAGE = "This is a test";

    private static final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);

    private static boolean failed = false;

    private static Speaker speaker1;
    private static Speaker speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        speaker1 = Starter.bootstrap();
        speaker2 = Starter.bootstrap();

        speaker2.addListener("MyTCPTopic", listener);
        speaker2.addListener("VolumeTopic", volumeListener);
        speaker2.addListener("DelayedTCPTopic", delayedListener);
        speaker2.addListener("AllTCPPartitionTopic", ".*", listener);
        speaker2.addListener("SpecificTCPPartitionTopic", "MyTCPPartition", listener);
        
        Thread.sleep(1000l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();
        
        Thread.sleep(1000l);
    }

    @Test
    public void testVolume() throws Exception {
        failed = false;
        
        for(int i = 0; i < 1000; ++i) {
            speaker1.send("VolumeTopic", new MyMessage(MESSAGE + i));
        }

        Thread.sleep(1000l);

        assertTrue(volumeListener.counter == 1000);
    }

    @Test
    public void testMessage() throws Exception {
        failed = false;
        
        speaker1.send("MyTCPTopic", new MyMessage(MESSAGE + "1"));
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

        speaker2.send("MyTCPTopic", new MyMessage(MESSAGE + "1"));
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
        
        speaker1.send("AllTCPPartitionTopic", "MyTCPPartition", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker1.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker2.send("AllTCPPartitionTopic", "MyTCPPartition", new MyMessage(MESSAGE + "1"));
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
        
        speaker1.send("SpecificTCPPartitionTopic", "MyTCPPartition", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker1.send("SpecificTCPPartitionTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker1.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker1.send("SpecificTCPPartitionTopic", "BadPartition", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker2.send("SpecificTCPPartitionTopic", "MyTCPPartition", new MyMessage(MESSAGE + "1"));
        m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker2.send("SpecificTCPPartitionTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker2.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker2.send("SpecificTCPPartitionTopic", "BadPartition", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testDelay() throws Exception {
        failed = false;

        speaker1.send("DelayedTCPTopic", new MyMessage(MESSAGE + "8"), 2000);
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

    private static class VolumeListener implements MessageListener<MyMessage> {
        public int counter = 0;

        @Override
        public void receive(MyMessage message) {
            ++counter;
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
