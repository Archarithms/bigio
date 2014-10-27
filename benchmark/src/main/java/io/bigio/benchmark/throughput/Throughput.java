/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio.benchmark.throughput;

import io.bigio.MessageListener;
import io.bigio.Parameters;
import io.bigio.Speaker;
import io.bigio.Starter;
import java.util.ArrayList;
import java.util.List;
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

    private final List<ThroughputMessage> messages = new ArrayList<>();
    private final List<Integer> sizes = new ArrayList<>();

    private ThroughputMessage currentMessage;

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
            ThroughputMessage m = new ThroughputMessage();
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
                    LOG.error("Thread interrupted.", ex);
                }
            }
        });
    }

    public Throughput bootstrap() {
        this.speaker = Starter.bootstrap();
        return this;
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

        switch (role) {
            case "producer":
                LOG.info("Running as a producer");
                speaker.addListener("HelloWorldProducer", new MessageListener<ThroughputMessage>() {
                    @Override
                    public void receive(ThroughputMessage message) {
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
                }); seedThread.start();
                break;
            case "consumer":
                LOG.info("Running as a consumer");
                speaker.addListener("HelloWorldConsumer", new MessageListener<ThroughputMessage>() {
                    @Override
                    public void receive(ThroughputMessage message) {
                        try {
                            if(running) {
                                speaker.send("HelloWorldProducer", message);
                            }
                        } catch (Exception ex) {
                            LOG.error("Error", ex);
                        }
                    }
                }); break;
        }
    }

    public static void main(String[] args) {
        new Throughput().go();
    }
}
