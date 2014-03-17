/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.core.member;

import com.a2i.sim.core.GossipMessage;

/**
 *
 * @author atrimble
 */
public class MemberKey {
    public static String getKey(Member member) {
        return new StringBuilder()
                .append(member.getIp())
                .append(":")
                .append(member.getGossipPort())
                .append(":")
                .append(member.getDataPort()).toString();
    }

    public static String getKey(GossipMessage message) {
        return new StringBuilder()
                .append(message.getIp())
                .append(":")
                .append(message.getGossipPort())
                .append(":")
                .append(message.getDataPort()).toString();
    }

    public static RemoteMemberTCP decode(String key) {
        String[] values = key.split(":");
        RemoteMemberTCP mem = new RemoteMemberTCP();
        mem.setIp(values[0]);
        mem.setGossipPort(Integer.parseInt(values[1]));
        mem.setDataPort(Integer.parseInt(values[2]));
        return mem;
    }
}
