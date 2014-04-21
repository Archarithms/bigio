/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.core.member;

import com.a2i.dms.core.Envelope;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author atrimble
 */
public interface Member {
    
    public AtomicInteger getSequence();

    public MemberStatus getStatus();

    public void setStatus(MemberStatus status);

    public Map<String, String> getTags();

    public String getIp();

    public void setIp(String ip);

    public int getDataPort();

    public void setDataPort(int dataPort);

    public int getGossipPort();

    public void setGossipPort(int gossipPort);

    public void send(Envelope message) throws IOException;

}
