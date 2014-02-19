/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import com.a2i.sim.core.ClusterService;
import java.lang.management.ManagementFactory;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
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
 *
 * @author atrimble
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Starter implements CommandLineRunner {

    private static final boolean MONITOR_THREAD_CONTENTION = true;

    @Autowired
    private ClusterService cluster;
    
    private static final Logger LOG = LoggerFactory.getLogger(Starter.class);

    public Starter() {
        if(MONITOR_THREAD_CONTENTION) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(MONITOR_THREAD_CONTENTION);
        }
    }

    public static void setLoggingLevel(String level) {

        ch.qos.logback.classic.Logger rootLogger = 
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        ThresholdFilter threshold = null;
            
        for(Filter<ILoggingEvent> filter : rootLogger.getAppender("CONSOLE").getCopyOfAttachedFiltersList()) {
            if(filter instanceof ThresholdFilter) {
                threshold = (ThresholdFilter)filter;
                break;
            }
        }

        if(threshold == null) {
            return;
        }

        if(level.equalsIgnoreCase("all")) {
            threshold.setLevel(Level.ALL.levelStr);
        } else if(level.equalsIgnoreCase("trace")) {
            threshold.setLevel(Level.TRACE.levelStr);
        } else if(level.equalsIgnoreCase("debug")) {
            threshold.setLevel(Level.DEBUG.levelStr);
        } else if(level.equalsIgnoreCase("info")) {
            threshold.setLevel(Level.INFO.levelStr);
        } else if(level.equalsIgnoreCase("warn")) {
            threshold.setLevel(Level.WARN.levelStr);
        } else if(level.equalsIgnoreCase("error")) {
            threshold.setLevel(Level.ERROR.levelStr);
        } else if(level.equalsIgnoreCase("off")) {
            threshold.setLevel(Level.OFF.levelStr);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Speak easy my friends");

        if(args.length > 0 && args[0].equals("interactive")) {

            switch(Parameters.INSTANCE.currentOS()) {
                case WIN_64:
                case WIN_32:
                    TerminalFactory.configure(TerminalFactory.Type.WINDOWS);
                    break;
                case LINUX_64:
                case LINUX_32:
                    TerminalFactory.configure(TerminalFactory.Type.UNIX);
                    break;
                case MAC_64:
                case MAC_32:
                    TerminalFactory.configure(TerminalFactory.Type.UNIX);
                    break;
                default:
                    LOG.error("Cannot determine operating system. Cluster cannot form.");
            }

            ConsoleReader reader = new ConsoleReader();

            reader.setHandleUserInterrupt(true);
            reader.setPrompt("speak> ");

            String line;
            PrintWriter out = new PrintWriter(reader.getOutput());

            try {
                while ((line = reader.readLine()) != null) {
                    if(line.equals("members")) {
                        cluster.members();
                    } else if(line.contains("join")) {
                        String[] arr = line.split("\\s+");
                        if(arr.length < 2) {
                            System.out.println("Usage: join <some ip:port>");
                        } else {
                            cluster.join(arr[1]);
                        }
                    } else if(line.equals("leave")) {
                        cluster.leave();
                    } else if(line.equals("mem")) {
                        printOSStats();
                        printMemStats();
                    } else if(line.contains("log")) {
                        String[] arr = line.split("\\s+");
                        if(arr.length < 2) {
                            System.out.println("Usage: log <all|trace|debug|info|warn|error|off>");
                        } else {
                            setLoggingLevel(arr[1]);
                        }
                    } else if(line.equals("threads")) {
                        printOSStats();
                        printThreadStats();
                    } else if(line.equals("gc")) {
                        System.gc();
                    } else if(!"".equals(line)) {
                        out.println("    \"" + line + "\"");
                        out.flush();
                    }

                    if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                        exit();
                    }
                }
            } catch(UserInterruptException ex) {
                exit();
            }
        }
    }

    private void printOSStats() {
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

    private void exit() {
        LOG.info("Goodbye");
        System.exit(0);
    }
    
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(Starter.class, args);
    }
}
