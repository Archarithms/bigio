/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.benchmark.throughput;

import io.bigio.Parameters;

/**
 *
 * @author atrimble
 */
public class ThroughputThreadedConsumer {
    public static void main(String[] args) {
        Parameters.INSTANCE.setProperty("com.a2i.benchmark.role", "consumer");
        new ThroughputThreaded().bootstrap().go();
    }
}
