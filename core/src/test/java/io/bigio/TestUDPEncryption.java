/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio;

import io.bigio.core.ClusterService;
import io.bigio.core.member.MeMember;
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
public class TestUDPEncryption {

    private static final Logger LOG = LoggerFactory.getLogger(TestUDPEncryption.class);

    private static final MyMessageListener listener = new MyMessageListener();

    private static final String MESSAGE = "This is a test";

    private static final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);

    private static boolean failed = false;

    private static BigIO speaker1;
    private static BigIO speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        Parameters.INSTANCE.setProperty(ClusterService.PROTOCOL_PROPERTY, "udp");
        Parameters.INSTANCE.setProperty(MeMember.ENCRYPTION_PROPERTY, "true");

        speaker1 = BigIO.bootstrap();
        speaker2 = BigIO.bootstrap();

        speaker2.addListener("MyUDPTopic", listener);
        
        Thread.sleep(1000l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();

        Thread.sleep(1000l);
        
        Parameters.INSTANCE.setProperty(ClusterService.PROTOCOL_PROPERTY, "tcp");
        Parameters.INSTANCE.setProperty(MeMember.ENCRYPTION_PROPERTY, "false");
    }

    @Test
    public void testMessage() throws Exception {
        failed = false;
        
        speaker1.send("MyUDPTopic", new MyMessage(MESSAGE + "1"));
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

        speaker2.send("MyUDPTopic", new MyMessage(MESSAGE + "1"));
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
