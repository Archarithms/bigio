/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.dms.test;

import com.a2i.dms.Inject;
import com.a2i.dms.Parameters;
import com.a2i.dms.Speaker;
import com.a2i.dms.core.Envelope;
import com.a2i.dms.core.MessageListener;
import com.a2i.dms.core.codec.EnvelopeEncoder;
import com.a2i.dms.core.codec.GenericEncoder;
import com.a2i.dms.util.TimeUtil;
import java.io.IOException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
//@Component
public class BenchmarkComponent {

    private static final Logger LOG = LoggerFactory.getLogger(BenchmarkComponent.class);
    
    @Inject
    private Speaker speaker;

    private boolean running = true;
    private long time;
    private long messageCount = 0;
    private long sendCount = 0;

    private int messagesPerSecond = 100000;

    Thread senderThread = new Thread() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000l);
            } catch(Exception ex) {

            }

            time = System.currentTimeMillis();
            while(running) {
                try {
                    for(int i = 0; i < messagesPerSecond; ++i) {
                        speaker.send("HelloWorld", new SimpleMessage("This message should be en/decoded", ++sendCount, System.currentTimeMillis()));
                    }
                    Thread.sleep(1000l);
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
//                    Thread.sleep(100l);
                    speaker.send("HelloWorldLocal", new SimpleMessage("This message should be en/decoded", ++sendCount, System.currentTimeMillis()));
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    public BenchmarkComponent() {
        SimpleMessage m = new SimpleMessage("This message should be en/decoded", 0, System.currentTimeMillis());
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
            senderThread.start();
        } else if(role.equals("consumer")) {
            LOG.info("Running as a consumer");
            speaker.addListener("HelloWorld", new MessageListener<SimpleMessage>() {
                long lastReceived = 0;
                @Override
                public void receive(SimpleMessage message) {
                    if(messageCount == 0) {
                        time = System.currentTimeMillis();
                    } else {
                        if(message.getSequence() - lastReceived > 1) {
                            LOG.info("Dropped " + (message.getSequence() - lastReceived) + " messages");
                        }
                        lastReceived = message.getSequence();
                    }
                    ++messageCount;
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
