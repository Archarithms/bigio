/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.core;

/**
 *
 * @author atrimble
 * @param <T>
 */
public interface MessageListener<T> {
    public void receive(T message);
}
