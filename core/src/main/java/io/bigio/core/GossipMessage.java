/*
 * Copyright (c) 2014, Archarithms Inc.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A message that contains the gossiped data.
 * 
 * @author Andy Trimble
 */
public class GossipMessage {

    private String ip;
    private int gossipPort;
    private int dataPort;
    private int millisecondsSinceMidnight;
    private byte[] publicKey = null;
    private final Map<String, String> tags = new HashMap<>();
    private final List<String> members = new ArrayList<>();
    private final List<Integer> clock = new ArrayList<>();
    private final Map<String, List<String>> listeners = new HashMap<>();

    /**
     * Default constructor.
     */
    public GossipMessage() {
        
    }

    /**
     * Constructor with sender's information.
     * 
     * @param ip the IP address of the sender.
     * @param gossipPort the gossip port of the sender.
     * @param dataPort the data port of the sender.
     */
    public GossipMessage(String ip, int gossipPort, int dataPort) {
        this.ip = ip;
        this.gossipPort = gossipPort;
        this.dataPort = dataPort;
    }

    /**
     * Produce a nice textual representation of the message.
     * 
     * @return the message as a string.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("GossipMessage: ").append("\n")
                .append("Address: ").append(getIp()).append("\n")
                .append("GossipPort: ").append(getGossipPort()).append("\n")
                .append("DataPort: ").append(getDataPort()).append("\n")
                .append("Time: ").append(getMillisecondsSinceMidnight()).append("\n")
                .append("Tags: ").append("\n");
        getTags().keySet().stream().forEach((key) -> {
            buff.append("    ").append(key).append(" -> ").append(getTags().get(key)).append("\n");
        });
        buff.append("Members: ").append("\n");
        for(int i = 0; i < getMembers().size(); ++i) {
            buff.append("    ").append(getMembers().get(i)).append(" -- ").append(getClock().get(i)).append("\n");
        }
        buff.append("Listeners: ").append("\n");
        getListeners().keySet().stream().map((key) -> {
            buff.append("    ").append(key).append("\n");
            return key;
        }).forEach((key) -> {
            getListeners().get(key).stream().forEach((topic) -> {
                buff.append("        ").append(topic).append("\n");
            });
        });
        return buff.toString();
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the gossipPort
     */
    public int getGossipPort() {
        return gossipPort;
    }

    /**
     * @param gossipPort the gossipPort to set
     */
    public void setGossipPort(int gossipPort) {
        this.gossipPort = gossipPort;
    }

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @return the dataPort
     */
    public int getDataPort() {
        return dataPort;
    }

    /**
     * @param dataPort the dataPort to set
     */
    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * @return the members
     */
    public List<String> getMembers() {
        return members;
    }

    /**
     * @return the listeners
     */
    public Map<String, List<String>> getListeners() {
        return listeners;
    }

    /**
     * @return the millisecondsSinceMidnight
     */
    public int getMillisecondsSinceMidnight() {
        return millisecondsSinceMidnight;
    }

    /**
     * @return the clock
     */
    public List<Integer> getClock() {
        return clock;
    }

    /**
     * @param millisecondsSinceMidnight the millisecondsSinceMidnight to set
     */
    public void setMillisecondsSinceMidnight(int millisecondsSinceMidnight) {
        this.millisecondsSinceMidnight = millisecondsSinceMidnight;
    }

    /**
     * @return the publicKey
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey the publicKey to set
     */
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
