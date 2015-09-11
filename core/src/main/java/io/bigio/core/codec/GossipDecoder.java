/*
 * Copyright (c) 2015, Archarithms Inc.
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

import io.bigio.core.GossipMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageTypeException;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for decoding gossip messages.
 * 
 * @author Andy Trimble
 */
public class GossipDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(GossipDecoder.class);
    private static final MessagePack msgPack = new MessagePack();
    
    private GossipDecoder() {

    }
    
    /**
     * Decode a gossip message.
     * 
     * @param bytes the raw message.
     * @return the decoded message.
     * @throws IOException in case of an error in decoding.
     */
    public static GossipMessage decode(byte[] bytes) throws IOException, MessageTypeException {

        MessageUnpacker unpacker = msgPack.newUnpacker(bytes);
        
        return decode(unpacker);
    }

    /**
     * Decode a gossip message.
     * 
     * @param unpacker an object containing the raw message.
     * @return the decoded message.
     * @throws IOException in case of an error in decoding.
     */
    private static GossipMessage decode(MessageUnpacker unpacker) throws IOException, MessageTypeException {
        GossipMessage message = new GossipMessage();

        StringBuilder ipBuilder = new StringBuilder();
        ipBuilder
                .append(unpacker.unpackInt())
                .append(".")
                .append(unpacker.unpackInt())
                .append(".")
                .append(unpacker.unpackInt())
                .append(".")
                .append(unpacker.unpackInt());

        message.setIp(ipBuilder.toString());

        message.setGossipPort(unpacker.unpackInt());
        message.setDataPort(unpacker.unpackInt());
        message.setMillisecondsSinceMidnight(unpacker.unpackInt());
        boolean hasPublicKey = unpacker.unpackBoolean();
        if(hasPublicKey) {
            int length = unpacker.unpackArrayHeader();
            byte[] key = new byte[length];
            for(int i = 0; i < length; ++i) {
                key[i] = unpacker.unpackByte();
            }
            message.setPublicKey(key);
        }

        int items = unpacker.unpackMapHeader();
        for(int i = 0; i < items; ++i) {
            message.getTags().put(unpacker.unpackString(), unpacker.unpackString());
        }

        int num = unpacker.unpackArrayHeader();
        for(int i = 0; i < num; ++i) {
            unpacker.unpackArrayHeader();
            ipBuilder = new StringBuilder();
            ipBuilder.append(unpacker.unpackInt())
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
            message.getMembers().add(ipBuilder.toString());
        }

        num = unpacker.unpackArrayHeader();
        for(int i = 0; i < num; ++i) {
            message.getClock().add(unpacker.unpackInt());
        }

        items = unpacker.unpackMapHeader();
        for(int i = 0; i < items; ++i) {
            String key = unpacker.unpackString();
            num = unpacker.unpackArrayHeader();
            List<String> tmpList = new ArrayList<>();
            for(int j = 0; j < num; ++j) {
                tmpList.add(unpacker.unpackString());
            }
            message.getListeners().put(key, tmpList);
        }

        return message;
    }
}
