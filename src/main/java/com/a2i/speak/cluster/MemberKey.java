/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

/**
 *
 * @author atrimble
 */
public class MemberKey {
    public static String getKey(Member member) {
        return new StringBuilder()
                .append(member.getIp())
                .append(":")
                .append(member.getCommandPort())
                .append(":")
                .append(member.getDataPort()).toString();
    }

    public static String getKey(CommandMessage message) {
        return new StringBuilder()
                .append(message.getIp())
                .append(":")
                .append(message.getCommandPort())
                .append(":")
                .append(message.getDataPort()).toString();
    }

    public static RemoteMember decode(String key) {
        String[] values = key.split(":");
        RemoteMember mem = new RemoteMember();
        mem.setIp(values[0]);
        mem.setCommandPort(Integer.parseInt(values[1]));
        mem.setDataPort(Integer.parseInt(values[2]));
        return mem;
    }
}
