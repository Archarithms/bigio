/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.core.member;

import com.a2i.dms.core.GossipMessage;

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
}
