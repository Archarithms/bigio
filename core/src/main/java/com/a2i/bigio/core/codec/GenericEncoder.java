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

package com.a2i.bigio.core.codec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for encoding generic messages. Generic messages have the
 * encoding/decoding logic injected at runtime using ASM. This class
 * reflectively calls the injected encode method.
 * 
 * @author Andy Trimble
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
