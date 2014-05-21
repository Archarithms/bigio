/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.bigio.cli;

import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import com.a2i.bigio.Initialize;
import com.a2i.bigio.Inject;
import com.a2i.bigio.Parameters;
import com.a2i.bigio.Starter;
import com.a2i.bigio.core.ClusterService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andy Trimble
 */
@Component
public class CommandLineInterface extends Thread {

    private ConsoleReader reader = null;

    @Inject
    private ClusterService cluster;

    @Inject
    private final List<CommandLine> commands = new ArrayList<>();

    private final static Logger LOG = LoggerFactory.getLogger(CommandLineInterface.class);

    @Initialize
    public void init() {
        start();
    }

    @Override
    public void run() {
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

        try {
        
            reader = new ConsoleReader();

            reader.setHandleUserInterrupt(true);
            reader.setPrompt("bigio> ");

            String line;

            while ((line = reader.readLine()) != null) {
                boolean found = false;

                String[] args = line.split("\\s+");
                
                if (line.equals("gc")) {
                    found = true;
                    System.gc();
                } else if (line.equals("help")) {
                    found = true;
                    for(CommandLine command : commands) {
                        System.out.println("\n" + command.getCommand() + "\n\t" + command.help());
                    }
                    System.out.println("\ngc\n\tPerforms garbage collection.");
                    System.out.println("\nquit\n\tExits ths system.");
                    System.out.println("\nexit\n\tExits ths system.");
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
        } catch (IOException ex) {
            LOG.error("IO Exception", ex);
        }
    }
}
