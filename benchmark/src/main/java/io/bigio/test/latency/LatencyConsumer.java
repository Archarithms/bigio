/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.test.latency;

import io.bigio.Parameters;

/**
 *
 * @author atrimble
 */
public class LatencyConsumer {
    public static void main(String[] args) {
        Parameters.INSTANCE.setProperty("com.a2i.benchmark.role", "consumer");
        new Latency().bootstrap().go();
    }
}
