/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

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
public class CommandMessage {

    private static final Logger LOG = LoggerFactory.getLogger(CommandMessage.class);

    private int sequence;
    private String ip;
    private int commandPort;
    private int dataPort;
    private final Map<String, String> tags = new HashMap<>();
    private final List<String> members = new ArrayList<>();
    private final Map<String, String> listeners = new HashMap<>();

    public CommandMessage() {
        
    }

    public CommandMessage(int sequence, String ip, int commandPort, int dataPort) {
        this.sequence = sequence;
        this.ip = ip;
        this.commandPort = commandPort;
        this.dataPort = dataPort;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("Command: ").append("\n")
                .append("Sequence: ").append(getSequence()).append("\n")
                .append("Address: ").append(getIp()).append("\n")
                .append("CommandPort: ").append(getCommandPort()).append("\n")
                .append("DataPort: ").append(getDataPort()).append("\n")
                .append("Tags: ").append("\n");
        for(String key : getTags().keySet()) {
            buff.append("    ").append(key).append(" -> ").append(getTags().get(key)).append("\n");
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
     * @return the commandPort
     */
    public int getCommandPort() {
        return commandPort;
    }

    /**
     * @param commandPort the command port to set
     */
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
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
     * @return the members
     */
    public List<String> getMembers() {
        return members;
    }

    /**
     * @return the listeners
     */
    public Map<String, String> getListeners() {
        return listeners;
    }
}
