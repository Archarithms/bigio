/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak;

/**
 *
 * @author atrimble
 */
public class Envelope {
    
    private String senderKey;
    private int sequence;
    private long executeTime;
    private String topic;
    private byte[] payload;

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
    public long getExecuteTime() {
        return executeTime;
    }

    /**
     * @param executeTime the executeTime to set
     */
    public void setExecuteTime(long executeTime) {
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
}
