/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.core;

import com.a2i.dms.core.member.AbstractMember;
import com.a2i.dms.core.member.Member;
import com.a2i.dms.util.Relation;
import java.util.regex.Pattern;
import reactor.event.Event;
import reactor.function.Consumer;

/**
 *
 * @author atrimble
 */
public class Registration implements Relation {
    private Member member = null;
    private String topic = null;
    private Pattern partition = null;
    private MessageListener listener = null;
    private Consumer<Event<Envelope>> consumer = null;

    public Registration(Member member, String topic, Pattern partition) {
        this.member = member;
        this.topic = topic;
        this.partition = partition;
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public Class<?> getClass(int itemNum) {
        switch(itemNum) {
            case 0:
                return AbstractMember.class;
            case 1:
                return String.class;
            case 2:
                return Pattern.class;
            default:
                return null;
        }
    }

    @Override
    public Object getItem(int itemNum) {
        switch(itemNum) {
            case 0:
                return getMember();
            case 1:
                return getTopic();
            case 2:
                return getPartition();
            default:
                return null;
        }
    }

    /**
     * @return the member
     */
    public Member getMember() {
        return member;
    }

    /**
     * @return the listener
     */
    public MessageListener getListener() {
        return listener;
    }

    /**
     * @return the consumer
     */
    public Consumer<Event<Envelope>> getConsumer() {
        return consumer;
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
    public Pattern getPartition() {
        return partition;
    }

    /**
     * @param member the member to set
     */
    public void setMember(Member member) {
        this.member = member;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    /**
     * @param consumer the consumer to set
     */
    public void setConsumer(Consumer<Event<Envelope>> consumer) {
        this.consumer = consumer;
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
    public void setPartition(Pattern partition) {
        this.partition = partition;
    }
}
