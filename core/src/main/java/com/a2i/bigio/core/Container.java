/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.core;

import com.a2i.bigio.CommandLine;
import com.a2i.bigio.Component;
import com.a2i.bigio.Initialize;
import com.a2i.bigio.Inject;
import com.a2i.bigio.Parameters;
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
import javax.annotation.PostConstruct;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public enum Container {
    INSTANCE;

    private static final String DEFAULT_COMPONENT_DIRECTORY = "components";
    private static final String COMPONENT_DIRECTORY_PROPERTY = "com.a2i.bigio.componentDir";
    
    private static final String DEFAULT_BIN_DIRECTORY = "bin";
    private static final String BIN_DIRECTORY_PROPERTY = "com.a2i.bigio.binDir";

    private static final String COMPONENT_PROPERTY = "com.a2i.bigio.components";

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
    
    public void scan() {
        loadProperties();
        loadJars();
        buildDependencyGraph();
        instantiateComponents();
        inject();
    }

    public <T> T getInstance(Class<T> clazz) {
        return (T)instances.get(clazz);
    }

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
            Reflections cliReflect = new Reflections("com.a2i.bigio.cli");
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
                .setScanners(new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner());
        reflections = new Reflections(config);
    }

    private void buildDependencyGraph() {
        components.addAll(reflections.getTypesAnnotatedWith(Component.class));
        injections.addAll(reflections.getFieldsAnnotatedWith(Inject.class));

        for(Method init : reflections.getMethodsAnnotatedWith(PostConstruct.class)) {
            initializations.put(init.getDeclaringClass(), init);
        }
        for(Method init : reflections.getMethodsAnnotatedWith(Initialize.class)) {
            initializations.put(init.getDeclaringClass(), init);
        }
        
        for(Field inject : injections) {
            if(inject.getType().isAssignableFrom(List.class)) {
                if(multipleDependencies.get(inject.getDeclaringClass()) == null) {
                    multipleDependencies.put(inject.getDeclaringClass(), new ArrayList<Field>());
                }
                multipleDependencies.get(inject.getDeclaringClass()).add(inject);
            } else {
                if(dependencies.get(inject.getDeclaringClass()) == null) {
                    dependencies.put(inject.getDeclaringClass(), new ArrayList<Field>());
                }
                dependencies.get(inject.getDeclaringClass()).add(inject);
            }
        }
    }

    private void instantiateComponents() {
        if(toInstantiate.isEmpty()) {
            for(Class<?> cl : components) {
                try {
                    instances.put(cl, cl.newInstance());
                } catch (InstantiationException ex) {
                    LOG.error("Error instantiating class " + cl.getName(), ex);
                } catch (IllegalAccessException ex) {
                    LOG.error("Illegal access", ex);
                }
            }
        } else {
            for(Class<?> cl : toInstantiate) {
                instantiateTree(cl);
            }
        }
    }

    private void instantiateTree(Class<?> clazz) {
        if(instances.get(clazz) == null) {
            try {
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
            for(Field field : dependencies.get(clazz)) {
                for(Class<?> cl : components) {
                    if(field.getType().isAssignableFrom(cl)) {
                        instantiateTree(cl);
                    }
                }
            }
        }

        if(multipleDependencies.containsKey(clazz)) {
            for(Field field : multipleDependencies.get(clazz)) {
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];

                for(Class<?> cl : components) {
                    if(listClass.isAssignableFrom(cl) && toInstantiate.contains(cl)) {
                        instantiateTree(cl);
                    }
                }
            }
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
        for(Class<?> parent : dependencies.keySet()) {
            inject(parent);
        }

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
