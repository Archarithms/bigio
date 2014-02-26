/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core;

import com.a2i.sim.core.member.AbstractMember;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.util.Relation;
import reactor.event.Event;
import reactor.function.Consumer;

/**
 *
 * @author atrimble
 */
public class Registration implements Relation {
    private Member member = null;
    private String topic = null;
    private MessageListener listener = null;
    private Consumer<Event<Envelope>> consumer = null;

    public Registration(Member member, String topic) {
        this.member = member;
        this.topic = topic;
    }

    @Override
    public int getLength() {
        return 2;
    }

    @Override
    public Class<?> getClass(int itemNum) {
        switch(itemNum) {
            case 0:
                return AbstractMember.class;
            case 1:
                return String.class;
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
}
