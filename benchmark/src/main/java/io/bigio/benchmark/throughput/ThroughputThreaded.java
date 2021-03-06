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

import io.bigio.BigIO;
import io.bigio.MessageListener;
import io.bigio.Parameters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A threaded throughput benchmark.
 * 
 * @author Andrew Trimble
 */
public class ThroughputThreaded {

    private static final Logger LOG = LoggerFactory.getLogger(ThroughputThreaded.class);
    
    private BigIO bigio;

    private boolean headerPrinted = false;
    
    private final int initialBytes = 16;
    private final int maxBytes = 16384 + 1;

    private final List<ThroughputMessage> messages = new ArrayList<>();
    private final List<Integer> sizes = new ArrayList<>();

    private final List<ProducerListener> producers = new ArrayList<>();

    private int currentMessageIndex = 0;
    private ThroughputMessage currentMessage;

    private static final int THREADS = 8;
    private static final long DURATION = 60000l;
    private final long THROW_AWAY = 1l;

    public ThroughputThreaded() {
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
    }

    public ThroughputThreaded bootstrap() {
        this.bigio = BigIO.bootstrap();
        return this;
    }

    private void printStats() {

        long startTime = Long.MAX_VALUE;
        long endTime = Long.MIN_VALUE;
        int messageCount = 0;
        for(ProducerListener producer : producers) {
            startTime = Math.min(startTime, producer.getStartTime());
            endTime = Math.max(endTime, producer.getEndTime());
            messageCount += 2 * producer.getMessageCount();
        }
        long time = endTime - startTime;
        long seconds = time / 1000;
        long bandwidth = messageCount / seconds;
        double megaMessages = messageCount / 1024.0 / 1024.0;
        double megabits = megaMessages * sizes.get(currentMessageIndex) * 8;
        double mbs = megabits / (double)seconds;
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
                for(int i = 0; i < THREADS; ++i) {
                    ProducerListener producer = new ProducerListener("Consumer" + i);
                    producers.add(producer);
                    bigio.addListener("Producer" + i, producer);
                }   
                
                try {
                    Thread.sleep(2000l);
                } catch(InterruptedException ex) {
                    LOG.error("Interrupted", ex);
                }

                for(ProducerListener producer : producers) {
                    producer.begin();
                }

                final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        for(ProducerListener producer : producers) {
                            producer.end();
                        }

                        try {
                            Thread.sleep(1000l);
                        } catch (InterruptedException ex) {
                            LOG.error("Interrupted", ex);
                        }

                        printStats();

                        ++currentMessageIndex;
                        if(currentMessageIndex < messages.size()) {
                            currentMessage = messages.get(currentMessageIndex);

                            for(ProducerListener producer : producers) {
                                producer.begin();
                            }
                            executor.schedule(this, DURATION, TimeUnit.MILLISECONDS);
                        }
                    }
                }, DURATION, TimeUnit.MILLISECONDS);
                
                break;
            case "consumer":
                LOG.info("Running as a consumer");
                for(int i = 0; i < THREADS; ++i) {
                    bigio.addListener("Consumer" + i, new ConsumerListener("Producer" + i));
                }   break;
        }
    }

    private class ConsumerListener implements MessageListener<ThroughputMessage> {

        private final String producerTopic;

        public ConsumerListener(String producerTopic) {
            this.producerTopic = producerTopic;
        }

        @Override
        public void receive(ThroughputMessage message) {
            try {
                bigio.send(producerTopic, message);
            } catch (Exception ex) {
                LOG.error("Error", ex);
            }
        }
    }

    private class ProducerListener implements MessageListener<ThroughputMessage> {
        private long startTime;
        private long endTime;
        private boolean running = true;
        private int messageCount = 0;
        private boolean warmedUp = false;
        private final String consumerTopic;

        public ProducerListener(final String consumerTopic) {
            this.consumerTopic = consumerTopic;
            running = false;
        }

        public void begin() {
            messageCount = 0;
            warmedUp = false;
            running = true;

            try {
                startTime = System.currentTimeMillis();
                bigio.send(consumerTopic, currentMessage);
            } catch (Exception ex) {
                LOG.error("Could not seed", ex);
            }
        }

        public void end() {
            endTime = System.currentTimeMillis();
            running = false;
        }

        @Override
        public void receive(ThroughputMessage message) {
            if(messageCount >= THROW_AWAY && !warmedUp) {
                warmedUp = true;
                messageCount = 0;
                startTime = System.currentTimeMillis();
            }

            ++messageCount;

            try {
                if(running) {
                    bigio.send(consumerTopic, currentMessage);
                } 
            } catch (Exception ex) {
                LOG.error("Error", ex);
            }
        }

        /**
         * @return the startTime
         */
        public long getStartTime() {
            return startTime;
        }

        /**
         * @return the endTime
         */
        public long getEndTime() {
            return endTime;
        }

        /**
         * @return the messageCount
         */
        public int getMessageCount() {
            return messageCount;
        }
    }
}
