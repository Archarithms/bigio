/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author atrimble
 */
public class Member {

    public enum Status { 
        Alive, Left, Failed, Unknown;
    
        public static Status fromString(String in) {
            switch (in) {
                case "alive":
                    return Alive;
                case "left":
                    return Left;
                case "failed":
                    return Failed;
                default:
                    return Unknown;
            }
        }
    };

    private String name;
    private Status status;

    private final Map<String, String> tags = new HashMap<>();
    
    private final AtomicInteger sequence = new AtomicInteger(0);

    private String ip;
    private int dataPort;
    private int commandPort;

    public Member() {

    }

    public Member(String ip, int comandPort, int dataPort) {
        this.ip = ip;
        this.commandPort = comandPort;
        this.dataPort = dataPort;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\nMember ").append(name);
        builder.append("\n    Located at ");
        builder.append(ip);
        builder.append(":");
        builder.append(commandPort);
        builder.append(",");
        builder.append(dataPort);
        if(status == Status.Alive || status == Status.Unknown) {
            builder.append("\n    is ");
        } else {
            builder.append("\n    has ");
        }
        builder.append(status);

        builder.append("\n    with properties");
        for(String key : tags.keySet()) {
            builder.append("\n        ");
            builder.append(key);
            builder.append(" -> ");
            builder.append(tags.get(key));
        }

        builder.append("\n");

        return builder.toString();
    }

    /**
     * @return the sequence
     */
    public AtomicInteger getSequence() {
        return sequence;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
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
     * @return the data port
     */
    public int getDataPort() {
        return dataPort;
    }

    /**
     * @param dataPort the data port to set
     */
    public void setPort(int dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * @return the commandPort
     */
    public int getCommandPort() {
        return commandPort;
    }

    /**
     * @param commandPort the commandPort to set
     */
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }
}
