/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

/**
 *
 * @author atrimble
 */
public class EnvelopeEncoder {

    private static final MessagePack msgPack = new MessagePack();
    
    public static byte[] encode(Envelope message) throws IOException {
        ByteArrayOutputStream msgBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Packer packer = msgPack.createPacker(msgBuffer);

        String[] keys = message.getSenderKey().split(":");
        String[] ip = keys[0].split("\\.");
        packer.write(Integer.parseInt(ip[0]));
        packer.write(Integer.parseInt(ip[1]));
        packer.write(Integer.parseInt(ip[2]));
        packer.write(Integer.parseInt(ip[3]));
        packer.write(Integer.parseInt(keys[1]));
        packer.write(Integer.parseInt(keys[2]));

        packer.write(message.getSequence());
        packer.write(message.getExecuteTime());
        packer.write(message.getTopic());
        packer.write(message.getPayload());

        out.write((short)msgBuffer.size() >>> 8);
        out.write((short)msgBuffer.size());
        msgBuffer.writeTo(out);

        return out.toByteArray();
    }
}
