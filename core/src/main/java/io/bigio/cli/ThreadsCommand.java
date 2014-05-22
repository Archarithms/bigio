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
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * This is the "threads" CLI command. This command will display a list of
 * the current threads inside the JVM.
 * 
 * @author Andy Trimble
 */
@Component
public class ThreadsCommand implements CommandLine {

    /**
     * Get the command string.
     * 
     * @return the command.
     */
    @Override
    public String getCommand() {
        return "threads";
    }

    /**
     * Execute the command.
     * 
     * @param args the arguments to the command (if any).
     */
    @Override
    public void execute(String... args) {
        MemCommand.printOSStats();
        printThreadStats();
    }

    /**
     * Return the help/description string for display.
     * 
     * @return the help/description string
     */
    @Override
    public String help() {
        return "Prints information on threads in this JVM.";
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
