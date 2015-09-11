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
package io.bigio.benchmark.latency;

import io.bigio.BigIO;
import io.bigio.Component;
import io.bigio.Inject;
import io.bigio.MessageListener;
import io.bigio.Parameters;
import io.bigio.core.codec.GenericCodec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A benchmark class for testing latencies.
 *
 * @author Andrew Trimble
 */
@Component
public class Latency {

    private static final Logger LOG = LoggerFactory.getLogger(Latency.class);

    @Inject
    private BigIO bigio;

    private boolean running = true;
    private boolean headerPrinted = false;
    private int messageCount = 0;
    private final int throwAway = 100;
    private final int sampleSize = 10000;

    private final List<Long> latencies = new ArrayList<>(sampleSize);

    private final long clockOverhead;

    private int currentMessageIndex = 0;
    private final int initialBytes = 16;
    private final int maxBytes = 16384 + 1;

    private final List<LatencyMessage> messages = new ArrayList<>();

    private LatencyMessage currentMessage;

    private boolean seeded = false;

    Thread injectThread = new Thread() {
        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while (!seeded && running) {
                try {
                    Thread.sleep(1000l);
                    LOG.info("Seeding");
                    currentMessage.sendTime = System.nanoTime();
                    bigio.send("HelloWorldConsumer", currentMessage);
                } catch (InterruptedException | IOException ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    public Latency() {

        int currentBytes = initialBytes;
        while (currentBytes < maxBytes) {
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < currentBytes - 12; ++i) {
                padding.append('a');
            }
            if (currentBytes < 64) {
                padding.append("aa");
            }
            LatencyMessage m = new LatencyMessage();
            m.padding = padding.toString();
            messages.add(m);
            currentBytes = currentBytes * 2;
        }

        for (int i = 0; i < sampleSize; ++i) {
            latencies.add(0l);
        }
        latencies.clear();

        currentMessageIndex = 0;
        currentMessage = messages.get(currentMessageIndex);

        long startTime;
        long finishTime = 0;

        startTime = System.nanoTime();
        for (int i = 0; i < 16; ++i) {
            finishTime = System.nanoTime();
        }
        clockOverhead = (finishTime - startTime) / 16;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                running = false;

                try {
                    injectThread.join();
                } catch (InterruptedException ex) {
                    LOG.error("Inject thread interrupted.", ex);
                }
            }
        });
    }

    public Latency bootstrap() {
        this.bigio = BigIO.bootstrap();
        return this;
    }

    private void printStats() {
        Collections.sort(latencies);

        long sum = 0;
        for (long l : latencies) {
            sum += l;
        }
        int currentSampleSize = latencies.size();
        double average = (double) sum / currentSampleSize;
        double deviationSum = 0;
        for (long l : latencies) {
            deviationSum += (l - average) * (l - average);
        }
        double deviation = deviationSum / (sum / currentSampleSize - 1);
        deviation = Math.sqrt(deviation);

        int percentile_50_sample_index = (50 * currentSampleSize) / 100;
        int percentile_90_sample_index = (90 * currentSampleSize) / 100;
        int percentile_99_sample_index = (99 * currentSampleSize) / 100;
        int percentile_9999_sample_index
                = (9999 * currentSampleSize) / 10000;

        average /= 1000.0; //convert to usec
        deviation /= 1000.0; //convert to usec
        double min_sample = (double) latencies.
                get(0) / 1000.0;
        double max_sample = (double) latencies.
                get(currentSampleSize - 1) / 1000.0;
        double percentile_50_sample = (double) latencies.
                get(percentile_50_sample_index) / 1000.0;
        double percentile_90_sample = (double) latencies.
                get(percentile_90_sample_index) / 1000.0;
        double percentile_99_sample = (double) latencies.
                get(percentile_99_sample_index) / 1000.0;
        double percentile_9999_sample = (double) latencies.
                get(percentile_9999_sample_index) / 1000.0;

        int length = 0;

        try {
            length = GenericCodec.encode(currentMessage).length;
        } catch (IOException ex) {

        }

        if (!headerPrinted) {
            System.out.println(
                    "\nbytes , stdev us ,  ave us  ,  min us  ,  50%% us ,  90%% us ,  99%% us , 99.99%%  ,  max us  ,  samples\n"
                    + "------,----------,----------,----------,----------,----------,----------,----------,----------,---------");
            headerPrinted = true;
        }
        System.out.format("%6d,%10.1f,%10.1f,%10.1f,%10.1f,%10.1f,%10.1f,%10.1f,%10.1f,%6d\n",
                length,
                deviation,
                average,
                min_sample,
                percentile_50_sample,
                percentile_90_sample,
                percentile_99_sample,
                percentile_9999_sample,
                max_sample,
                currentSampleSize);
    }

    @PostConstruct
    public void go() {
        LOG.info("Going");
        String role = Parameters.INSTANCE.getProperty("com.a2i.benchmark.role", "local");

        switch (role) {
            case "producer":
                setupProducer();
                break;
            case "consumer":
                setupConsumer();
                break;
            case "local":
            default:
                setupProducer();
                setupConsumer();
                break;
        }
    }

    private void setupProducer() {
        LOG.info("Running as a producer");
        bigio.addListener("HelloWorldProducer", new MessageListener<LatencyMessage>() {
            long lat = 0;

            @Override
            public void receive(LatencyMessage message) {
                try {
                    seeded = true;
                    bigio.send("HelloWorldConsumer", message);
                } catch (Exception ex) {
                    LOG.error("Error", ex);
                }
            }
        });
        injectThread.start();
    }

    private void setupConsumer() {
        LOG.info("Running as a consumer");
        bigio.addListener("HelloWorldConsumer", new MessageListener<LatencyMessage>() {
            long lat = 0;

            @Override
            public void receive(LatencyMessage message) {

                lat = System.nanoTime() - message.sendTime - clockOverhead;
                if (messageCount > throwAway) {

                            // Some weird bug that gives us a bogus latency
                    // It's too big to be reasonable, so throw it out.
                    if (lat < 1e15) {
                        latencies.add(lat);
                    } else {
                        --messageCount;
                    }
                }

                ++messageCount;

                if (messageCount > sampleSize + throwAway) {
                    printStats();
                    messageCount = 0;
                    latencies.clear();

                    ++currentMessageIndex;
                    if (currentMessageIndex == messages.size()) {
                        System.exit(0);
                    } else {
                        currentMessage = messages.get(currentMessageIndex);
                    }
                }

                try {
                    if (running) {
                        currentMessage.sendTime = System.nanoTime();
                        bigio.send("HelloWorldProducer", currentMessage);
                    }
                } catch (Exception ex) {
                    LOG.error("Error", ex);
                }
            }
        });
    }
}
