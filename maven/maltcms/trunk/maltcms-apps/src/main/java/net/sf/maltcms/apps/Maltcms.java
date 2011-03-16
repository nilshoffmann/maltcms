/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: Maltcms.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */
package net.sf.maltcms.apps;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import maltcms.ui.wizard.PipelineWizard;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;

import cross.Factory;
import cross.annotations.AnnotationInspector;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.workflow.IWorkflow;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

/**
 * Main Application Hook, starts with setting allowed command-line parameters.
 * Then, if parseCommandLine is called, system configuration (properties) are
 * read, then, the configuration in the default.properties file, within the path
 * is read. Finally, the command-line arguments are evaluated, possibly
 * overriding default options.
 * 
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Maltcms implements Thread.UncaughtExceptionHandler {

    static Maltcms mcms;
    private boolean runGui = false;

    public boolean isRunGui() {
        return this.runGui;
    }

    public void setRunGui(boolean runGui) {
        this.runGui = runGui;
    }

    /**
     * Allow only one running instance of Maltcms (per VM)
     *
     * @return instance of Maltcms.
     */
    public static Maltcms getInstance() {
        if (Maltcms.mcms == null) {
            Maltcms.mcms = new Maltcms();
        }
        return Maltcms.mcms;
    }

    private static void handleRuntimeException(final Logger log,
            final Throwable npe, final ICommandSequence ics) {
        int ecode;
        Maltcms.shutdown(1, log);
        log.error("Caught Throwable, returning to console!");
        log.error(npe.getLocalizedMessage());
        npe.printStackTrace(System.err);
        Maltcms.printBugTrackMessage(log);
        ecode = 1;
        // Save configuration
        Factory.dumpConfig("runtime.properties", ics.getIWorkflow().getStartupDate());
        System.exit(ecode);
    }

    /**
     * Application's main procedure.
     *
     * @param args
     *            what is passed in from the command-line, usually
     */
    public static void main(final String[] args) {
        if (System.getProperty("log4j.configuration") == null || System.getProperty("log4j.configuration").isEmpty()) {
            URL url = Loader.getResource("cfg/log4j.properties");
            PropertyConfigurator.configure(url);
//            System.setProperty("log4j.configuration", "cfg/log4j.properties");
        }
        final Logger log = cross.Logging.getLogger(Maltcms.class);
        final int ecode = 0;
        ICommandSequence cs = null;
        try {
            final Maltcms m = Maltcms.getInstance();
            final CompositeConfiguration cfg = m.parseCommandLine(args);
            if (m.isRunGui()) {
                PipelineWizard.main(args);
            } else {
                log.info("Running Maltcms version {}", cfg.getString("application.version"));
                EvalTools.notNull(cfg, cfg);
                log.info("Configuring Factory");
                Factory.getInstance().configure(cfg);
                // Set up the command sequence
                cs = Factory.getInstance().createCommandSequence();
                long start = System.nanoTime();
                // Evaluate until empty
                try {
                    while (cs.hasNext()) {
                        cs.next();
                    }
                    Maltcms.shutdown(30, log);
                    // Save configuration
                    Factory.dumpConfig("runtime.properties", cs.getIWorkflow().getStartupDate());
                    // Save workflow
                    final IWorkflow iw = cs.getIWorkflow();
                    iw.save();
                    start = Math.abs(System.nanoTime() - start);
                    final float seconds = ((float) start)
                            / ((float) 1000000000);
                    final StringBuilder sb = new StringBuilder();
                    final Formatter formatter = new Formatter(sb);
                    formatter.format(CommandPipeline.NUMBERFORMAT, (seconds));
                    log.info("Runtime of pipeline: {} sec", sb.toString());
                    System.exit(ecode);
                } catch (final Throwable t) {
                    Maltcms.handleRuntimeException(log, t, cs);
                }
            }
        } catch (final Throwable t) {
            Maltcms.handleRuntimeException(log, t, cs);
        }
    }

    public static void printBugTrackMessage(final Logger log) {
        log.error("Please submit a bug report including this output to:");
        log.error("https://sourceforge.net/tracker/?func=add&group_id=251287&atid=1126545");
        log.error("Your report will help improve Maltcms, thank you!");
        log.error("Please attach the following dump of Maltcms' configuration to your report!");
    }

    private static void shutdown(final long seconds, final Logger log) {
        // Shutdown application thread
        try {
            Factory.getInstance().shutdown();
        } catch (final IllegalArgumentException e) {
            // log.warn(e.getLocalizedMessage());
        }
        try {
            Factory.awaitTermination(seconds, TimeUnit.MINUTES);
        } catch (final IllegalArgumentException e) {
        } catch (final InterruptedException e) {
            log.error(e.getLocalizedMessage());
        }
    }
    private final Logger log = cross.Logging.getLogger(this.getClass());
    private static final String licence = "Maltcms is licensed under the terms of the GNU LESSER GENERAL PUBLIC LICENSE (LGPL) Version 3 as of 29 June 2007\nLibraries used by Maltcms can be licensed under different conditions, please consult the licenses directory for more information!";
    private final Options o;

    /**
     * Protected Ctor, override this class, to set your own command line
     * options.
     *
     */
    protected Maltcms() {
        this.o = new Options();
        this.o.addOption(addOption("c", "cfg", "Supply a config-file", false,
                (char) 0, true, false, 0, false, false, 0, "file", false));
        this.o.addOption(addOption(
                "i",
                "input-dir",
                "If input files are given locally, tries to prepend the given directory as base-directory, so you don't have to type the same path all the time",
                false, (char) 0, true, false, 0, false, false, 0,
                "dir", false));
        this.o.addOption(addOption("o", "output-dir",
                "Target directory for all output", false, (char) 0, true,
                false, 0, false, false, 0, "dir", false));
        this.o.addOption(addBooleanOption("r", "recurse",
                "Recurse into input basedir", false));
        this.o.addOption(addBooleanOption("h", "help", "Displays this help",
                false));
        this.o.addOption(addBooleanOption("?", null, "Displays this help",
                false));
        this.o.addOption(addOption(
                "f",
                "files",
                "Files which should be read: FILENAME>VARNAME1#INDEXVAR[RANGEBEGIN:RANGEEND]&VARNAME2...",
                true, ',', true, true, 0, false, false, 0,
                "file1,file2, ...", false));
        this.o.addOption(addOption(
                "D",
                null,
                "Set commandline options in java-properties (NAME.SUBNAME = VALUE) format",
                true, ',', true, true, 0, false, false, 0,
                "NAME1=OPTION1,NAME2=OPTION2, ...", false));
        this.o.addOption(addBooleanOption(
                "p",
                "print",
                "Print default configuration or print configuration given a config-file with -c or --cfg",
                false));
        this.o.addOption(addBooleanOption("g", "gui",
                "Run the gui for Maltcms", false));
        this.o.addOption(addOption(
                "a",
                "anchors",
                "Names of files containing alignment anchors, if given without path prefix, assumes location as given with option -i.",
                true, ',', true, true, 0, false, false, 0,
                "file1,file2, ...", false));
        this.o.addOption(addOption(
                "s",
                "showProperties",
                "Prints the properties available for configuration of given classes",
                true, ',', true, true, 0, false, false, 0,
                "class1,class2, ...", false));
        this.o.addOption(addOption(
                "l",
                "listServiceProviders",
                "Prints the service providers available for a specific service, e.g. cross.commands.fragments.AFragmentCommand",
                true, ',', true, true, 0, false, false, 0,
                "class1,class2, ...", false));
    }

    protected Option addBooleanOption(final String s, final String l,
            final String descr, final boolean required) {
        return addOption(s, l, descr, false, (char) 0, false, false, 0, false,
                false, 0, null, required);
    }

    /**
     *
     * @param cc
     *            the default configuration, within the subdirectory cfg/, from
     *            where you call Maltcms from
     */
    public void addDefaultConfiguration(final CompositeConfiguration cc) {
        EvalTools.notNull(cc, this);
        PropertiesConfiguration cfg;
        final String defaultProps = System.getProperty("maltcmsDefaultConfig",
                "cfg/default.properties");
        URL propRes = Maltcms.class.getClassLoader().getResource(defaultProps);
        // if loading as resource fails, load as file
        if (propRes == null) {
            try {
                propRes = new File(defaultProps).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            // otherwise load as resource
            try {
                cfg = new PropertiesConfiguration(propRes);
                cc.addConfiguration(cfg);
            } catch (final ConfigurationException e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * Utility method to add new ICommand-Line Logging to the
     * org.apache.commons.cli.
     *
     * @param s
     * @param l
     * @param descr
     * @param sep
     * @param ssep
     * @param arg
     * @param args
     * @param numArgs
     * @param optional_arg
     * @param optional_args
     * @param num_optional_args
     * @param argname
     * @param required
     * @return
     */
    protected Option addOption(final String s, final String l,
            final String descr, final boolean sep, final char ssep,
            final boolean arg, final boolean args, final int numArgs,
            final boolean optional_arg, final boolean optional_args,
            final int num_optional_args, final String argname,
            final boolean required) {
        if (l != null) {
            OptionBuilder.withLongOpt(l);
        }
        if (descr != null) {
            OptionBuilder.withDescription(descr);
        }
        if (sep) {
            this.log.debug("Using value separator: {}", ssep);
            if (ssep == 0x00) {
                OptionBuilder.withValueSeparator();
            } else {
                OptionBuilder.withValueSeparator(ssep);
            }
        }
        if (arg) {
            OptionBuilder.hasArg();
        }
        if (args) {
            if (numArgs > 0) {
                OptionBuilder.hasArgs(numArgs);
            } else {
                OptionBuilder.hasArgs();
            }
        }
        if (optional_arg) {
            OptionBuilder.hasOptionalArg();
        }
        if (optional_args) {
            if (num_optional_args > 0) {
                OptionBuilder.hasOptionalArgs(num_optional_args);
            } else {
                OptionBuilder.hasOptionalArgs();
            }
        }
        if (argname != null) {
            OptionBuilder.withArgName(argname);
        }
        if (required) {
            OptionBuilder.isRequired();
        }
        if (s != null) {
            return OptionBuilder.create(s);
        } else {
            return OptionBuilder.create();
        }
    }

    /**
     * Builds a composite configuration, adding default configuration and then
     * the system configuration.
     *
     * @return
     */
    public CompositeConfiguration getDefaultConfig() {
        final CompositeConfiguration cfg = new CompositeConfiguration();
        // add defaults last, so if first config redeclares a property it is
        // used.
        addDefaultConfiguration(cfg);
        cfg.addConfiguration(new SystemConfiguration());
        return cfg;
    }

    /**
     * Builds the final composite configuration from all available configuration
     * sources. CLI-Logging override default options, default options override
     * system options.
     *
     * @param args
     *            the usual java arguments
     * @return the readily configured and composed configuration
     */
    public CompositeConfiguration parseCommandLine(final String[] args) {
        EvalTools.notNull(args, this);
        this.log.debug("{}", Arrays.toString(args));
        final CommandLineParser clp = new GnuParser();
        final CompositeConfiguration cfg = new CompositeConfiguration();
        final CompositeConfiguration defaultCfg = getDefaultConfig();
        // addDefaultConfiguration(defaultCfg);
        final PropertiesConfiguration cmdLineCfg = new PropertiesConfiguration();
        boolean printOptions = false;
        String[] showPropertiesOptions = null;
        String[] listServiceProviders = null;
        try {
            final CommandLine cl = clp.parse(this.o, args);
            final Option[] opts = cl.getOptions();
            boolean printHelp = false;
            if (args.length == 0) {
                printHelp = true;
            }
            if (cl.hasOption("a")) {
                cmdLineCfg.setProperty("anchors.use", "true");
                cmdLineCfg.setProperty("anchors.location", cl.getOptionValues("a"));
            }
            if (cl.hasOption("s")) {
                showPropertiesOptions = cl.getOptionValues("s");
            }
            if (cl.hasOption("l")) {
                listServiceProviders = cl.getOptionValues("l");
            }
            if (cl.hasOption("i")) {
                cmdLineCfg.setProperty("input.basedir", cl.getOptionValue("i"));
            } else {
                cmdLineCfg.setProperty("input.basedir", defaultCfg.getString("user.home"));
            }
            for (final Option o1 : opts) {
                if (o1.getOpt().equals("p")) {
                    printOptions = true;
                }
                if (o1.getOpt().equals("h")) {
                    printHelp = true;
                }
                if (o1.getOpt().equals("?")) {
                    printHelp = true;
                }
                if (o1.getOpt().equals("r")) {
                    cmdLineCfg.setProperty("input.basedir.recurse", true);
                }
                if (o1.getOpt().equals("D")) {
                    for (final String s : o1.getValues()) {
                        final String[] split = s.split("=");
                        if (split.length > 1) {
                            cmdLineCfg.setProperty(split[0], split[1]);
                        } else {
                            this.log.warn("Could not split " + s + " at " + "=");
                        }
                    }
                }
                if (o1.getOpt().equals("o")) {
                    cmdLineCfg.setProperty("output.basedir", o1.getValue());
                }
                if (o1.getOpt().equals("f")) {
                    cmdLineCfg.setProperty("input.dataInfo", cl.getOptionValues("f"));
                    // o1.setValueSeparator(',');
                    // this.log.info("{}",o1.getValuesList());
                    // String[] s = o1.getValues();
                    // this.log.info("{}",Arrays.toString(s));
                    // processVariables(s, cmdLineCfg);
                }
            }
            if (printHelp) {
                printHelp(defaultCfg);
            }
            // add cmdLine first -> most important settings
            cfg.addConfiguration(cmdLineCfg);
            if (cl.hasOption("c")) {
                try {
                    // try to add config given by parameter c
                    cfg.addConfiguration(new PropertiesConfiguration(cl.getOptionValue("c")));
                } catch (final ConfigurationException e) {
                    this.log.error(e.getLocalizedMessage());
                }
            }
            // add defaults last
            cfg.addConfiguration(defaultCfg);
            if (cl.hasOption("g")) {
                this.runGui = true;
            }
        } catch (final ParseException e) {
            this.log.error("Error reading commandline!");
            this.log.error(e.getLocalizedMessage());
            printHelp(defaultCfg);
            System.exit(1);
        }
        if (showPropertiesOptions != null) {
            handleShowProperties(showPropertiesOptions);
        }
        if (listServiceProviders != null) {
            handleListServiceProviders(listServiceProviders);
        }
        if (printOptions) {
            printOptions(cfg);
            System.exit(0);
        }
        return cfg;
    }

    /**
     * @param optionValues
     */
    private void handleShowProperties(String[] optionValues) {
        for (String s : optionValues) {
            Class<?> c;
            try {
                c = getClass().getClassLoader().loadClass(s);
                this.log.info("Class: {}", c.getName());
                Collection<String> reqVars = AnnotationInspector.getRequiredVariables(c);
                if (!reqVars.isEmpty()) {
                    this.log.info("Required variables: ");
                }
                for (String rv : reqVars) {
                    this.log.info("\t{}", rv);
                }

                Collection<String> optVars = AnnotationInspector.getOptionalRequiredVariables(c);
                if (!optVars.isEmpty()) {
                    this.log.info("Optional variables: ");
                }
                for (String rv : optVars) {
                    this.log.info("\t{}", rv);
                }

                Collection<String> provVars = AnnotationInspector.getProvidedVariables(c);
                if (!provVars.isEmpty()) {
                    this.log.info("Provided variables: ");
                }
                for (String rv : provVars) {
                    this.log.info("\t{}", rv);
                }

                Collection<String> keys = AnnotationInspector.getRequiredConfigKeys(c);
                if (!keys.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Configuration keys: \n");
                    for (String key : keys) {
                        sb.append("\t"
                                + key
                                + " = "
                                + AnnotationInspector.getDefaultValueFor(c, key) + "\n");
                    }
                    this.log.info(sb.toString());
                } else {
                    this.log.info(
                            "Could not find annotated configuration keys for class {}!",
                            c.getName());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    /**
     * @param optionValues
     */
    private void handleListServiceProviders(String[] optionValues) {
        for (String s : optionValues) {
            Class<?> c;
            try {
                c = Class.forName(s);
                ServiceLoader<?> sl = ServiceLoader.load(c);
                this.log.info("Service Providers available for Service {}:", s);
                for (Object o : sl) {
                    if (o != null) {
                        this.log.info("\t{}", o.getClass().getName());
                    } else {
                        this.log.info("\t null");
                    }
                }
                this.log.info("Call Maltcms with -s my.service.provider to see available configuration keys and default values!");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        System.exit(0);
    }

    /**
     * Print help on command line options.
     *
     * @param defaultConfig
     */
    private void printHelp(final Configuration defaultConfig) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("call "
                + defaultConfig.getString("application.name")
                + " with the following arguments", " Version "
                + defaultConfig.getString("application.version"), this.o,
                Maltcms.licence);
        System.exit(0);
    }

    /**
     * Simply print out all config options currently known
     *
     * @param cfg
     */
    protected void printOptions(final Configuration cfg) {
        EvalTools.notNull(cfg, this);
        this.log.info("Current properties:");
        final Iterator<?> iter = cfg.getKeys();
        while (iter.hasNext()) {
            final String s = (String) iter.next();
            List<?> l = cfg.getList(s);
            if (l.size() == 1) {
                this.log.info("{} = ", s, l.get(0));
            } else {
                this.log.info("{} = ", s, l);
            }

        }
        // if (cfg.containsKey("show.properties.exit")) {
        // if (cfg.getBoolean("show.properties.exit")) {
        System.exit(0);
        // }
        // }
    }

    /**
     * Prepares the String[] for input to <i>processVariableFragments</i>.
     * Evaluates the appropriate option.
     *
     * @param s
     * @param cfg
     */
    public void processVariables(final String[] s, final Configuration cfg) {
        EvalTools.notNull(s, this);
        EvalTools.notNull(cfg, this);
        final String prefix = "";
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            this.log.debug("Building String: " + s[i]);
            builder.append(prefix + s[i]);
            if (i < s.length - 1) {
                builder.append(",");
            }
        }
        this.log.debug("Adding Variables {}", builder.toString());
        cfg.addProperty("input.dataInfo", builder.toString());

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        Maltcms.handleRuntimeException(this.log, e, null);
    }
}
