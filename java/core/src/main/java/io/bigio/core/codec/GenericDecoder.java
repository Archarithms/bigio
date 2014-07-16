/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio.core.codec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for decoding generic messages. Generic messages have the
 * encoding/decoding logic injected at runtime using ASM. This class
 * reflectively calls the injected decode method.
 * 
 * @author Andy Trimble
 */
public class GenericDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(GenericDecoder.class);

    private static final Map<Class, Method> METHODS = new HashMap<>();
    
    /**
     * Decode a message payload.
     * 
     * @param className the type of message.
     * @param bytes the raw message.
     * @return a decoded message.
     * @throws IOException in case of a decoding error.
     */
    public static Object decode(String className, byte[] bytes) throws IOException {

        try {
            Class clazz = Class.forName(className);

            if(clazz.getAnnotation(io.bigio.Message.class) != null) {
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
