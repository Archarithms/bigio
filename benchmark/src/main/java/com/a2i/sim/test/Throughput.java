/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.test;

import com.a2i.sim.Parameters;
import com.a2i.sim.Speaker;
import com.a2i.sim.Starter;
import com.a2i.sim.core.MessageListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class Throughput {

    private static final Logger LOG = LoggerFactory.getLogger(Throughput.class);
    
    private Speaker speaker;

    private boolean running = true;
    private boolean headerPrinted = false;
    private long startTime;
    private long endTime;
    private long messageCount = 0;
    private final long throwAway = 100000l;
    private final long sampleSize = 1500000l;
    private boolean warmedUp = false;

    private int currentMessageIndex = 0;
    private final int initialBytes = 16;
    private final int maxBytes = 16384 + 1;

    private final List<LatencyMessage> messages = new ArrayList<>();
    private final List<Integer> sizes = new ArrayList<>();

    private LatencyMessage currentMessage;

    private boolean seeded = false;

    Thread seedThread = new Thread() {
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while(!seeded && running) {
                try {
                    Thread.sleep(100l);
                    //LOG.info("Seeding");
                    currentMessage.sendTime = System.nanoTime();
                    speaker.send("HelloWorldConsumer", currentMessage);
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    public Throughput() {
        this.speaker = Starter.bootstrap();
        
        int currentBytes = initialBytes;
        while(currentBytes < maxBytes) {
            StringBuilder padding = new StringBuilder();
            for(int i = 0; i < currentBytes - 12; ++i) {
                padding.append('a');
            }
            if(currentBytes < 64) {
                padding.append("aa");
            }
            LatencyMessage m = new LatencyMessage();
            m.padding = padding.toString();
            m.sendTime = System.nanoTime();
            messages.add(m);
            sizes.add(currentBytes);
            currentBytes = currentBytes * 2;
        }
        currentMessage = messages.get(0);
        currentMessageIndex = 0;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                running = false;
                
                endTime = System.currentTimeMillis();
                printStats();
                
                try {
                    seedThread.join();
                } catch(InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void printStats() {

        long time = endTime - startTime;
        long seconds = time / 1000;
        long bandwidth = messageCount / seconds;
        double mbs = (messageCount * sizes.get(currentMessageIndex) * 8 / 1024 / 1024) / seconds;

        if(!headerPrinted) {
            System.out.println(
                    "\n" + 
            "bytes , messages , duration ,messages/s,    Mb/s  \n" +
            "------,----------,----------,----------,------------");
            headerPrinted = true;
        }
        System.out.format("%6d,%10d,%10d,%10d,%12.1f\n",
                        sizes.get(currentMessageIndex),
                        messageCount,
                        seconds,
                        bandwidth,
                        mbs);
    }

    public void go() {
        String role = Parameters.INSTANCE.getProperty("com.a2i.benchmark.role", "local");

        if(role.equals("producer")) {
            LOG.info("Running as a producer");
            speaker.addListener("HelloWorldProducer", new MessageListener<LatencyMessage>() {
                @Override
                public void receive(LatencyMessage message) {
//                    if(!seeded) {
//                        seeded = true;
//                        LOG.info("Beginning test");
//                    }

                    if(messageCount >= throwAway && !warmedUp) {
                        warmedUp = true;
                        messageCount = 0;
                        startTime = System.currentTimeMillis();
                    }

                    ++messageCount;
                    
                    if(messageCount > sampleSize) {
                        endTime = System.currentTimeMillis();
                        
                        printStats();
                        messageCount = 0;
                        warmedUp = false;

                        ++currentMessageIndex;
                        if(currentMessageIndex == messages.size()) {
                            System.exit(0);
                        } else {
                            currentMessage = messages.get(currentMessageIndex);
                        }
                    }

                    try {
                        if(running) {
                            speaker.send("HelloWorldConsumer", currentMessage);
                        }
                    } catch (Exception ex) {
                        LOG.error("Error", ex);
                    }
                }
            });
            seedThread.start();
        } else if(role.equals("consumer")) {
            LOG.info("Running as a consumer");
            speaker.addListener("HelloWorldConsumer", new MessageListener<LatencyMessage>() {
                @Override
                public void receive(LatencyMessage message) {
                    try {
                        if(running) {
                            speaker.send("HelloWorldProducer", message);
                        }
                    } catch (Exception ex) {
                        LOG.error("Error", ex);
                    }
                }
            });
        }
    }

    public static void main(String[] args) {
        new Throughput().go();
    }
}
