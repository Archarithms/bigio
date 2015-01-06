/*
 * Copyright (c) 2015, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio.core;

/**
 * A message envelope containing the BigIO metadata for all messages.
 * 
 * @author Andy Trimble
 * @param <T> the wrapped message type
 */
public class Envelope<T> {
    
    private String senderKey;
    private int executeTime;
    private int millisecondsSinceMidnight;
    private String topic;
    private String partition;
    private String className;
    private byte[] payload;

    private byte[] key;
    private boolean decoded = false;
    private boolean encrypted = false;
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

    /**
     * @return the partition
     */
    public String getPartition() {
        return partition;
    }

    /**
     * @param partition the partition to set
     */
    public void setPartition(String partition) {
        this.partition = partition;
    }

    /**
     * @return the encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * @param encrypted the encrypted to set
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * @return the key
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(byte[] key) {
        this.key = key;
    }
}
