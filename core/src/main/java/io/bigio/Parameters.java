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

package io.bigio;

import io.bigio.core.OperatingSystem;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This singleton class manages all of the configurable parameters. Configuration
 * files are loaded from the 'config' directory. Any property in the directory
 * structure that ends with '.properties' will be loaded by this class and 
 * their contents will be available through this API.
 * 
 * @author Andy Trimble
 */
public enum Parameters {
    INSTANCE;
    
    
    private final Logger LOG;
    private final int MAX_DEPTH;
    private final Properties properties;
    private final FileSystem fileSystem;
    private final Path configDir;

    private static OperatingSystem os;

    /**
     * Protected constructor.
     */
    Parameters() {
        LOG = LoggerFactory.getLogger(Parameters.class);
        MAX_DEPTH = 10;
        properties = new Properties();
        fileSystem = FileSystems.getDefault();
        configDir = fileSystem.getPath("config");
    
        init();
    }

    /**
     * Get a property.
     * 
     * @param name the name of the property.
     * @return the property value, or null if the property does not exist.
     */
    public String getProperty(String name) {
        return properties.getProperty(name);
    }
    
    /**
     * Get a property. If the property doesn't exist, the default value will
     * be returned.
     * 
     * @param name the name of the property.
     * @param defaultValue the default value of the property.
     * @return the value should the property exist, or the default property 
     * if it does not.
     */
    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    /**
     * Get the operating system.
     * 
     * @return the operating system.
     */
    public OperatingSystem currentOS() {
        return os;
    }

    /**
     * Load the configuration.
     */
    private void init() {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");

        if(osName.contains("Windows")) {
            if(osArch.contains("amd64")) {
                os = OperatingSystem.WIN_64;
            } else {
                os = OperatingSystem.WIN_32;
            }
        } else if(osName.contains("Linux")) {
            if(osArch.contains("amd64")) {
                os = OperatingSystem.LINUX_64;
            } else {
                os = OperatingSystem.LINUX_32;
            }
        } else {
            if(osArch.contains("amd64")) {
                os = OperatingSystem.MAC_64;
            } else {
                os = OperatingSystem.MAC_32;
            }
        }
        
        Set<FileVisitOption> options = new TreeSet<>();
        options.add(FileVisitOption.FOLLOW_LINKS);

        try {
            Files.walkFileTree(configDir, options, MAX_DEPTH, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(file.getFileName().toString().endsWith("properties")) {
                        LOG.debug("Loading configuration file '" + file.toString() + "'");

                        try (BufferedReader in = Files.newBufferedReader(file, Charset.defaultCharset())) {
                            properties.load(in);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });

            for(Entry<Object, Object> entry : properties.entrySet()) {
                System.getProperties().setProperty(entry.getKey().toString(), entry.getValue().toString());
            }

            for(Entry<Object, Object> entry : System.getProperties().entrySet()) {
                properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
            }

        } catch(IOException ex) {
            LOG.error("Error while loading configuration.", ex);
        }
    }
}
