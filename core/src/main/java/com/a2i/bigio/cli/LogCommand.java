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

package com.a2i.bigio.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class LogCommand implements CommandLine {

    private static final Logger LOG = LoggerFactory.getLogger(LogCommand.class);

    private static final String USAGE = "Usage: log [all|trace|debug|info|warn|error|off|none]";

    @Override
    public String getCommand() {
        return "log";
    }

    @Override
    public void execute(String... args) {
        if (args.length < 2) {
            System.out.println(USAGE);
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
        } else if(level.equalsIgnoreCase("none")) {
            threshold.setLevel(Level.OFF.levelStr);
        }
    }

    @Override
    public String help() {
        return "Sets the console log level. " + USAGE;
    }
}
