/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.core.ClusterService;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.core.member.MemberHolder;
import com.a2i.sim.core.member.MemberKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class TagCommand implements CommandLine {

    private static final String USAGE = "Usage: tag <add|remove|list>";

    @Autowired
    private ClusterService cluster;

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
                for(Member m : MemberHolder.INSTANCE.getActiveMembers()) {
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
}
