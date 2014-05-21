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
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

/**
 * This is the "mem" CLI command. This command will print information on current
 * memory usage.
 * 
 * @author Andy Trimble
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
