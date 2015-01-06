/*
 * Copyright (c) 2015, Archarithms Inc.
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

package io.bigio.cli;

import io.bigio.CommandLine;
import io.bigio.Component;
import io.bigio.Inject;
import io.bigio.core.ListenerRegistry;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberKey;
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

    /**
     * Get the command string.
     * 
     * @return the command.
     */
    @Override
    public String getCommand() {
        return "listeners";
    }

    /**
     * Execute the command.
     * 
     * @param args the arguments to the command (if any).
     */
    @Override
    public void execute(String... args) {
        StringBuilder buff = new StringBuilder();

        Map<String, List<Member>> topics = new HashMap<>();
        registry.getAllRegistrations().stream().map((reg) -> {
            if(topics.get(reg.getTopic()) == null) {
                topics.put(reg.getTopic(), new ArrayList<>());
            }
            return reg;
        }).forEach((reg) -> {
            topics.get(reg.getTopic()).add(reg.getMember());
        });

        topics.keySet().stream().map((topic) -> {
            buff.append("\n").append(topic).append(":").append("\n");
            return topic;
        }).forEach((topic) -> {
            topics.get(topic).stream().forEach((member) -> {
                buff.append("    ").append(MemberKey.getKey(member)).append("\n");
            });
        });

        System.out.println(buff.toString());
    }

    /**
     * Return the help/description string for display.
     * 
     * @return the help/description string
     */
    @Override
    public String help() {
        return "Displays the current set of registered listeners";
    }
}
