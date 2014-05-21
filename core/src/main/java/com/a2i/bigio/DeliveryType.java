/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio;

/**
 * An enumeration of the various types of deliveries.
 * 
 * @author Andy Trimble
 */
public enum DeliveryType {
    /**
     * Broadcast messages to all known receives.
     */
    BROADCAST, 

    /**
     * Send messages to the set of known receives in a round-robin manner.
     */
    ROUND_ROBIN, 

    /**
     * Send messages to randomly selected receivers.
     */
    RANDOM;
}
