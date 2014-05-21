/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.agent;

/**
 *
 * @author atrimble
 */
public class FieldInfo {
    private final String name;
    private final String type;
    private final String signature;

    public FieldInfo(String name, String type, String signature) {
        this.name = name;
        this.type = type;
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " : " + type + " : " + signature;
    }
}
