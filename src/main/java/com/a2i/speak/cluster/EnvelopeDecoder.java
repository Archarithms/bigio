/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author atrimble
 */
public class EnvelopeDecoder {
    
    private static final MessagePack msgPack = new MessagePack();
    
    public static Envelope decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        Envelope message = new Envelope();

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder
                .append(unpacker.readInt())
                .append(".")
                .append(unpacker.readInt())
                .append(".")
                .append(unpacker.readInt())
                .append(".")
                .append(unpacker.readInt())
                .append(":")
                .append(unpacker.readInt())
                .append(":")
                .append(unpacker.readInt());
        message.setSenderKey(keyBuilder.toString());
        message.setSequence(unpacker.readInt());
        message.setExecuteTime(unpacker.readLong());
        message.setTopic(unpacker.readString());
        message.setPayload(unpacker.readByteArray());

        return message;
    }
}
