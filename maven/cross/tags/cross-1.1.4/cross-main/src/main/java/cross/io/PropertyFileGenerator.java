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
 * $Id$
 */
package cross.io;

import java.io.File;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.LoggerFactory;

import cross.annotations.AnnotationInspector;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class PropertyFileGenerator {

    /**
     * Creates a property file for the given class, containing
     * those fields, which are annotated by {@Configurable}.
     * @param className
     * @param basedir
     */
    public static void createProperties(String className, File basedir) {
        Class<?> c;
        try {
            c = PropertyFileGenerator.class.getClassLoader().loadClass(className);
            LoggerFactory.getLogger(PropertyFileGenerator.class).info("Class: {}", c.getName());
            Collection<String> keys = AnnotationInspector.getRequiredConfigKeys(c);

            if (!keys.isEmpty()) {
                try {
                    PropertiesConfiguration pc = new PropertiesConfiguration(new File(basedir, c.getName()));
                    for (String key : keys) {
                        pc.addProperty(key, AnnotationInspector.getDefaultValueFor(c, key));
                    }
                } catch (ConfigurationException e) {
                    LoggerFactory.getLogger(PropertyFileGenerator.class).warn("{}", e.getLocalizedMessage());
                }
            } else {
                LoggerFactory.getLogger(PropertyFileGenerator.class).info(
                        "Could not find annotated configuration keys for class {}!",
                        c.getName());
            }
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(PropertyFileGenerator.class).warn("{}", e.getLocalizedMessage());
        }
    }
}
