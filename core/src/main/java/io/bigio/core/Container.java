/*
 * Copyright (c) 2015, Archarithms Inc.
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

package io.bigio.core;

import io.bigio.CommandLine;
import io.bigio.Component;
import io.bigio.Initialize;
import io.bigio.Inject;
import io.bigio.Parameters;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the BigIO container implementation.
 * 
 * @author Andy Trimble
 */
public enum Container {
    INSTANCE;

    private static final String DEFAULT_COMPONENT_DIRECTORY = "components";
    private static final String COMPONENT_DIRECTORY_PROPERTY = "io.bigio.componentDir";
    
    private static final String DEFAULT_BIN_DIRECTORY = "bin";
    private static final String BIN_DIRECTORY_PROPERTY = "io.bigio.binDir";

    private static final String COMPONENT_PROPERTY = "io.bigio.components";

    private String componentDir;
    private String binDir;

    private Reflections reflections;

    private final List<Class<?>> components = new ArrayList<>();
    private final List<Field> injections = new ArrayList<>();
    private final Map<Class<?>, Method> initializations = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, List<Field>> dependencies = new HashMap<>();
    private final Map<Class<?>, List<Field>> multipleDependencies = new HashMap<>();
    private final List<Class<?>> toInstantiate = new ArrayList<>();
    private final List<Class<?>> satisfied = new ArrayList<>();
    
    private static final Logger LOG = LoggerFactory.getLogger(Container.class);
    
    /**
     * Scan for components.
     */
    public void scan() {
        loadProperties();
        loadJars();
        buildDependencyGraph();
        instantiateComponents();
        inject();
    }

    /**
     * Get the managed instance of a class.
     * 
     * @param <T> a class type.
     * @param clazz a class.
     * @return the managed instance of the class.
     */
    public <T> T getInstance(Class<T> clazz) {
        return (T)instances.get(clazz);
    }

    /**
     * Get the managed components.
     * 
     * @return the set of managed components.
     */
    public Set<Class<?>> getComponents() {
        return instances.keySet();
    }

    private void loadProperties() {
        componentDir = Parameters.INSTANCE.getProperty(COMPONENT_DIRECTORY_PROPERTY, DEFAULT_COMPONENT_DIRECTORY);
        binDir = Parameters.INSTANCE.getProperty(BIN_DIRECTORY_PROPERTY, DEFAULT_BIN_DIRECTORY);

        String toCreate = Parameters.INSTANCE.getProperty(COMPONENT_PROPERTY);
        if(toCreate != null && !"".equals(toCreate)) {
            String[] comps = toCreate.split(",");
            for(String cl : comps) {
                try {
                    toInstantiate.add(Class.forName(cl));
                } catch (ClassNotFoundException ex) {
                    LOG.warn("Could not find class '" + cl + "'");
                }
            }

            // Hack: Make sure all CLI commands are loaded
            Reflections cliReflect = new Reflections("io.bigio.cli");
            Set<Class<? extends CommandLine>> clis = cliReflect.getSubTypesOf(CommandLine.class);
            toInstantiate.addAll(clis);
        }
    }

    private void loadJars() {
        
        List<URL> urls = new ArrayList<>();
        FileSystem fileSystem = FileSystems.getDefault();

        Path componentPath = fileSystem.getPath(componentDir);
        Path binPath = fileSystem.getPath(binDir);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(binPath)) {
            for (Path path : directoryStream) {
                urls.add(path.toUri().toURL());
            }
        } catch (IOException ex) {
            LOG.warn("Error loading jars in '" + binPath + "'");
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(componentPath)) {
            for (Path path : directoryStream) {
                urls.add(path.toUri().toURL());
            }
        } catch (IOException ex) {
            LOG.warn("Error loading jars in '" + componentPath + "'");
        }
        
