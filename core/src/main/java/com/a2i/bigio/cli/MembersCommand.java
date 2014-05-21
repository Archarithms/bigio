/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.cli;

import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import com.a2i.bigio.Inject;
import com.a2i.bigio.Speaker;
import com.a2i.bigio.core.member.Member;
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
