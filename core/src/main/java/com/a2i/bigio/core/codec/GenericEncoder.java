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
public class GenericEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(GenericEncoder.class);

    private static final Map<Class, Method> METHODS = new HashMap<>();

    public static byte[] encode(Object message) throws IOException {

        if(message.getClass().getAnnotation(com.a2i.bigio.Message.class) != null) {
            try {
                Method method = METHODS.get(message.getClass());

                if(method == null) {
                    method = message.getClass().getMethod("_encode_");
                    METHODS.put(message.getClass(), method);
                }

                byte[] ret = (byte[])method.invoke(message);
                return ret;
            } catch (NoSuchMethodException ex) {
                LOG.error("Cannot find encoding method.", ex);
            } catch (SecurityException ex) {
                LOG.error("Security exception.", ex);
            } catch (IllegalAccessException ex) {
                LOG.error("Illegal method access.", ex);
            } catch (InvocationTargetException ex) {
                LOG.error("Invocation exception.", ex);
            }
        }

        return null;
    }
}
