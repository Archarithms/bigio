/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.cli;

import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import com.a2i.bigio.Inject;
import com.a2i.bigio.core.ClusterService;
import com.a2i.bigio.core.member.MemberKey;

/**
 *
 * @author atrimble
 */
@Component
public class WhoAmI implements CommandLine {

    @Inject
    private ClusterService cluster;

    @Override
    public String getCommand() {
        return "whoami";
    }

    @Override
    public void execute(String... args) {
        System.out.println(MemberKey.getKey(cluster.getMe()));
    }

    @Override
    public String help() {
        return "Prints information on this cluster node.";
    }
}
