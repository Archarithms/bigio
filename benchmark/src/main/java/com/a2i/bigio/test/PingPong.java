/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.bigio.test;

import com.a2i.bigio.Component;
import com.a2i.bigio.Inject;
import com.a2i.bigio.Parameters;
import com.a2i.bigio.Speaker;
import com.a2i.bigio.core.Envelope;
import com.a2i.bigio.core.MessageListener;
import com.a2i.bigio.core.codec.EnvelopeEncoder;
import com.a2i.bigio.core.codec.GenericEncoder;
import com.a2i.bigio.util.TimeUtil;
import java.io.IOException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class PingPong {

    private static final Logger LOG = LoggerFactory.getLogger(PingPong.class);
    
    @Inject
    private Speaker speaker;

    private boolean running = true;
    private long time;
    private long messageCount = 0;
    private final long throwAway = 2000000l;
    private final long maxMessages = 1000000000 + throwAway;
    private boolean warmedUp = false;

    private final SimpleMessage m = new SimpleMessage("m", 0, 0);

    private int averageSize = 0;

    private long usageTotal = 0;
    private long usageSamples = 0;

    Thread injectThread = new Thread() {
        @Override
        public void run() {
            while(running) {
                try {
                    Thread.sleep(1000l);
                    speaker.send("HelloWorldConsumer", m);
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
                    speaker.send("HelloWorldLocal", m);
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    Thread memThread = new Thread() {
        Runtime runtime = Runtime.getRuntime();

        @Override
        public void run() {
            while(running) {
                ++usageSamples;
                usageTotal += runtime.totalMemory() - runtime.freeMemory();

                try {
                    Thread.sleep(100l);
                } catch(InterruptedException ex) {

                }
            }
        }
    };

    public PingPong() {
        SimpleMessage m = new SimpleMessage("m", 0, 0);
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

            averageSize = bytes.length;
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                running = false;
                
                time = System.currentTimeMillis() - time;
                
                try {
                    injectThread.join();
                    localThread.join();
                    memThread.join();
                } catch(InterruptedException ex) {
                    ex.printStackTrace();
                }

                long count = messageCount;

                long seconds = time / 1000;
                long bandwidth = count / seconds;

                LOG.info("Received " + count + " messages in " + seconds + 
                        " seconds for a bandwidth of " + bandwidth + " m/s");

                LOG.info((count * averageSize / 1024 / 1024 * 8) / seconds + " Mbit/s");
                
                double avgMemUsage = (double)usageTotal / (double)usageSamples;
                
                LOG.info("Average mem usage: " + (avgMemUsage / 1024 / 1024) + " MB");
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
                    if(messageCount >= throwAway && !warmedUp) {
                        LOG.info("Reached the warm-up threshold: resetting stats");
                        warmedUp = true;
                        messageCount = 0;
                        memThread.start();
                        time = System.currentTimeMillis();
                    }

                    ++messageCount;

                    if(messageCount > maxMessages) {
                        running = false;
                        LOG.info("Finished");
                    }

                    try {
                        if(running) {
                            speaker.send("HelloWorldConsumer", m);
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
                    if(messageCount >= throwAway && !warmedUp) {
                        LOG.info("Reached the warm-up threshold: resetting stats");
                        warmedUp = true;
                        messageCount = 0;
                        memThread.start();
                        time = System.currentTimeMillis();
                    }
                    
                    ++messageCount;

                    if(messageCount > maxMessages) {
                        running = false;
                        LOG.info("Finished");
                    }

                    try {
                        if(running) {
                            speaker.send("HelloWorldProducer", m);
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
                    if(messageCount >= throwAway && !warmedUp) {
                        LOG.info("Reached the warm-up threshold: resetting stats");
                        warmedUp = true;
                        messageCount = 0;
                        memThread.start();
                        time = System.currentTimeMillis();
                    }

                    ++messageCount;

                    if(messageCount > maxMessages) {
                        running = false;
                        LOG.info("Finished");
                    }
                }
            });

            System.gc();

            try {
                Thread.sleep(1000l);
            } catch(InterruptedException ex) {}

            localThread.start();
        }
    }

    public static void main(String[] args) {
        new PingPong().go();
    }
}
