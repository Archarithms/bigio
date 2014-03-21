/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim;

import com.a2i.sim.core.ClusterService;
import com.a2i.sim.cli.CommandLineInterface;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This is the main entry point for A2I Sim.
 * 
 * @author Andy Trimble
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Starter implements CommandLineRunner {

    private static final boolean MONITOR_THREAD_CONTENTION = true;

    @Autowired
    private ClusterService cluster;

    @Autowired
    private CommandLineInterface cli;

    @Autowired
    private Speaker speaker;
    
    private static final Logger LOG = LoggerFactory.getLogger(Starter.class);

    public Starter() {
        if(MONITOR_THREAD_CONTENTION) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(MONITOR_THREAD_CONTENTION);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if(args.length > 0 && args[0].equals("interactive")) {
            cli.init();
        }
    }

    public static void exit() {
        LOG.info("Goodbye");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        Parameters.INSTANCE.currentOS(); // Just to load the properties
        ConfigurableApplicationContext context = SpringApplication.run(Starter.class, args);
    }
}
