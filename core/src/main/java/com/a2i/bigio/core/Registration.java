/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.core;

import com.a2i.bigio.core.member.Member;

/**
 *
 * @author atrimble
 */
public class Registration {
    private Member member = null;
    private String topic = null;
    private String partition = null;

    public Registration(Member member, String topic, String partition) {
        this.member = member;
        this.topic = topic;
        this.partition = partition;
    }

    /**
     * @return the member
     */
    public Member getMember() {
        return member;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the partition 
     */
    public String getPartition() {
        return partition;
    }

    /**
     * @param member the member to set
     */
    public void setMember(Member member) {
        this.member = member;
    }

    /**
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * @param partition the partition to set
     */
    public void setPartition(String partition) {
        this.partition = partition;
    }
}
