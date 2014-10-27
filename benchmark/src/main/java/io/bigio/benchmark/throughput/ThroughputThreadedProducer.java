/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.benchmark.throughput;

import io.bigio.Parameters;

/**
 *
 * @author atrimble
 */
public class ThroughputThreadedProducer {
    public static void main(String[] args) {
        Parameters.INSTANCE.setProperty("com.a2i.benchmark.role", "producer");
        new ThroughputThreaded().bootstrap().go();
    }
}
