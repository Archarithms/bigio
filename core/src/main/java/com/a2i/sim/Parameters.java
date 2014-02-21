/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim;

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
 *
 * @author atrimble
 */
public enum Parameters {
    INSTANCE;
    
    private final Logger LOG;
    private final int MAX_DEPTH;
    private final Properties properties;
    private final FileSystem fileSystem;
    private final Path configDir;

    private static OperatingSystem os;

    Parameters() {
        LOG = LoggerFactory.getLogger(Parameters.class);
        MAX_DEPTH = 10;
        properties = new Properties();
        fileSystem = FileSystems.getDefault();
        configDir = fileSystem.getPath("config");
    
        init();
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }
    
    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public OperatingSystem currentOS() {
        return os;
    }

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

        } catch(IOException ex) {
            LOG.error("Error while loading configuration.", ex);
        }
    }
}
