/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

/**
 *
 * @author atrimble
 */
public class GossipEncoder {

    private static final MessagePack msgPack = new MessagePack();
    
    public static byte[] encode(GossipMessage message) throws IOException {
        ByteArrayOutputStream msgBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String[] splitIp = message.getIp().split("\\.");
        List<List<Integer>> members = new ArrayList<>();
        for(String m : message.getMembers()) {
            List<Integer> memberList = new ArrayList<>();
            String[] keys = m.split(":");
            String[] memIp = keys[0].split("\\.");
            memberList.add(Integer.parseInt(memIp[0]));
            memberList.add(Integer.parseInt(memIp[1]));
            memberList.add(Integer.parseInt(memIp[2]));
            memberList.add(Integer.parseInt(memIp[3]));
            memberList.add(Integer.parseInt(keys[1]));
            memberList.add(Integer.parseInt(keys[2]));
            members.add(memberList);
        }

        Packer packer = msgPack.createPacker(msgBuffer);
        packer.write(message.getSequence());
        packer.write(Integer.parseInt(splitIp[0]));
        packer.write(Integer.parseInt(splitIp[1]));
        packer.write(Integer.parseInt(splitIp[2]));
        packer.write(Integer.parseInt(splitIp[3]));
        packer.write(message.getGossipPort());
        packer.write(message.getDataPort());
        packer.write(message.getTags());
        packer.write(members);
        packer.write(message.getListeners());

        out.write((short)msgBuffer.size() >>> 8);
        out.write((short)msgBuffer.size());
        msgBuffer.writeTo(out);

        return out.toByteArray();
    }
}
