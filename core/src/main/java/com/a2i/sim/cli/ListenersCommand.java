/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.core.ListenerRegistry;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.core.member.MemberKey;
import com.a2i.sim.core.Registration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class ListenersCommand implements CommandLine {

    @Override
    public String getCommand() {
        return "listeners";
    }

    @Override
    public void execute(String... args) {
        StringBuilder buff = new StringBuilder();

        Map<String, List<Member>> topics = new HashMap<>();
        for(Registration reg : ListenerRegistry.INSTANCE.getAllRegistrations()) {
            if(topics.get(reg.getTopic()) == null) {
                topics.put(reg.getTopic(), new ArrayList<Member>());
            }
            topics.get(reg.getTopic()).add(reg.getMember());
        }

        for(String topic : topics.keySet()) {
            buff.append("\n").append(topic).append(":").append("\n");
            for(Member member : topics.get(topic)) {
                buff.append("    ").append(MemberKey.getKey(member)).append("\n");
            }
        }

        System.out.println(buff.toString());
    }
}
