/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.sim.cli;

import com.a2i.sim.CommandLine;
import com.a2i.sim.Parameters;
import com.a2i.sim.core.ClusterService;
import com.a2i.sim.Starter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Andy Trimble
 */
@Component
public class CommandLineInterface {

    @Autowired
    private ClusterService cluster;

    @Autowired
    private final List<CommandLine> commands = new ArrayList<>();

    private final static Logger LOG = LoggerFactory.getLogger(CommandLineInterface.class);

    public void init() throws IOException {

        switch (Parameters.INSTANCE.currentOS()) {
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
        reader.setPrompt("sim> ");

        String line;
        PrintWriter out = new PrintWriter(reader.getOutput());

        try {
            while ((line = reader.readLine()) != null) {
                boolean found = false;

                String[] args = line.split("\\s+");
                
                if (line.equals("members")) {
                    found = true;
                    cluster.members();
                } else if (line.contains("join")) {
                    found = true;
                    if (args.length < 2) {
                        System.out.println("Usage: join <some ip:port>");
                    } else {
                        cluster.join(args[1]);
                    }
                } else if (line.equals("leave")) {
                    found = true;
                    cluster.leave();
                } else if (line.equals("gc")) {
                    found = true;
                    System.gc();
                }

                for(CommandLine command : commands) {
                    if(args[0].equalsIgnoreCase(command.getCommand())) {
                        found = true;
                        command.execute(args);
                    }
                }

                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    found = true;
                    Starter.exit();
                }

                if(!found && !"".equals(args[0])) {
                    LOG.warn("Command not found '" + args[0] + "'");
                }
            }
        } catch (UserInterruptException ex) {
            Starter.exit();
        }
    }
}
