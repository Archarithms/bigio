/*
 * Copyright 2014 Archarithms Inc.
 */
package io.bigio;

import io.bigio.core.member.MeMember;
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
public class TestTCPEncryption {

    private static final Logger LOG = LoggerFactory.getLogger(TestTCPEncryption.class);

    private static final MyMessageListener listener = new MyMessageListener();
    private static final VolumeListener volumeListener = new VolumeListener();

    private static final String MESSAGE = "This is a test";

    private static final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);

    private static boolean failed = false;

    private static Speaker speaker1;
    private static Speaker speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        Parameters.INSTANCE.setProperty(MeMember.ENCRYPTION_PROPERTY, "true");

        speaker1 = Starter.bootstrap();
        speaker2 = Starter.bootstrap();

        speaker2.addListener("MyTCPTopic", listener);
        speaker2.addListener("VolumeTopic", volumeListener);

        Thread.sleep(1000l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();

        Thread.sleep(1000l);
        
        Parameters.INSTANCE.setProperty(MeMember.ENCRYPTION_PROPERTY, "false");
    }

    @Test
    public void testVolume() throws Exception {
        failed = false;

        for (int i = 0; i < 100; ++i) {
            speaker1.send("VolumeTopic", new MyMessage(MESSAGE + i));
        }

        Thread.sleep(1000l);

        LOG.info("Received " + volumeListener.counter + " messages");

        assertTrue(volumeListener.counter == 100);
    }

    @Test
    public void testMessage() throws Exception {
        failed = false;

        speaker1.send("MyTCPTopic", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        if (failed) {
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

        if (failed) {
            fail();
        }

        speaker2.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

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
