/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.core.codec;

/**
 *
 * @author atrimble
 */
public interface BigIOMessage {
    public void bigiodecode(byte[] bytes);
    public byte[] bigioencode() throws Exception;
}
