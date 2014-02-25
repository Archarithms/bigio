/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.core.ListenerRegistry;
import com.a2i.sim.core.Member;
import com.a2i.sim.core.MemberKey;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class ListenersMessage implements CommandLine {

    @Override
    public String getCommand() {
        return "listeners";
    }

    @Override
    public void execute(String... args) {
        StringBuilder buff = new StringBuilder();

        for(String topic : ListenerRegistry.INSTANCE.getLocalTopicsOfInterest()) {
            buff.append("\n").append(topic).append(":").append("\n");
            for(Member member : ListenerRegistry.INSTANCE.getRegisteredMembers(topic)) {
                buff.append("    ").append(MemberKey.getKey(member)).append("\n");
            }
        }

        System.out.println(buff.toString());
    }
}
