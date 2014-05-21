/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.bigio.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class GossipMessage {

    private static final Logger LOG = LoggerFactory.getLogger(GossipMessage.class);

    private String ip;
    private int gossipPort;
    private int dataPort;
    private int millisecondsSinceMidnight;
    private final Map<String, String> tags = new HashMap<>();
    private final List<String> members = new ArrayList<>();
    private final List<Integer> clock = new ArrayList<>();
    private final Map<String, List<String>> listeners = new HashMap<>();

    public GossipMessage() {
        
    }

    public GossipMessage(String ip, int gossipPort, int dataPort) {
        this.ip = ip;
        this.gossipPort = gossipPort;
        this.dataPort = dataPort;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("GossipMessage: ").append("\n")
                .append("Address: ").append(getIp()).append("\n")
                .append("GossipPort: ").append(getGossipPort()).append("\n")
                .append("DataPort: ").append(getDataPort()).append("\n")
                .append("Time: ").append(getMillisecondsSinceMidnight()).append("\n")
                .append("Tags: ").append("\n");
        for(String key : getTags().keySet()) {
            buff.append("    ").append(key).append(" -> ").append(getTags().get(key)).append("\n");
        }
        buff.append("Members: ").append("\n");
        for(int i = 0; i < getMembers().size(); ++i) {
            buff.append("    ").append(getMembers().get(i)).append(" -- ").append(getClock().get(i)).append("\n");
        }
        buff.append("Listeners: ").append("\n");
        for(String key : getListeners().keySet()) {
            buff.append("    ").append(key).append("\n");
            for(String topic : getListeners().get(key)) {
                buff.append("        ").append(topic).append("\n");
            }
        }
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
}
