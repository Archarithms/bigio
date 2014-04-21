/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms;

import com.a2i.dms.core.ClusterService;
import com.a2i.dms.core.Container;
import com.a2i.dms.core.MCDiscovery;
import com.a2i.dms.core.member.MemberHolder;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main entry point for A2I Sim.
 * 
 * @author Andy Trimble
 */
public class Starter {

    private static final boolean MONITOR_THREAD_CONTENTION = true;

    private static final Logger LOG = LoggerFactory.getLogger(Starter.class);

    public Starter() {
        if(MONITOR_THREAD_CONTENTION) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(MONITOR_THREAD_CONTENTION);
        }

//        Starter.bootstrap();
    }

    /**
     * Bootstrap the system and return a Speaker object.
     * 
     * @return an initialized speaker object.
     */
    public static Speaker bootstrap() {
        Speaker speaker = new Speaker();
        ClusterService cluster = new ClusterService();
        MemberHolder memberHolder = new MemberHolder();
        MCDiscovery mc = new MCDiscovery();
        mc.setMemberHolder(memberHolder);
        cluster.setMulticastDiscovery(mc);
        cluster.setMemberHolder(memberHolder);
        speaker.setCluster(cluster);
        speaker.init();
        return speaker;
    }

    public static void exit() {
        LOG.info("Goodbye");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        Parameters.INSTANCE.currentOS(); // Just to load the properties
        Container.INSTANCE.scan();
        Starter starter = new Starter();
    }
}
