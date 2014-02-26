/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
