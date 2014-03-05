/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.core.ClusterService;
import com.a2i.sim.core.member.MemberKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class WhoAmI implements CommandLine {

    @Autowired
    private ClusterService cluster;

    @Override
    public String getCommand() {
        return "whoami";
    }

    @Override
    public void execute(String... args) {
        System.out.println(MemberKey.getKey(cluster.getMe()));
    }
}
