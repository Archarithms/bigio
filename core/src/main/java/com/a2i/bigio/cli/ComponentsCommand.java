/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.cli;

import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import com.a2i.bigio.core.Container;

/**
 *
 * @author atrimble
 */
@Component
public class ComponentsCommand implements CommandLine {

    @Override
    public String getCommand() {
        return "components";
    }

    @Override
    public void execute(String... args) {
        for(Class<?> cl : Container.INSTANCE.getComponents()) {
            System.out.println(cl.getName());
        }
    }

    @Override
    public String help() {
        return "Lists the components running in this container";
    }
    
}