        Configuration config = new ConfigurationBuilder()
                .setUrls(urls)
                .setScanners(
                        new TypeAnnotationsScanner(), 
                        new FieldAnnotationsScanner(), 
                        new MethodAnnotationsScanner(),
                        new SubTypesScanner());
        reflections = new Reflections(config);
    }

    private void buildDependencyGraph() {
        components.addAll(reflections.getTypesAnnotatedWith(Component.class));
        injections.addAll(reflections.getFieldsAnnotatedWith(Inject.class));

        reflections.getMethodsAnnotatedWith(Initialize.class).stream().forEach((init) -> {
            initializations.put(init.getDeclaringClass(), init);
        });
        
        injections.stream().forEach((inject) -> {
            if(inject.getType().isAssignableFrom(List.class)) {
                if(multipleDependencies.get(inject.getDeclaringClass()) == null) {
                    multipleDependencies.put(inject.getDeclaringClass(), new ArrayList<>());
                }
                multipleDependencies.get(inject.getDeclaringClass()).add(inject);
            } else {
                if(dependencies.get(inject.getDeclaringClass()) == null) {
                    dependencies.put(inject.getDeclaringClass(), new ArrayList<>());
                }
                dependencies.get(inject.getDeclaringClass()).add(inject);
            }
        });
    }

    private void instantiateComponents() {
        if(toInstantiate.isEmpty()) {
            components.stream().forEach((cl) -> {
                try {
                    if(LOG.isTraceEnabled()) {
                        LOG.trace("Instantiating " + cl.getName());
                    }
                    instances.put(cl, cl.newInstance());
                    if(!dependencies.containsKey(cl) && initializations.containsKey(cl)) {
                        try {
                            initializations.get(cl).invoke(instances.get(cl));
                        } catch (IllegalAccessException ex) {
                            LOG.error("Illegal access", ex);
                        } catch (IllegalArgumentException ex) {
                            LOG.error("Illegal argument", ex);
                        } catch (InvocationTargetException ex) {
                            LOG.error("Invocation Target Exception", ex);
                        }
                    }
                } catch (InstantiationException ex) {
                    LOG.error("Error instantiating class " + cl.getName(), ex);
                } catch (IllegalAccessException ex) {
                    LOG.error("Illegal access", ex);
                }
            });
        } else {
            toInstantiate.stream().map((cl) -> {
                if(LOG.isTraceEnabled()) {
                    LOG.trace("Instantiating " + cl.getName());
                }
                return cl;
            }).map((cl) -> {
                instantiateTree(cl);
                return cl;
            }).filter((cl) -> (!dependencies.containsKey(cl))).forEach((cl) -> {
                try {
                    initializations.get(cl).invoke(instances.get(cl));
                } catch (IllegalAccessException ex) {
                    LOG.error("Illegal access", ex);
                } catch (IllegalArgumentException ex) {
                    LOG.error("Illegal argument", ex);
                } catch (InvocationTargetException ex) {
                    LOG.error("Invocation Target Exception", ex);
                }
            });
        }
    }

    private void instantiateTree(Class<?> clazz) {
        if(instances.get(clazz) == null) {
            try {
                if(LOG.isTraceEnabled()) {
                    LOG.trace("Instantiating " + clazz.getName());
                }
                instances.put(clazz, clazz.newInstance());
            } catch (InstantiationException ex) {
                LOG.error("Error instantiating class " + clazz.getName(), ex);
            } catch (IllegalAccessException ex) {
                LOG.error("Illegal access", ex);
            }
        } else {
            return;
        }

        if(dependencies.containsKey(clazz)) {
            dependencies.get(clazz).stream().forEach((field) -> {
                components.stream().filter((cl) -> (field.getType().isAssignableFrom(cl))).forEach((cl) -> {
                    instantiateTree(cl);
                });
            });
        }

        if(multipleDependencies.containsKey(clazz)) {
            multipleDependencies.get(clazz).stream().map((field) -> (ParameterizedType) field.getGenericType()).map((listType) -> (Class<?>) listType.getActualTypeArguments()[0]).forEach((listClass) -> {
                components.stream().filter((cl) -> (listClass.isAssignableFrom(cl) && toInstantiate.contains(cl))).forEach((cl) -> {
                    instantiateTree(cl);
                });
            });
        }
    }

    private void inject(Class<?> parent) {
        if(satisfied.contains(parent)) {
            return;
        }

        if(!dependencies.containsKey(parent)) {
            satisfied.add(parent);
            return;
        }

        for(Field field : dependencies.get(parent)) {
            inject(field.getType());

            for(Object obj : instances.values()) {
                if(field.getType().isAssignableFrom(obj.getClass())) {
                    try {
                        if(instances.get(parent) != null) {
                            field.setAccessible(true);
                            field.set(instances.get(parent), obj);
                        }
                    } catch (IllegalArgumentException ex) {
                        LOG.error("Illegal argument in injection", ex);
                    } catch (IllegalAccessException ex) {
                        LOG.error("Illegal access", ex);
                    }
                }
            }
        }

        satisfied.add(parent);
        if(initializations.containsKey(parent) && instances.containsKey(parent)) {
            try {
                initializations.get(parent).invoke(instances.get(parent));
            } catch (IllegalAccessException ex) {
                LOG.error("Illegal access", ex);
            } catch (IllegalArgumentException ex) {
                LOG.error("Illegal argument", ex);
            } catch (InvocationTargetException ex) {
                LOG.error("Invocation Target Exception", ex);
            }
        }
    }

    private void inject() {
        dependencies.keySet().stream().forEach((parent) -> {
            inject(parent);
        });

        for(Class<?> parent : multipleDependencies.keySet()) {
            for(Field field : multipleDependencies.get(parent)) {
                for(Object obj : instances.values()) {
                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];

                    if(listClass.isAssignableFrom(obj.getClass())) {
                        try {
                            if(instances.get(parent) != null) {
                                field.setAccessible(true);
                                if(field.get(instances.get(parent)) == null) {
                                    field.set(instances.get(parent), new ArrayList());
                                }
                                Method addMethod = ArrayList.class.getMethod("add", Object.class);
                                addMethod.invoke(field.get(instances.get(parent)), obj);
                            }
                        } catch(NoSuchMethodException ex) {
                            LOG.error("No such method", ex);
                        } catch(IllegalAccessException ex) {
                            LOG.error("Illegal access", ex);
                        } catch (IllegalArgumentException ex) {
                            LOG.error("Illegal argument", ex);
                        } catch (InvocationTargetException ex) {
                            LOG.error("Invocation target exception", ex);
                        }
                    }
                }
            }
        }
    }
}
