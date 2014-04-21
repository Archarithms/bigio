/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.cli;

import com.a2i.dms.CommandLine;
import com.a2i.dms.Component;
import com.a2i.dms.core.Container;

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
