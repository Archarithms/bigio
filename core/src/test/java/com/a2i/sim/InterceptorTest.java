/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim;

import com.a2i.sim.core.Envelope;
import com.a2i.sim.core.MessageListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author atrimble
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class InterceptorTest {
    
    @Autowired
    private Speaker speaker;

    private final BlockingQueue<TestMessage> queue = new ArrayBlockingQueue<>(1);
    private final TestMessageListener listener = new TestMessageListener();
    private final TestInterceptor interceptor = new TestInterceptor();

    @Test
    public void testIntercept() throws Exception {
        speaker.addInterceptor("InterceptTopic", interceptor);
        speaker.addListener("InterceptTopic", listener);

        speaker.send("InterceptTopic", new TestMessage("1"));
        TestMessage m = queue.poll(1000l, TimeUnit.MILLISECONDS);
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
    
    private class TestMessageListener implements MessageListener<TestMessage> {

        @Override
        public void receive(TestMessage message) {
            boolean success = queue.offer(message);

            if (!success) {
                fail();
            }
        }
    }

    private static final class TestMessage {

        private String message;

        public TestMessage() {

        }

        public TestMessage(String message) {
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
