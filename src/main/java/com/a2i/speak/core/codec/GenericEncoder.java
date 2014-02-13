/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

/**
 *
 * @author atrimble
 */
public class GenericEncoder {

    private static final MessagePack msgPack = new MessagePack();
    
    public static byte[] encode(Object message) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Packer packer = msgPack.createPacker(out);
        packer.write("Hello World!");

        return out.toByteArray();
    }
}
