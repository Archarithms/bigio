/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.sim;

import com.a2i.sim.core.MessageListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author atrimble
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class MessageTest {

    private static final Logger LOG = LoggerFactory.getLogger(MessageTest.class);

    @Autowired
    private Speaker speaker;

    private static final String MESSAGE = "This is a test";

    private final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);

    private final MyMessageListener listener = new MyMessageListener();
    private final DelayedMessageListener delayedListener = new DelayedMessageListener();

    @Test
    public void testMessage() throws Exception {
        speaker.addListener("MyTopic", listener);
        speaker.send("MyTopic", new MyMessage(MESSAGE + "1"));
        MyMessage m = queue.poll(2000l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");

        speaker.send("BadTopic", new MyMessage(MESSAGE + "2"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);

        speaker.removeListener(listener);
        speaker.send("MyTopic", new MyMessage(MESSAGE + "3"));
        m = queue.poll(500l, TimeUnit.MILLISECONDS);
        assertNull(m);
    }

    @Test
    public void testDelay() throws Exception {
        speaker.addListener("DelayedTopic", delayedListener);
        speaker.send("DelayedTopic", new MyMessage(MESSAGE + "1"), 2000);
        MyMessage m = queue.poll(1000l, TimeUnit.MILLISECONDS);
        assertNull(m);

        m = queue.poll(2500l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), MESSAGE + "1");
    }

    private class MyMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a message " + message.getMessage());
            
            boolean success = queue.offer(message);

            if (!success) {
                fail();
            }
        }
    }

    private class DelayedMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
            LOG.info("Got a message " + message.getMessage());
            
            boolean success = queue.offer(message);

            if (!success) {
                fail();
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
