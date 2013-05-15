/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io;

import cross.annotations.AnnotationInspector;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Deprecated
public class PropertyFileGenerator {

    /**
     * Creates a property file for the given class, containing those fields,
     * which are annotated by {@link cross.annotations.Configurable}.
     * @param className
     * @param basedir
     */
    public static void createProperties(String className, File basedir) {
        Class<?> c;
        try {
            c = PropertyFileGenerator.class.getClassLoader().loadClass(className);
            LoggerFactory.getLogger(PropertyFileGenerator.class).info(
                    "Class: {}", c.getName());
            PropertiesConfiguration pc = createProperties(c);
            if (!basedir.exists()) {
                basedir.mkdirs();
            }
            try {
                pc.save(new File(basedir, c.getSimpleName() + ".properties"));
            } catch (ConfigurationException ex) {
                LoggerFactory.getLogger(PropertyFileGenerator.class).warn("{}",
                        ex.getLocalizedMessage());
            }
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(PropertyFileGenerator.class).warn("{}", e.
                    getLocalizedMessage());
        }
    }

    /**
     * Creates a property object for the given class, containing those fields,
     * which are annotated by {
     *
     * @Configurable}.
     * @param c
     */
    public static PropertiesConfiguration createProperties(Class<?> c) {
        Collection<String> keys = AnnotationInspector.getRequiredConfigKeys(c);
        if (!keys.isEmpty()) {
            PropertiesConfiguration pc = new PropertiesConfiguration();
            for (String key : keys) {
                pc.addProperty(key, AnnotationInspector.getDefaultValueFor(c,
                        key));
            }
            return pc;
        } else {
            LoggerFactory.getLogger(PropertyFileGenerator.class).info(
                    "Could not find annotated configuration keys for class {}!",
                    c.getName());
        }
        return new PropertiesConfiguration();
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("f", true, "base directory for output of files");
        Option provOptions = new Option("p", true,
                "Comma separated list of provider classes to create Properties for");
        provOptions.setRequired(true);
        provOptions.setValueSeparator(',');
        options.addOption(provOptions);
        CommandLineParser parser = new PosixParser();
        HelpFormatter hf = new HelpFormatter();
        try {
            File basedir = null;
            List<String> providers = Collections.emptyList();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("f")) {
                basedir = new File(cmd.getOptionValue("f"));
            } else {
                hf.printHelp(
                        "java -cp maltcms.jar " + PropertyFileGenerator.class,
                        options);
            }
            if (cmd.hasOption("p")) {
                String[] str = cmd.getOptionValues("p");
                providers = Arrays.asList(str);
            } else {
                hf.printHelp(
                        "java -cp maltcms.jar " + PropertyFileGenerator.class,
                        options);
            }
            for (String provider : providers) {
                createProperties(provider, basedir);
            }
        } catch (ParseException ex) {
            Logger.getLogger(PropertyFileGenerator.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
