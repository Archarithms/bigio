/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.core;

/**
 *
 * @author atrimble
 * @param <T>
 */
public interface MessageListener<T> {
    public void receive(T message);
}
