/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.Component;
import com.a2i.sim.Inject;
import com.a2i.sim.Speaker;
import com.a2i.sim.core.member.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class MembersCommand implements CommandLine {

    private static final Logger LOG = LoggerFactory.getLogger(MembersCommand.class);
    
    @Inject
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

    @Override
    public String help() {
        return "Prints the set of known members of the cluster.";
    }
}
