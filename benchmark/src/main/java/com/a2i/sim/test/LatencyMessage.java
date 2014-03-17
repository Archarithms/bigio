/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.test;

import com.a2i.sim.Message;

/**
 *
 * @author atrimble
 */
@Message
public class LatencyMessage {
    public long sendTime = 0;
    public String padding = "a";
}
