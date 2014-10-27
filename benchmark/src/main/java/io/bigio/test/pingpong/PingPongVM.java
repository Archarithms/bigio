/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.test.pingpong;

import io.bigio.Parameters;

/**
 *
 * @author atrimble
 */
public class PingPongVM {
    public static void main(String[] args) {
        Parameters.INSTANCE.setProperty("com.a2i.benchmark.role", "local");
        new PingPong().bootstrap().go();
    }
}
