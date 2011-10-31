/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: ObjectFactory.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package cross;

import java.util.Collection;

import java.util.Map;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import cross.annotations.AnnotationInspector;
import cross.applicationContext.DefaultApplicationContextFactory;
import cross.datastructures.tools.EvalTools;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
public class ObjectFactory implements IObjectFactory {

    private Configuration cfg = new PropertiesConfiguration();
    private File userConfigLocation = null;
    private ApplicationContext context = null;
    public static final String CONTEXT_LOCATION_KEY = "pipeline.xml";

    @Override
    public void configure(final Configuration cfg) {
        this.cfg = new PropertiesConfiguration();
        ConfigurationUtils.copy(cfg, this.cfg);
        userConfigLocation = (File) this.cfg.getProperty("userConfigLocation");
        String[] contextLocations = null;
        if (cfg.containsKey(CONTEXT_LOCATION_KEY)) {
            log.info("Using user-defined location: {}",cfg.getStringArray(CONTEXT_LOCATION_KEY));
            contextLocations = cfg.getStringArray(CONTEXT_LOCATION_KEY);
        }
        if(contextLocations==null) {
            throw new NullPointerException("No pipeline configuration found! Please define! Example: -c cfg/chroma.properties");
        }
//        } else {
//            String str = new File(System.getProperty(
//                "user.dir"),"cfg/xml/chroma.xml").getAbsolutePath();
//            log.info("Using fallback default location: {}",str);
//            contextLocations = new String[]{str};
////            contextLocations = new String[]{"/cfg/xml/chroma.xml"};
//        }
        log.info("Using context locations: {}",
                Arrays.toString(contextLocations));
        try {
            context = new DefaultApplicationContextFactory(contextLocations).
                    createApplicationContext();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T> void configureType(final T t) {
        configureType(t, this.cfg);
    }

    private <T> T configureType(final T t, final Configuration cfg) {
        if (t instanceof IConfigurable) {
            if (cfg == null || cfg.isEmpty()) {
                log.error(
                        "ObjectFactory's configuration is null or empty! Skipping configuration of {}",
                        t.getClass().getName());
                return t;
            }
            log.debug("Instance of type {} is configurable!", t.getClass().
                    toString());
            final Collection<String> requiredKeys = AnnotationInspector.
                    getRequiredConfigKeys(t);
            log.debug("Required keys for class {}", t.getClass());
            log.debug("{}", requiredKeys);
            log.debug("Configuring with full configuration!");
            ((IConfigurable) t).configure(cfg);
        }
        return t;
    }

    /**
     * Create a new Instance of c, configure automatically, if c is an instance
     * of IConfigurable
     * 
     * @param <T>
     * @param c
     * @return
     */
    @Override
    public <T> T instantiate(final Class<T> c) {
        T t = null;
        if (context != null) {
            try {
                t = context.getBean(c);
                log.info("Retrieved bean {} from context!");
                return t;
            } catch (NoSuchBeanDefinitionException nsbde) {
                log.debug("Could not create bean {} from context! Reason:\n {}",
                        c.getName(), nsbde.getLocalizedMessage());
            } catch (BeansException be) {
                log.debug("Could not create bean {} from context! Reason:\n {}",
                        c.getName(), be.getLocalizedMessage());
            }
        }
        log.info("Using regular configuration mechanism!");
        return configureType(instantiateType(c), this.cfg);
    }

    private <T> T instantiateType(final Class<T> c) {
        try {
            return c.newInstance();
        } catch (final InstantiationException e) {
            log.error(e.getLocalizedMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getLocalizedMessage());
        }
        throw new IllegalArgumentException("Could not instantiate class "
                + c.getName());
    }

    /**
     * Instantiate a class, given by a classname and the class of Type T.
     * 
     * @param <T>
     * @param classname
     * @param cls
     * @return
     */
    @Override
    public <T> T instantiate(final String classname, final Class<T> cls) {
        EvalTools.notNull(classname, "Class name of type " + cls.getName()
                + " was null!", Factory.class);
        final Class<?> c = loadClass(classname);
        final Class<? extends T> t = c.asSubclass(cls);
        return instantiate(t);
    }

    /**
     * Instantiate a class, given a classname and the class of Type t and
     * configure with configuration from configurationFile.
     * 
     * @param <T>
     * @param classname
     * @param cls
     * @param configurationFile
     * @return
     */
    @Override
    public <T> T instantiate(final String classname, final Class<T> cls,
            final String configurationFile) {
        CompositeConfiguration cc = new CompositeConfiguration();
        try {
            File configFileLocation = new File(configurationFile);
            //resolve non-absolute paths
//                        if(!configFileLocation.isAbsolute()) {
//                            configFileLocation = new File(this.userConfigLocation,configFileLocation.getPath());
//                        }
            cc.addConfiguration(new PropertiesConfiguration(configFileLocation.
                    getAbsolutePath()));
        } catch (ConfigurationException e) {
            log.warn(e.getLocalizedMessage());
        }
        cc.addConfiguration(this.cfg);

        return instantiate(classname, cls, cc);

        // throw new IllegalArgumentException("Could not instantiate class "
        // + cls.getName());
    }

    /**
     * Instantiate a class, given a classname and the class of Type t and
     * configure with configuration from config.
     * 
     * @param <T>
     * @param classname
     * @param cls
     * @param config
     * @return
     */
    @Override
    public <T> T instantiate(final String classname, final Class<T> cls,
            final Configuration config) {
        return configureType(instantiate(classname, cls), config);
    }

    /**
     * Load a class by its name. Tries to locate the given class name on the
     * user class path and on the default java class path. Currently only
     * supports loading of classes from local storage.
     * 
     * @param name
     * @return
     */
    protected Class<?> loadClass(final String name) {
        EvalTools.notNull(name, ObjectFactory.class);
        Class<?> cls = null;
        try {
            log.debug("Loading class {}", name);
            try {
                cls = this.getClass().getClassLoader().loadClass(name);
                EvalTools.notNull(cls, ObjectFactory.class);
                return cls;
            } catch (final NullPointerException npe) {
                log.error("Could not load class with name " + name
                        + "! Check for typos!");
            }
        } catch (final ClassNotFoundException e) {
            log.error(e.getLocalizedMessage());
        }
        return cls;
    }

    @Override
    public <T> Map<String, T> getObjectsOfType(Class<T> cls) {
        return context.getBeansOfType(cls);
    }

    @Override
    public <T> T getNamedObject(String name,
            Class<T> cls) {
        return context.getBean(name, cls);
    }
}
