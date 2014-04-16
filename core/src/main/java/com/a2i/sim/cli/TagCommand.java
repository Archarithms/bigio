/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.Component;
import com.a2i.sim.Inject;
import com.a2i.sim.core.ClusterService;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.core.member.MemberHolder;
import com.a2i.sim.core.member.MemberKey;

/**
 *
 * @author atrimble
 */
@Component
public class TagCommand implements CommandLine {

    private static final String USAGE = "Usage: tag [add <key> <value> | remove <key> | list]";

    @Inject
    private ClusterService cluster;

    @Inject
    private MemberHolder memberHolder;

    @Override
    public String getCommand() {
        return "tag";
    }

    @Override
    public void execute(String... args) {
        if(args.length < 2) {
            System.out.println("Usage: tag <add|remove|list>");
            return;
        }

        switch(args[1]) {
            case "add":
                if(args.length < 4) {
                    System.out.println("Usage: tag add <key> <value>");
                    return;
                }
                cluster.getMe().getTags().put(args[2], args[3]);
                break;
            case "remove":
                if(args.length < 3) {
                    System.out.println("Usage: tag remove <key>");
                    return;
                }
                cluster.getMe().getTags().remove(args[2]);
                break;
            case "list":
                StringBuilder buff = new StringBuilder();
                for(Member m : memberHolder.getActiveMembers()) {
                    String key = MemberKey.getKey(m);
                    buff.append(key).append("\n");

                    for(String k : m.getTags().keySet()) {
                        buff.append("    ").append(k).append(" --> ").append(m.getTags().get(k)).append("\n");
                    }
                }
                System.out.println(buff.toString());
                break;
            default:
                System.out.println(USAGE);
        }
    }

    @Override
    public String help() {
        return "Sets or displays tag information. " + USAGE;
    }
}
