/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.test;

import com.a2i.sim.Parameters;
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
public class PingPong {

    private static final Logger LOG = LoggerFactory.getLogger(PingPong.class);
    
    @Autowired
    private Speaker speaker;

    private boolean running = true;
    private long time;
    private long messageCount = 0;
    private long sendCount = 0;

    Thread injectThread = new Thread() {
        @Override
        public void run() {
            while(running) {
                try {
                    Thread.sleep(1000l);
                    speaker.send("HelloWorldConsumer", new SimpleMessage("This message should be en/decoded", ++sendCount));
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    Thread localThread = new Thread() {
        @Override
        public void run() {
            time = System.currentTimeMillis();
            while(running) {
                try {
                    speaker.send("HelloWorldLocal", new SimpleMessage("This message should be en/decoded", ++sendCount));
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    public PingPong() {
        SimpleMessage m = new SimpleMessage("This message should be en/decoded", 0);
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
                    injectThread.join();
                    localThread.join();
                } catch(InterruptedException ex) {
                    ex.printStackTrace();
                }

                time = System.currentTimeMillis() - time;

                long count = messageCount;
                long send = sendCount;

                long seconds = time / 1000;
                long bandwidth = count / seconds;
                long sendBandwidth = send / seconds;

                LOG.info("Received " + count + " messages in " + seconds + 
                        " seconds for a bandwidth of " + bandwidth + " m/s");

                LOG.info("Sent " + send + " messages in " + seconds + 
                        " seconds for a bandwidth of " + sendBandwidth + " m/s");
            }
        });
    }

    @PostConstruct
    public void go() {
        String role = Parameters.INSTANCE.getProperty("com.a2i.benchmark.role", "local");

        if(role.equals("producer")) {
            LOG.info("Running as a producer");
            speaker.addListener("HelloWorldProducer", new MessageListener<SimpleMessage>() {
                @Override
                public void receive(SimpleMessage message) {
                    if(messageCount == 0) {
                        time = System.currentTimeMillis();
                    }
                    
                    ++messageCount;
                    try {
                        if(running) {
                            speaker.send("HelloWorldConsumer", new SimpleMessage("Hello from the producer", ++sendCount));
                        }
                    } catch (Exception ex) {
                        LOG.error("Error", ex);
                    }
                }
            });
            injectThread.start();
        } else if(role.equals("consumer")) {
            LOG.info("Running as a consumer");
            speaker.addListener("HelloWorldConsumer", new MessageListener<SimpleMessage>() {
                @Override
                public void receive(SimpleMessage message) {
                    if(messageCount == 0) {
                        time = System.currentTimeMillis();
                    }
                    
                    ++messageCount;
                    try {
                        if(running) {
                            speaker.send("HelloWorldProducer", new SimpleMessage("Hello from the consumer", ++sendCount));
                        }
                    } catch (Exception ex) {
                        LOG.error("Error", ex);
                    }
                }
            });
        } else {
            LOG.info("Running in VM only");
            speaker.addListener("HelloWorldLocal", new MessageListener<SimpleMessage>() {
                @Override
                public void receive(SimpleMessage message) {
                    ++messageCount;
                }
            });

            localThread.start();
        }
    }
}
