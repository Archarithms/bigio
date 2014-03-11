/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.Speaker;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.util.NetworkUtil;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class MembersCommand implements CommandLine {

    private static final Logger LOG = LoggerFactory.getLogger(MembersCommand.class);
    
    @Autowired
    private Speaker speaker;

    @Override
    public String getCommand() {
        return "members";
    }

    @Override
    public void execute(String... args) {
        System.out.println();
        for(Member member : speaker.listMembers()) {
            System.out.println(member.toString());
        }
    }
}
