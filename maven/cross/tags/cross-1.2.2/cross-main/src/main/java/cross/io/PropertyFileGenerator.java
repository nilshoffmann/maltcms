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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.ParseException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.LoggerFactory;

import cross.annotations.AnnotationInspector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

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
     * Creates a property object for the given class, containing
     * those fields, which are annotated by {@Configurable}.
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
        //TODO add maltcms-datastructures -> PublicMemberGetters support
        return new PropertiesConfiguration();
    }

    public static void main(String[] args) {
        Options options = new Options();

        // add t option
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
            for (String provider:providers) {
                createProperties(provider, basedir);
            }
        } catch (ParseException ex) {
            Logger.getLogger(PropertyFileGenerator.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
}
