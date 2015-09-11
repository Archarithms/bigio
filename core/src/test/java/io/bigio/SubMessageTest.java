/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class SubMessageTest {

    private static final Logger LOG = LoggerFactory.getLogger(TestRemoteMessagesUDP.class);

    private static final MyMessageListener listener = new MyMessageListener();
    private static final FailMessageListener failListener = new FailMessageListener();

    private static final String MESSAGE = "This is a test";

    private static final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);
    private static final BlockingQueue<FailMessage> failQueue = new ArrayBlockingQueue<>(1);

    private static boolean failed = false;

    private static BigIO speaker1;
    private static BigIO speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        System.setProperty("com.a2i.protocol", "udp");

        speaker1 = BigIO.bootstrap();
        speaker2 = BigIO.bootstrap();

        speaker2.addListener("MyUDPTopic", listener);
        speaker2.addListener("FAIL", failListener);
        
        Thread.sleep(1000l);
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();

        Thread.sleep(1000l);
        
        System.setProperty("com.a2i.protocol", "tcp");
    }


    // This test works
    @Test
    public void testMessage() throws Exception {
        failed = false;

        List<List<Double>> doubles = new ArrayList<>();

        for (int i = 0; i < 6; ++i) {
            List<Double> value = new ArrayList<>();
            for (int j = 0; j < 6; ++j) {
                value.add(Math.random());
            }
            doubles.add(value);
        }

        speaker1.send("MyUDPTopic", new MyMessage(MESSAGE + "1", doubles));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        List<List<Double>> received = m.cov.a;
        for (int i = 0; i < 6; ++i) {
            List<Double> value = doubles.get(i);
            List<Double> receivedValue = received.get(i);
            for (int j = 0; j < 6; ++j) {
                LOG.info("Testing: " + i + " " + j + ": " + value.get(j) + " == " + receivedValue.get(j));
                assertEquals(value.get(j), receivedValue.get(j), 0.0001);
            }
        }

    }

    // This test fails
    @Test
    public void testFailMessage() throws Exception {
        failed = false;

        List<List<Double>> doubles = new ArrayList<>();

        for (int i = 0; i < 6; ++i) {
            List<Double> value = new ArrayList<>();
            for (int j = 0; j < 6; ++j) {
                value.add(Math.random());
            }
            doubles.add(value);
        }

        speaker1.send("FAIL", new FailMessage(MESSAGE + "2", doubles));
        FailMessage m = failQueue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "2");

        List<List<Double>> received = m.cov.a;
        for (int i = 0; i < 6; ++i) {
            List<Double> value = doubles.get(i);
            List<Double> receivedValue = received.get(i);
            for (int j = 0; j < 6; ++j) {
                assertEquals(value.get(j), receivedValue.get(j), 0.0001);
            }
        }
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

    private static class FailMessageListener implements MessageListener<FailMessage> {

        @Override
        public void receive(FailMessage message) {
            LOG.info("Got a message " + message.getMessage());

            boolean success = failQueue.offer(message);

            if (!success) {
                failed = true;
            }
        }
    }

    @Message
    public static final class MyMessage {

        private String message;

        public SubMessage2DArray cov = new SubMessage2DArray();

        public MyMessage() {

        }

        public MyMessage(String message, List<List<Double>> doubles) {
            this.message = message;
            this.cov.instantiate();
            for (int i = 0; i < doubles.size(); ++i) {
                List<Double> source = doubles.get(i);
                for (int j = 0; j < source.size(); ++j) {
                    this.cov.a.get(i).set(j, source.get(j));
                }

            }
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Message
    public static final class FailMessage {

        private String message;

        public FailMessageArray cov = new FailMessageArray();

        public FailMessage() {

        }

        public FailMessage(String message, List<List<Double>> doubles) {
            this.message = message;
            this.cov.instantiate();
            for (int i = 0; i < doubles.size(); ++i) {
                List<Double> source = doubles.get(i);
                for (int j = 0; j < source.size(); ++j) {
                    this.cov.a.get(i).set(j, source.get(j));
                }

            }
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Message
    public static class FailMessageArray {

        public List<List<Double>> a = new ArrayList<>(6);

        public FailMessageArray() {
            instantiate();
        }

        public void instantiate() {
            for (int i = 0; i < 6; ++i) {
                List<Double> l = new ArrayList<>(6);
                for (int j = 0; j < 6; ++j) {
                    l.add(0.0);
                }
                a.add(l);
            }
        }
    }

    @Message
    public static class SubMessage2DArray {

        public List<List<Double>> a = new ArrayList<>(6);

        public SubMessage2DArray() {

        }

        public void instantiate() {
            for (int i = 0; i < 6; ++i) {
                List<Double> l = new ArrayList<>(6);
                for (int j = 0; j < 6; ++j) {
                    l.add(0.0);
                }
                a.add(l);
            }
        }
    }

    private static void print(List<List<Double>> arr) {
        for(List<Double> l : arr) {
            StringBuilder buff = new StringBuilder();

            for(Double d : l) {
                buff.append(d).append(" ");
            }

            LOG.info(buff.toString());
        }
    }
}
