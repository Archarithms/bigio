/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.core.codec;

import com.a2i.sim.core.GossipMessage;
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
 *
 * @author atrimble
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
    
    public static GossipMessage decode(ByteBuffer bytes) throws IOException {

        // Discard the message length.
        // This method is only used by the Multicast discovery mechanism.
        // The TCP mechanism already has the size bytes stripped off.
        bytes.get();
        bytes.get();

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        return decode(unpacker);

    }

    public static GossipMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
        
        return decode(unpacker);
    }

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
