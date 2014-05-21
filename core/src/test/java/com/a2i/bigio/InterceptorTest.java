/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.bigio;

import com.a2i.bigio.core.Envelope;
import com.a2i.bigio.core.MessageListener;
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

/**
 *
 * @author atrimble
 */
public class InterceptorTest {
    
    private static Speaker speaker;

    private final BlockingQueue<MyMessage> queue = new ArrayBlockingQueue<>(1);
    private final MyMessageListener listener = new MyMessageListener();
    private final TestInterceptor interceptor = new TestInterceptor();

    @BeforeClass
    public static void init() {
        speaker = Starter.bootstrap();
    }

    @AfterClass
    public static void shutdown() {
        speaker.shutdown();
    }

    @Test
    public void testIntercept() throws Exception {
        speaker.addInterceptor("InterceptTopic", interceptor);
        speaker.addListener("InterceptTopic", listener);

        speaker.send("InterceptTopic", new MyMessage("1"));
        MyMessage m = queue.poll(1000l, TimeUnit.MILLISECONDS);
        assertNull(m);

        m = queue.poll(2500l, TimeUnit.MILLISECONDS);
        assertNotNull(m);
        assertEquals(m.getMessage(), "1");
    }

    private class TestInterceptor implements Interceptor {
        @Override
        public Envelope intercept(Envelope envelope) {
            envelope.setExecuteTime(envelope.getExecuteTime() + 2000);
            return envelope;
        }
    }
    
    private class MyMessageListener implements MessageListener<MyMessage> {

        @Override
        public void receive(MyMessage message) {
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
