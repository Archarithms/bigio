/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.benchmark.latency;

import io.bigio.Parameters;

/**
 *
 * @author atrimble
 */
public class LatencyProducer {
    public static void main(String[] args) {
        Parameters.INSTANCE.setProperty("com.a2i.benchmark.role", "producer");
        new Latency().bootstrap().go();
    }
}
