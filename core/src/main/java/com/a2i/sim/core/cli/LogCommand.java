/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import com.a2i.sim.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class LogCommand implements CommandLine {

    private static final Logger LOG = LoggerFactory.getLogger(LogCommand.class);

    @Override
    public String getCommand() {
        return "log";
    }

    @Override
    public void execute(String... args) {
        if (args.length < 2) {
            System.out.println("Usage: log <all|trace|debug|info|warn|error|off>");
        } else {
            setLoggingLevel(args[1]);
        }
    }
    
    private void setLoggingLevel(String level) {

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

        LOG.info("Setting log level to '" + level + "'");

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
}
