/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package com.a2i.bigio.cli;

import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import com.a2i.bigio.Inject;
import com.a2i.bigio.core.ListenerRegistry;
import com.a2i.bigio.core.Registration;
import com.a2i.bigio.core.member.Member;
import com.a2i.bigio.core.member.MemberKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the "listeners" CLI command. This command will print all of the
 * registered listeners in a BigIO cluster.
 * 
 * @author Andy Trimble
 */
@Component
public class ListenersCommand implements CommandLine {

    @Inject
    private ListenerRegistry registry;

    @Override
    public String getCommand() {
        return "listeners";
    }

    @Override
    public void execute(String... args) {
        StringBuilder buff = new StringBuilder();

        Map<String, List<Member>> topics = new HashMap<>();
        for(Registration reg : registry.getAllRegistrations()) {
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
