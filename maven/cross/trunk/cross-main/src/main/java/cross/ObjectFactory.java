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

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;

import cross.annotations.AnnotationInspector;
import cross.datastructures.tools.EvalTools;

/**
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class ObjectFactory implements IObjectFactory {

	private final Logger log = Logging.getLogger(ObjectFactory.class);

	private Configuration cfg = new PropertiesConfiguration();

	@Override
	public void configure(final Configuration cfg) {
		this.cfg = new PropertiesConfiguration();
		ConfigurationUtils.copy(cfg, this.cfg);
	}

	public <T> void configureType(final T t) {
		configureType(t, this.cfg);
	}

	private <T> T configureType(final T t, final Configuration cfg) {
		if (t instanceof IConfigurable) {
			if (cfg == null || cfg.isEmpty()) {
				this.log
				        .error(
				                "ObjectFactory's configuration is null or empty! Skipping configuration of {}",
				                t.getClass().getName());
				return t;
			}
			this.log.debug("Instance of type {} is configurable!", t.getClass()
			        .toString());
			final Collection<String> requiredKeys = AnnotationInspector
			        .getRequiredConfigKeys(t);
			this.log.debug("Required keys for class {}", t.getClass());
			this.log.debug("{}", requiredKeys);
			// TODO this is temporary, as long as not all classes use the
			// new
			// Configurable Annotations
			// if (requiredKeys.isEmpty()) {
			this.log.debug("Configuring with full configuration!");
			((IConfigurable) t).configure(cfg);
			// } else {
			// Constrain visibility of configuration to those keys
			// required
			// BaseConfiguration bc = new BaseConfiguration();
			// for (String key : requiredKeys) {
			// bc.setProperty(key, getConfiguration().getProperty(key));
			// }
			// ArrayFactory.getInstance().log.info(
			// "Configuring with partial configuration {}"
			// ,ConfigurationUtils.toString(bc));
			// ((IConfigurable) t).configure(bc);
			// }
			// ArrayFactory.getInstance().log.info(requiredKeys.toString());
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
	public <T> T instantiate(final Class<T> c) {
		return configureType(instantiateType(c), this.cfg);
	}

	private <T> T instantiateType(final Class<T> c) {
		try {
			return c.newInstance();
		} catch (final InstantiationException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final IllegalAccessException e) {
			this.log.error(e.getLocalizedMessage());
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
	public <T> T instantiate(final String classname, final Class<T> cls,
	        final String configurationFile) {
		CompositeConfiguration cc = new CompositeConfiguration();
		try {

			cc.addConfiguration(new PropertiesConfiguration(configurationFile));
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
			this.log.debug("Loading class {}", name);
			try {
				cls = this.getClass().getClassLoader().loadClass(name);
				EvalTools.notNull(cls, ObjectFactory.class);
				return cls;
			} catch (final NullPointerException npe) {
				this.log.error("Could not load class with name " + name
				        + "! Check for typos!");
			}
		} catch (final ClassNotFoundException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return cls;
	}

}
