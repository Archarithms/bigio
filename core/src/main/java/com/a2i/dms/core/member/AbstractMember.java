/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.core.member;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author atrimble
 */
public abstract class AbstractMember implements Member {

    private final Map<String, String> tags = new HashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);
    private MemberStatus status = MemberStatus.Unknown;
    private String ip;
    private int dataPort;
    private int gossipPort;
    protected MemberHolder memberHolder;
    
    public abstract void initialize();
    public abstract void shutdown();

    public AbstractMember(MemberHolder memberHolder) {
        this.memberHolder = memberHolder;
    }

    public AbstractMember(String ip, int gossipPort, int dataPort, MemberHolder memberHolder) {
        this.ip = ip;
        this.gossipPort = gossipPort;
        this.dataPort = dataPort;
        this.memberHolder = memberHolder;
    }

    public final void setMemberHolder(MemberHolder holder) {
        memberHolder = holder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\nMember ").append(ip);
        builder.append(":");
        builder.append(gossipPort);
        builder.append(":");
        builder.append(dataPort);
        if (status == MemberStatus.Alive || status == MemberStatus.Unknown) {
            builder.append("\n    is ");
        } else {
            builder.append("\n    has ");
        }
        builder.append(status);

        builder.append("\n    with properties");
        for (String key : tags.keySet()) {
            builder.append("\n        ");
            builder.append(key);
            builder.append(" -> ");
            builder.append(tags.get(key));
        }

        builder.append("\n");

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Member) {
            Member them = (Member) obj;
            return them.getIp() != null && getIp() != null 
                    && them.getIp().equals(getIp())
                    && them.getGossipPort() == getGossipPort()
                    && them.getDataPort() == getDataPort();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.ip);
        hash = 83 * hash + this.dataPort;
        hash = 83 * hash + this.gossipPort;
        return hash;
    }

    /**
     * @return the sequence
     */
    @Override
    public AtomicInteger getSequence() {
        return sequence;
    }

    /**
     * @return the status
     */
    @Override
    public MemberStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    @Override
    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    /**
     * @return the tags
     */
    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @return the ip
     */
    @Override
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    @Override
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the data port
     */
    @Override
    public int getDataPort() {
        return dataPort;
    }

    /**
     * @param dataPort the data port to set
     */
    @Override
    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * @return the gossipPort 
     */
    @Override
    public int getGossipPort() {
        return gossipPort;
    }

    /**
     * @param gossipPort the gossip port to set
     */
    @Override
    public void setGossipPort(int gossipPort) {
        this.gossipPort = gossipPort;
    }
}
