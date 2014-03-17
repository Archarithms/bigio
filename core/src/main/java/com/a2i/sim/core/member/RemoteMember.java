/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.sim.core.member;

import com.a2i.sim.core.GossipMessage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public abstract class RemoteMember extends AbstractMember {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteMember.class);

    public RemoteMember() {
        super();
    }

    public RemoteMember(String ip, int gossipPort, int dataPort) {
        super(ip, gossipPort, dataPort);
    }

    public abstract void gossip(final GossipMessage message) throws IOException;
}
