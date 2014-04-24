/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class MessageTest {

    private static final Logger LOG = LoggerFactory.getLogger(MessageTest.class);

    private static Speaker speaker;

    private static final String MESSAGE = "This is a test";

    private final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);

    private final MyMessageListener listener = new MyMessageListener();
    private final DelayedMessageListener delayedListener = new DelayedMessageListener();

    private static boolean failed = false;

    @BeforeClass
    public static void init() {
        speaker = Starter.bootstrap();
    }

    @AfterClass
    public static void shutdown() {
        speaker.shutdown();
    }

    @Test
    public void testMessage() throws Exception {
        failed = false;
        
        speaker.addListener("MyTopic", listener);
        speaker.send("MyTopic", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker.removeAllListeners("MyTopic");
        speaker.send("MyTopic", new MyMessage(MESSAGE + "3"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testAllPartitions() throws Exception {
        failed = false;
        
        speaker.addListener("MyTopic", ".*", listener);
        speaker.send("MyTopic", "MyPartition", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker.removeAllListeners("MyTopic");
        speaker.send("MyTopic", new MyMessage(MESSAGE + "3"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testSpecificPartitions() throws Exception {
        failed = false;
        
        speaker.addListener("MyTopic", "MyPartition", listener);
        speaker.send("MyTopic", "MyPartition", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if(failed) {
            fail();
        }

        speaker.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker.send("MyTopic", "BadPartition", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker.removeAllListeners("MyTopic");
        speaker.send("MyTopic", new MyMessage(MESSAGE + "3"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testRoundRobin() throws Exception {
        failed = false;
        
        speaker.setDeliveryType("MyTopic", DeliveryType.ROUND_ROBIN);
        speaker.addListener("MyTopic", listener);
        speaker.send("MyTopic", new MyMessage(MESSAGE + "4"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "4");

        if(failed) {
            fail();
        }

        speaker.send("BadTopic", new MyMessage(MESSAGE + "5"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker.removeAllListeners("MyTopic");
        speaker.send("MyTopic", new MyMessage(MESSAGE + "5"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testRandom() throws Exception {
        failed = false;
        
        speaker.setDeliveryType("MyTopic", DeliveryType.RANDOM);
        speaker.addListener("MyTopic", listener);
        speaker.send("MyTopic", new MyMessage(MESSAGE + "6"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "6");

        if(failed) {
            fail();
        }

        speaker.send("BadTopic", new MyMessage(MESSAGE + "7"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker.removeAllListeners("MyTopic");
        speaker.send("MyTopic", new MyMessage(MESSAGE + "7"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    @Test
    public void testDelay() throws Exception {
        failed = false;

        speaker.addListener("DelayedTopic", delayedListener);
        speaker.send("DelayedTopic", new MyMessage(MESSAGE + "8"), 2000);
        MyMessage m = queue.poll(1000l, TimeUnit.MILLISECONDS);
        assertNull(m);

        failed = false;

        m = queue.poll(2500l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "8");

        if(failed) {
            fail();
        }

        speaker.removeAllListeners("DelayedTopic");
        queue.clear();
    }

    private class MyMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a message " + message.getMessage());
            
            boolean success = queue.offer(message);

            if (!success) {
                failed = true;
            }
        }
    }

    private class DelayedMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a delayed message " + message.getMessage());
            
            boolean success = queue.offer(message);

            if (!success) {
                failed = true;
            }
        }
    }

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
