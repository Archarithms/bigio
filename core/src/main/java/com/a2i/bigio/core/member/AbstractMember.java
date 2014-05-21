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

package com.a2i.bigio.core.member;

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
