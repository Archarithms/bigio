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
import io.bigio.Initialize;
import io.bigio.Inject;
import io.bigio.Parameters;
import io.bigio.Starter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The component that handles the CLI of BigIO.
 * 
 * @author Andy Trimble
 */
@Component
public class CommandLineInterface extends Thread {

    private ConsoleReader reader = null;

    @Inject
    private final List<CommandLine> commands = new ArrayList<>();

    private final static Logger LOG = LoggerFactory.getLogger(CommandLineInterface.class);

    /**
     * Start the command line interface thread.
     */
    @Initialize
    public void init() {
        start();
    }

    /**
     * The thread entry point.
     */
    @Override
    public void run() {
        // Set up the terminal configuration
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
