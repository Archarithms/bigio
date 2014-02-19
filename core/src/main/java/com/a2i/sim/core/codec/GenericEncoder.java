/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class GenericEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(GenericEncoder.class);

    private static final MessagePack msgPack = new MessagePack();
    
    public static byte[] encode(Object message) throws IOException {
        if(message.getClass().getAnnotation(com.a2i.sim.core.Message.class) != null) {
            try {
                Method method = message.getClass().getMethod("_encode_");
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

//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        
//        Packer packer = msgPack.createPacker(out);
//        packer.write("Hello World!");
//
//        return out.toByteArray();
    }
}
