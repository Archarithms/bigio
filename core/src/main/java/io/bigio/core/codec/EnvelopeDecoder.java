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

import io.bigio.core.Envelope;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.IOException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

/**
 * This is a class for decoding envelope messages.
 * 
 * @author Andy Trimble
 */
public class EnvelopeDecoder {
    
    private static final MessagePack msgPack = new MessagePack();

    private EnvelopeDecoder() {

    }

    /**
     * Decode a message envelope.
     * 
     * @param bytes the raw message.
     * @return the decoded message.
     * @throws IOException in case of a decode error.
     */
    public static Envelope decode(ByteBuf bytes) throws IOException {
        MessageUnpacker unpacker = msgPack.newUnpacker(new ByteBufInputStream(bytes));
        Envelope message = decode(unpacker);
        return message;
    }
    
    /**
     * Decode a message envelope.
     * 
     * @param bytes the raw message.
     * @return the decoded message.
     * @throws IOException in case of a decode error.
     */
    public static Envelope decode(byte[] bytes) throws IOException {
        MessageUnpacker unpacker = msgPack.newUnpacker(bytes);
        Envelope message = decode(unpacker);
        return message;
    }

    /**
     * Decode a message envelope.
     * 
     * @param unpacker a MsgPack object containing the raw message.
     * @return the decoded message.
     * @throws IOException in case of a decode error.
     */
    private static Envelope decode(MessageUnpacker unpacker) throws IOException {

        Envelope message = new Envelope();

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder
                .append(unpacker.unpackInt())
                .append(".")
                .append(unpacker.unpackInt())
                .append(".")
                .append(unpacker.unpackInt())
                .append(".")
                .append(unpacker.unpackInt())
                .append(":")
                .append(unpacker.unpackInt())
                .append(":")
                .append(unpacker.unpackInt());
        message.setSenderKey(keyBuilder.toString());
        message.setEncrypted(unpacker.unpackBoolean());
        if(message.isEncrypted()) {
            int length = unpacker.unpackArrayHeader();
            byte[] key = new byte[length];
            for(int i = 0; i < length; ++i) {
                key[i] = unpacker.unpackByte();
            }
            message.setKey(key);
        }
        message.setExecuteTime(unpacker.unpackInt());
        message.setMillisecondsSinceMidnight(unpacker.unpackInt());
        message.setTopic(unpacker.unpackString());
        message.setPartition(unpacker.unpackString());
        message.setClassName(unpacker.unpackString());

        int length = unpacker.unpackArrayHeader();
        byte[] payload = new byte[length];
        for(int i = 0; i < length; ++i) {
            payload[i] = unpacker.unpackByte();
        }
        message.setPayload(payload);

        return message;
    }
}
