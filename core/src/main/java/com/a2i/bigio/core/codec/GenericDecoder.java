/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.core.codec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class GenericDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(GenericDecoder.class);

    private static final Map<Class, Method> METHODS = new HashMap<>();
    
    public static Object decode(String className, byte[] bytes) throws IOException {

        try {
            Class clazz = Class.forName(className);

            if(clazz.getAnnotation(com.a2i.bigio.Message.class) != null) {
                try {
                    Method method = METHODS.get(clazz);

                    if(method == null) {
                        method = clazz.getMethod("_decode_", byte[].class);
                        METHODS.put(clazz, method);
                    }
                    
                    Object obj = clazz.newInstance();
                    method.invoke(obj, bytes);
                    return obj;
                } catch (NoSuchMethodException ex) {
                    LOG.error("Cannot find encoding method.", ex);
                } catch (SecurityException ex) {
                    LOG.error("Security exception.", ex);
                } catch (IllegalAccessException ex) {
                    LOG.error("Illegal method access.", ex);
                } catch (InvocationTargetException ex) {
                    LOG.error("Invocation exception.", ex);
                } catch (InstantiationException ex) {
                    LOG.error("Cannot create new message.", ex);
                }
            }
        } catch(ClassNotFoundException ex) {
            LOG.error("Cannot find message type", ex);
        }

        return null;
    }
}
