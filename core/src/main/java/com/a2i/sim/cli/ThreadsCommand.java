/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class ThreadsCommand implements CommandLine {

    @Override
    public String getCommand() {
        return "threads";
    }

    @Override
    public void execute(String... args) {
        MemCommand.printOSStats();
        printThreadStats();
    }

    private void printThreadStats() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        StringBuilder stats = new StringBuilder();
        
        stats.append("\nThreads:\n");
        for(long id : threadBean.getAllThreadIds()) {
            stats.append("    Name: ").append(threadBean.getThreadInfo(id).getThreadName()).append("\n");
            stats.append("        State: ").append(threadBean.getThreadInfo(id).getThreadState()).append("\n");
            stats.append("        Blocked Time: ").append(threadBean.getThreadInfo(id).getBlockedTime()).append(" ms\n");
            stats.append("        Wait Time: ").append(threadBean.getThreadInfo(id).getWaitedTime()).append(" ms\n");
            stats.append("        Lock Owner: ").append(threadBean.getThreadInfo(id).getLockOwnerName()).append("\n\n");
        }

        System.out.println(stats.toString());
    }
}
