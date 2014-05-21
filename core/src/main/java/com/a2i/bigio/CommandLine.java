/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio;

/**
 * This interface defines a command inside the CLI.
 * 
 * @author Andy Trimble
 */
public interface CommandLine {
    /**
     * Get the command to match on the command line.
     * 
     * @return the command name.
     */
    public String getCommand();

    /**
     * Execute the command.
     * 
     * @param args the set of arguments provided from the command line.
     */
    public void execute(String... args);

    /**
     * Get the help message.
     * 
     * @return the help message.
     */
    public String help();
}
