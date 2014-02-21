/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core;

/**
 *
 * @author atrimble
 * @param <T>
 */
public class Envelope<T> {
    
    private String senderKey;
    private int sequence;
    private int executeTime;
    private int millisecondsSinceMidnight;
    private String topic;
    private String className;
    private byte[] payload;

    private boolean decoded = false;
    private T message;

    /**
     * @return the senderKey
     */
    public String getSenderKey() {
        return senderKey;
    }

    /**
     * @param senderKey the senderKey to set
     */
    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }

    /**
     * @return the sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * @return the executeTime
     */
    public int getExecuteTime() {
        return executeTime;
    }

    /**
     * @param executeTime the executeTime to set
     */
    public void setExecuteTime(int executeTime) {
        this.executeTime = executeTime;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * @return the decoded
     */
    public boolean isDecoded() {
        return decoded;
    }

    /**
     * @param decoded the decoded to set
     */
    public void setDecoded(boolean decoded) {
        this.decoded = decoded;
    }

    /**
     * @return the message
     */
    public T getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(T message) {
        this.message = message;
    }

    /**
     * @return the millisecondsSinceMidnight
     */
    public int getMillisecondsSinceMidnight() {
        return millisecondsSinceMidnight;
    }

    /**
     * @param millisecondsSinceMidnight the millisecondsSinceMidnight to set
     */
    public void setMillisecondsSinceMidnight(int millisecondsSinceMidnight) {
        this.millisecondsSinceMidnight = millisecondsSinceMidnight;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }
}
