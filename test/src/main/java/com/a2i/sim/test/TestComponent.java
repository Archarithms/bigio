/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.test;

import com.a2i.sim.Speaker;
import com.a2i.sim.core.Envelope;
import com.a2i.sim.core.MessageListener;
import com.a2i.sim.core.TimeUtil;
import com.a2i.sim.core.codec.EnvelopeEncoder;
import com.a2i.sim.core.codec.GenericEncoder;
import java.io.IOException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class TestComponent {

    private static final Logger LOG = LoggerFactory.getLogger(TestComponent.class);
    
    @Autowired
    private Speaker speaker;

    private boolean running = true;
    private long time;
    private long messageCount = 0;

    Thread senderThread = new Thread() {
        @Override
        public void run() {
            time = System.currentTimeMillis();
            while(running) {
                try {
                    Thread.sleep(1000l);
                    speaker.send("HelloWorld", new SimpleMessage("This message should be en/decoded"));
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    public TestComponent() {
        SimpleMessage m = new SimpleMessage("This message should be en/decoded");
        try {
            byte[] payload = GenericEncoder.encode(m);
            Envelope envelope = new Envelope();
            envelope.setDecoded(false);
            envelope.setExecuteTime(0);
            envelope.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
            envelope.setSenderKey("192.168.1.1:55200:55200");
            envelope.setTopic("HelloWorld");
            envelope.setClassName(SimpleMessage.class.getName());
            envelope.setPayload(payload);
            envelope.setDecoded(false);

            byte[] bytes = EnvelopeEncoder.encode(envelope);
            LOG.info("Typical message size: " + bytes.length);
            LOG.info("Typical payload size: " + payload.length);
            LOG.info("Typical header size: " + (bytes.length - payload.length));
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                running = false;
                
                try {
                    senderThread.join();
                } catch(InterruptedException ex) {
                    ex.printStackTrace();
                }

                time = System.currentTimeMillis() - time;

                long count = messageCount;

                long seconds = time / 1000;
                long bandwidth = count / seconds;

                LOG.info("Received " + count + " messages in " + seconds + 
                        " seconds for a bandwidth of " + bandwidth + " m/s");
            }
        });
    }

    @PostConstruct
    public void go() {
        LOG.info("Adding listener");

        speaker.addListener("HelloWorld", new MessageListener<SimpleMessage>() {
            @Override
            public void receive(SimpleMessage message) {
                ++messageCount;
                LOG.debug("Woo Hoo!!! Got a message. '" + message.getString() + "'");
            }
        });

        senderThread.start();
    }
}
