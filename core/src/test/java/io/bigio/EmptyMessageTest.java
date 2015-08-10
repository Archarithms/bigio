/*
 * Copyright 2014 Archarithms Inc.
 */
package io.bigio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
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
public class EmptyMessageTest {

    private static final Logger LOG = LoggerFactory.getLogger(EmptyMessageTest.class);

    private static final MyMessageListener listener = new MyMessageListener();

    private static final BlockingQueue<EmptyMessage> queue = new ArrayBlockingQueue<>(1);

    private static boolean failed = false;

    private static Speaker speaker1;
    private static Speaker speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        speaker1 = Starter.bootstrap();
        speaker2 = Starter.bootstrap();

        speaker2.addListener("EmptyTopic", listener);

        Thread.sleep(2000l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();

        Thread.sleep(1000l);
    }

    @Test
    public void testMessage() throws Exception {
        failed = false;

        speaker1.send("EmptyTopic", new EmptyMessage());
        EmptyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);

        if (failed) {
            fail();
        }

        speaker1.send("BadTopic", new EmptyMessage());
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        queue.clear();
    }

    private static class MyMessageListener implements MessageListener<EmptyMessage> {

        @Override
        public void receive(EmptyMessage message) {
            boolean success = queue.offer(message);

            if (!success) {
                failed = true;
            }
        }
    }
}
