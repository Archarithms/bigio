/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.cli;

import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

/**
 *
 * @author atrimble
 */
@Component
public class MemCommand implements CommandLine {

    @Override
    public String getCommand() {
        return "mem";
    }

    @Override
    public void execute(String... args) {
        printOSStats();
        printMemStats();
    }
    
    protected static void printOSStats() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        StringBuilder stats = new StringBuilder();
        
        stats.append("\nOperating System:\n");
        stats.append("    Arch: ").append(osBean.getArch()).append("\n");
        stats.append("    Name: ").append(osBean.getName()).append("\n");
        stats.append("    Version: ").append(osBean.getVersion()).append("\n");
        stats.append("    Processors: ").append(osBean.getAvailableProcessors()).append("\n");
        stats.append("    Load Avg: ").append(osBean.getSystemLoadAverage()).append("\n");
        
        System.out.println(stats.toString());
    }

    private void printMemStats() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        List<MemoryPoolMXBean> memPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        StringBuilder stats = new StringBuilder();
        
        stats.append("\nMemory:\n");
        stats.append("\n    Heap:\n");
        stats.append("        Initial: ").append(memBean.getHeapMemoryUsage().getInit() / 1024 / 1024).append(" MB\n");
        stats.append("        Max: ").append(memBean.getHeapMemoryUsage().getMax() / 1024 / 1024).append(" MB\n");
        stats.append("        Used: ").append(memBean.getHeapMemoryUsage().getUsed() / 1024 / 1024).append(" MB\n");
        stats.append("        Committed: ").append(memBean.getHeapMemoryUsage().getCommitted() / 1024 / 1024).append(" MB\n");
        stats.append("\n    Non Heap:\n");
        stats.append("        Initial: ").append(memBean.getNonHeapMemoryUsage().getInit() / 1024 / 1024).append(" MB\n");
        stats.append("        Max: ").append(memBean.getNonHeapMemoryUsage().getMax() / 1024 / 1024).append(" MB\n");
        stats.append("        Used: ").append(memBean.getNonHeapMemoryUsage().getUsed() / 1024 / 1024).append(" MB\n");
        stats.append("        Committed: ").append(memBean.getNonHeapMemoryUsage().getCommitted() / 1024 / 1024).append(" MB\n");

        stats.append("\nMemory Pools:\n");
        for(MemoryPoolMXBean bean : memPoolBeans) {
            stats.append("    ").append(bean.getName()).append("\n");
            stats.append("        Type: ").append(bean.getType()).append("\n");
            stats.append("        Initial: ").append(bean.getUsage().getInit() / 1024 / 1024).append(" MB\n");
            stats.append("        Max: ").append(bean.getUsage().getMax() / 1024 / 1024).append(" MB\n");
            stats.append("        Used: ").append(bean.getUsage().getUsed() / 1024).append(" MB\n");
            stats.append("        Committed: ").append(bean.getUsage().getCommitted() / 1024 / 1024).append(" MB\n");
        }

        stats.append("\nGarbage Collection:\n");
        for(GarbageCollectorMXBean bean : gcBeans) {
            stats.append("    Name: ").append(bean.getName()).append("\n");
            stats.append("        Count: ").append(bean.getCollectionCount()).append("\n");
            stats.append("        Time: ").append(bean.getCollectionTime()).append(" ms\n");
        }

        System.out.println(stats.toString());
    }

    @Override
    public String help() {
        return "Prints information about the current state of JVM memory.";
    }
}
