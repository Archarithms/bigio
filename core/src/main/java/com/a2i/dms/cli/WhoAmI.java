/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.cli;

import com.a2i.dms.CommandLine;
import com.a2i.dms.Component;
import com.a2i.dms.Inject;
import com.a2i.dms.core.ClusterService;
import com.a2i.dms.core.member.MemberKey;

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
