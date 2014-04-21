/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.cli;

import com.a2i.dms.CommandLine;
import com.a2i.dms.Component;
import com.a2i.dms.core.ListenerRegistry;
import com.a2i.dms.core.Registration;
import com.a2i.dms.core.member.Member;
import com.a2i.dms.core.member.MemberKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public String help() {
        return "Displays the current set of registered listeners";
    }
}
