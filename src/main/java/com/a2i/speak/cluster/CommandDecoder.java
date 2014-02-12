/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import java.io.IOException;
import java.nio.ByteBuffer;
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
public class CommandDecoder {

    private static final MessagePack msgPack = new MessagePack();
    private static final Template<Map<String, String>> mapTemplate = 
            Templates.tMap(Templates.TString, Templates.TString);
    private static final Template<List<List<Integer>>> listTemplate = 
            Templates.tList(Templates.tList(Templates.TInteger));
    
    public static CommandMessage decode(ByteBuffer bytes) throws IOException {

        // Discard the message length.
        // This method is only used by the Multicast discovery mechanism.
        // The TCP mechanism already has the size bytes stripped off.
        bytes.get();
        bytes.get();

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        CommandMessage message = new CommandMessage();

        message.setSequence(unpacker.readInt());
        
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
        message.setCommandPort(unpacker.readInt());
        message.setDataPort(unpacker.readInt());
        message.getTags().putAll(unpacker.read(mapTemplate));
        
        List<List<Integer>> member = unpacker.read(listTemplate);
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
        
        message.getListeners().putAll(unpacker.read(mapTemplate));

        return message;
    }

    public static CommandMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        CommandMessage message = new CommandMessage();

        message.setSequence(unpacker.readInt());

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
        
        message.setCommandPort(unpacker.readInt());
        message.setDataPort(unpacker.readInt());
        message.getTags().putAll(unpacker.read(mapTemplate));
        
        List<List<Integer>> member = unpacker.read(listTemplate);
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
        
        message.getListeners().putAll(unpacker.read(mapTemplate));

        return message;
    }
}
