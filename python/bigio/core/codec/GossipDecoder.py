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

import io.bigio.core.GossipMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

/**
 * This is a class for decoding gossip messages.
 * 
 * @author Andy Trimble
 */
public class GossipDecoder {

    private static final MessagePack msgPack = new MessagePack();
    
    private static final Template<Map<String, String>> tagTemplate = 
            Templates.tMap(Templates.TString, Templates.TString);
    private static final Template<Map<String, List<String>>> listenerTemplate = 
            Templates.tMap(Templates.TString, Templates.tList(Templates.TString));
    private static final Template<List<List<Integer>>> memberTemplate = 
            Templates.tList(Templates.tList(Templates.TInteger));
    private static final Template<List<Integer>> clockTemplate = 
            Templates.tList(Templates.TInteger);
    
    /**
     * Decode a gossip message.
     * 
     * @param bytes the raw message.
     * @return the decoded message.
     * @throws IOException in case of an error in decoding.
     */
    public static GossipMessage decode(ByteBuffer bytes) throws IOException {

        // Discard the message length.
        // This method is only used by the Multicast discovery mechanism.
        // The TCP mechanism already has the size bytes stripped off.
        bytes.get();
        bytes.get();

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        return decode(unpacker);

    }

    /**
     * Decode a gossip message.
     * 
     * @param bytes the raw message.
     * @return the decoded message.
     * @throws IOException in case of an error in decoding.
     */
    public static GossipMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
        
        return decode(unpacker);
    }

    /**
     * Decode a gossip message.
     * 
     * @param unpacker an object containing the raw message.
     * @return the decoded message.
     * @throws IOException in case of an error in decoding.
     */
    private static GossipMessage decode(Unpacker unpacker) throws IOException {
        GossipMessage message = new GossipMessage();

        StringBuilder ipBuilder = new StringBuilder();
        ipBuilder.append(
                unpacker.readInt())
                .append(".")
                .append(unpacker.readInt())
                .append(".")
                .append(unpacker.readInt())
                .append(".")
                .append(unpacker.readInt());
        
        message.setIp(ipBuilder.toString());
        
        message.setGossipPort(unpacker.readInt());
        message.setDataPort(unpacker.readInt());
        message.setMillisecondsSinceMidnight(unpacker.readInt());
        message.getTags().putAll(unpacker.read(tagTemplate));
        
        List<List<Integer>> member = unpacker.read(memberTemplate);
        for(List<Integer> m : member) {
            ipBuilder = new StringBuilder();
            ipBuilder.append(
                    m.get(0))
                    .append(".")
                    .append(m.get(1))
                    .append(".")
                    .append(m.get(2))
                    .append(".")
                    .append(m.get(3))
                    .append(":")
                    .append(m.get(4))
                    .append(":")
                    .append(m.get(5));
            message.getMembers().add(ipBuilder.toString());
        }

        message.getClock().addAll(unpacker.read(clockTemplate));
        
        Map<String, List<String>> tmpMap = unpacker.read(listenerTemplate);
        for(String key : tmpMap.keySet()) {
            List<String> tmpList = new ArrayList<>();
            tmpList.addAll(tmpMap.get(key));
            message.getListeners().put(key, tmpList);
        }

        return message;
    }
}
