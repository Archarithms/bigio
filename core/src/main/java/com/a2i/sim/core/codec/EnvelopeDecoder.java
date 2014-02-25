/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core.codec;

import com.a2i.sim.core.Envelope;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author atrimble
 */
public class EnvelopeDecoder {
    
    private static final MessagePack msgPack = new MessagePack();

    public static Envelope decode(ByteBuf bytes) throws IOException {
        Unpacker unpacker = msgPack.createUnpacker(new ByteBufInputStream(bytes));
        Envelope message = decode(unpacker);
        return message;
    }
    
    public static Envelope decode(byte[] bytes) throws IOException {
        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
        Envelope message = decode(unpacker);
        return message;
    }

    private static Envelope decode(Unpacker unpacker) throws IOException {

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
        message.setExecuteTime(unpacker.readInt());
        message.setMillisecondsSinceMidnight(unpacker.readInt());
        message.setTopic(unpacker.readString());
        message.setClassName(unpacker.readString());
        message.setPayload(unpacker.readByteArray());

        return message;
    }
}
