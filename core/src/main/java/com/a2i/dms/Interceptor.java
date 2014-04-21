/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms;

import com.a2i.dms.core.Envelope;

/**
 * This interface defines a message interceptor. Interceptors are used to 
 * intercept messages before they are sent to consumers. The messages may be
 * transformed by an interceptor.
 * 
 * @author Andy Trimble
 */
public interface Interceptor {

    /**
     * Intercept a message.
     * 
     * @param envelope the message.
     * @return a transformation of the received message.
     */
    public Envelope intercept(Envelope envelope);
}
