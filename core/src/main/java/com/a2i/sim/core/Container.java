/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.core;

import com.a2i.sim.Component;
import com.a2i.sim.Initialize;
import com.a2i.sim.Inject;
import com.a2i.sim.Parameters;
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
public class Container {

    private static final String DEFAULT_COMPONENT_DIRECTORY = "components";
    private static final String COMPONENT_DIRECTORY_PROPERTY = "com.a2i.dms.componentDir";
    
    private static final String DEFAULT_BIN_DIRECTORY = "bin";
    private static final String BIN_DIRECTORY_PROPERTY = "com.a2i.dms.binDir";

    private static final String COMPONENT_PROPERTY = "com.a2i.dms.components";

    private String componentDir;
    private String binDir;

    private Reflections reflections;

    private final List<Class<?>> components = new ArrayList<>();
    private final List<Field> injections = new ArrayList<>();
    private final List<Method> initializations = new ArrayList<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, List<Field>> dependencies = new HashMap<>();
    private final Map<Class<?>, List<Field>> multipleDependencies = new HashMap<>();
    private final List<Class<?>> toInstantiate = new ArrayList<>();
    
    private static final Logger LOG = LoggerFactory.getLogger(Container.class);
    
    public void scan() {
        loadProperties();
        loadJars();
        buildDependencyGraph();
        instantiateComponents();
        inject();
        initialize();
    }

    public Object getInstance(Class<?> clazz) {
        return instances.get(clazz);
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
                LOG.info("Loading " + path);
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
        initializations.addAll(reflections.getMethodsAnnotatedWith(PostConstruct.class));
        initializations.addAll(reflections.getMethodsAnnotatedWith(Initialize.class));
        
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
                    LOG.info(cl.getName() + " " + field.getType().getName() + " " + field.getType().isAssignableFrom(cl));
                    if(field.getType().isAssignableFrom(cl)) {
                        LOG.info("Found a dependency subgraph " + cl.getName());
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
                    if(listClass.isAssignableFrom(cl)) {
                        instantiateTree(cl);
                    }
                }
            }
        }
    }

    private void inject() {
        for(Class<?> parent : dependencies.keySet()) {
            for(Field field : dependencies.get(parent)) {
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

    private void initialize() {
        for(final Method m : initializations) {
            try {
                if(instances.containsKey(m.getDeclaringClass())) {
                    LOG.info("Initializing " + m.getDeclaringClass().getName());
                    m.invoke(instances.get(m.getDeclaringClass()));
                }
            } catch (IllegalAccessException ex) {
                LOG.error("Illegal access", ex);
            } catch (IllegalArgumentException ex) {
                LOG.error("Illegal argument", ex);
            } catch (InvocationTargetException ex) {
                LOG.error("Invocation Target Exception", ex);
            }
        }
    }
}
