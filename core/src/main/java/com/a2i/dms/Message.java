/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is the annotation which defines a message.
 * 
 * @author Andy Trimble
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Message {
    
}
