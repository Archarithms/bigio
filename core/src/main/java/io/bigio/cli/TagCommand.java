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

package io.bigio.cli;

import io.bigio.CommandLine;
import io.bigio.Component;
import io.bigio.Inject;
import io.bigio.core.ClusterService;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberHolder;
import io.bigio.core.member.MemberKey;

/**
 * This is the "tag" CLI command. This command will display/modify member tags.
 * 
 * @author Andy Trimble
 */
@Component
public class TagCommand implements CommandLine {

    private static final String USAGE = "Usage: tag [add <key> <value> | remove <key> | list]";

    @Inject
    private ClusterService cluster;

    @Inject
    private MemberHolder memberHolder;

    /**
     * Get the command string.
     * 
     * @return the command.
     */
    @Override
    public String getCommand() {
        return "tag";
    }

    /**
     * Execute the command.
     * 
     * @param args the arguments to the command (if any).
     */
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

    /**
     * Return the help/description string for display.
     * 
     * @return the help/description string
     */
    @Override
    public String help() {
        return "Sets or displays tag information. " + USAGE;
    }
}
