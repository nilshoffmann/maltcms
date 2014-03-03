/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package net.sf.maltcms.apps;

import cross.Factory;
import cross.annotations.AnnotationInspector;
import cross.applicationContext.ReflectionApplicationContextGenerator;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.workflow.IWorkflow;
import cross.exception.ConstraintViolationException;
import cross.exception.ExitVmException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;

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
@Slf4j
public class Maltcms implements Thread.UncaughtExceptionHandler {

    static Maltcms mcms;
    private boolean runGui = false;

    /**
     *
     * @return
     */
    public boolean isRunGui() {
        return this.runGui;
    }

    /**
     *
     * @param runGui
     */
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

    private static void handleExitVmException(final Logger log,
        final ExitVmException npe) {
        int ecode;
        Maltcms.shutdown(1, log);
        log.error(npe.getLocalizedMessage());
        ecode = 1;
        System.exit(ecode);
    }

    private static void handleRuntimeException(final Logger log,
        final Throwable npe, final ICommandSequence ics) {
        int ecode;
        Maltcms.shutdown(1, log);
        log.error("Caught Throwable, returning to console!");
        log.error(npe.getLocalizedMessage());
        npe.printStackTrace(System.err);
        Maltcms.printBugTrackMessage(log, ics);
        ecode = 1;
        // Save configuration
        Factory.dumpConfig("runtime.properties", ics.getWorkflow().
            getStartupDate());
        System.exit(ecode);
    }

    /**
     * Application's main procedure.
     *
     * @param args what is passed in from the command-line, usually
     */
    public static void main(final String[] args) {
        URL log4jConfiguration = null;
        try {
            String resource = System.getProperty("log4j.configuration");
            if (resource != null) {
                log4jConfiguration = new URL(resource);
            } else {
                log4jConfiguration = Maltcms.class.getResource(
                    "/cfg/log4j.properties");
            }
        } catch (MalformedURLException ex) {
        }
        if (log4jConfiguration != null) {
            PropertyConfigurator.configure(log4jConfiguration);
        } else {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "INFO, A1");
            props.setProperty("log4j.appender.A1",
                "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern",
                "%m%n");
            props.setProperty("log4j.category.cross", "WARN");
            props.setProperty("log4j.category.cross.datastructures.pipeline",
                "INFO");
            props.setProperty("log4j.category.maltcms.commands.fragments",
                "INFO");
            props.setProperty("log4j.category.maltcms.commands.fragments2d",
                "INFO");
            props.setProperty("log4j.category.maltcms", "WARN");
            props.setProperty("log4j.category.ucar", "WARN");
            props.setProperty("log4j.category.smueller", "WARN");
            props.setProperty("log4j.category.org.springframework.beans.factory", "WARN");
            PropertyConfigurator.configure(props);
        }

        //final Logger log = cross.Logging.getLogger(Maltcms.class);
        final int ecode = 0;
        ICommandSequence cs = null;
        try {
            final Maltcms m = Maltcms.getInstance();
            final CompositeConfiguration cfg = m.parseCommandLine(args);
            log.info("Running Maltcms version {}",
                cfg.getString("application.version"));
            EvalTools.notNull(cfg, cfg);
            log.info("Configuring Factory");
            log.info("Using pipeline definition at {}", cfg.getString("pipeline.xml"));
            Factory.getInstance().configure(cfg);
            // Set up the command sequence
            cs = Factory.getInstance().createCommandSequence();
            final IWorkflow iw = cs.getWorkflow();
            if (cs.validate()) {
                // Evaluate until empty
                try {
                    iw.call();
                    Maltcms.shutdown(30, log);
                    // Save workflow
                    iw.save();
                    System.exit(ecode);
                } catch (final ExitVmException e) {
                    Maltcms.handleExitVmException(log, e);
                } catch (final IllegalArgumentException iae) {
                    Maltcms.handleExitVmException(log, new ExitVmException(iae));
                } catch (final Throwable t) {
                    Maltcms.handleRuntimeException(log, t, cs);
                }
            } else {
                throw new ConstraintViolationException(
                    "Pipeline is invalid, but strict checking was requested!");
            }
        } catch (final ExitVmException e) {
            Maltcms.handleExitVmException(log, e);
        } catch (final IllegalArgumentException iae) {
            Maltcms.handleExitVmException(log, new ExitVmException(iae));
        } catch (final Throwable t) {
            Maltcms.handleRuntimeException(log, t, cs);
        }
    }

    /**
     *
     * @param log
     * @param cs
     */
    public static void printBugTrackMessage(final Logger log, final ICommandSequence cs) {
        log.error("#############################################################################");
        log.error("# Maltcms has caught an unexpected exception while executing.");
        log.error("# Please read the exception message carefully for hints on what");
        log.error("# went wrong.");
        log.error("# If the message does not give hints on how to avoid the exception,");
        log.error("# please submit a bug report including this output to:");
        log.error(
            "# http://sf.net/p/maltcms/maltcms-bugs");
        log.error("#");
        log.error("# Please attach Maltcms' log file (maltcms.log) and the runtime");
        log.error("# properties and pipeline configuration to your report. ");
        log.error("# By default, the log file is created within the directory from which"
            + "");
        log.error("# maltcms is called, while the runtime properties and pipeline are");
        log.error("# located in ");
        log.error("# " + new File(cs.getWorkflow().getOutputDirectory(), "Factory").getAbsolutePath());
        log.error("# ");
        log.error("# Your report will help improve Maltcms, thank you!");
        log.error("#############################################################################");
    }

    private static void shutdown(final long seconds, final Logger log) {
        // Shutdown application thread
        try {
            Factory.getInstance().shutdown();
        } catch (final IllegalArgumentException e) {
            // log.warn(e.getLocalizedMessage());
        }
        try {
            Factory.getInstance().awaitTermination(seconds, TimeUnit.MINUTES);
        } catch (final IllegalArgumentException e) {
        }
    }
    private static final String licence = "Maltcms is licensed under the terms of the GNU LESSER GENERAL PUBLIC LICENSE (LGPL) Version 3 as of 29 June 2007\nLibraries used by Maltcms can be licensed under different conditions, please consult the licenses directory for more information!\nMore information may be found at http://maltcms.sf.net !";
    private final Options o;

    /**
     * Protected Ctor, override this class, to set your own command line
     * options.
     *
     */
    protected Maltcms() {
        this.o = new Options();
        this.o.addOption(addOption("c", null, "configuration file location", false,
            (char) 0, true, false, 0, false, false, 0, "file", false));
        this.o.addOption(addOption(
            "i",
            null,
            "input base directory",
            false, (char) 0, true, false, 0, false, false, 0, "dir", false));
        this.o.addOption(addOption("o", null,
            "target directory for all output", false, (char) 0, true,
            false, 0, false, false, 0, "dir", false));
        this.o.addOption(addBooleanOption("r", null,
            "recurse into input base directory", false));
        this.o.addOption(addBooleanOption("h", null, "display this help",
            false));
        this.o.addOption(addBooleanOption("?", null, "display this help",
            false));
        this.o.addOption(addOption(
            "e",
            null,
            "extension locations (directories or .jar files)",
            true, ',', true, true, 0, false, false, 0, "file:///path1/,...",
            false));
        this.o.addOption(addOption(
            "f",
            null,
            "input files (wildcard, e.g. *.cdf, file name only if -i is given, or full path)",
            //: FILENAME>VARNAME1#INDEXVAR[RANGEBEGIN:RANGEEND]&VARNAME2...",
            true, ',', true, true, 0, false, false, 0, "file1,...",
            false));
        this.o.addOption(addBooleanOption(
            "p",
            null,
            "print resolved configuration",
            false));
        this.o.addOption(addOption(
            "a",
            null,
            "alignment anchor files (without path prefix, assumes location as given with option -i)",
            true, ',', true, true, 0, false, false, 0, "file1,...",
            false));
        //FIXME currently disabled until beans xml context can be created
        this.o.addOption(addOption(
            "s",
            "showProperties",
            "Prints the properties available for configuration of given classes",
            true, ',', true, true, 0, false, false, 0,
            "class1,class2, ...", false));
        this.o.addOption(addOption(
            "l",
            null,
            "print available service providers (e.g. cross.commands.fragments.AFragmentCommand)",
            true, ',', true, true, 0, false, false, 0,
            "class1,...", false));
        //FIXME implement beans xml creation
        this.o.addOption(addOption(
            "b",
            "createBeanXml",
            "Creates an xml file in spring format for all given of cross.commands.fragments.AFragmentCommand. '*' as an argument will create one xml file for every available AFragmentCommand implementation.",
            true, ',', true, true, 0, false, false, 0,
            "class1,class2, ...", false));
    }

    /**
     *
     * @param s
     * @param l
     * @param descr
     * @param required
     * @return
     */
    protected Option addBooleanOption(final String s, final String l,
        final String descr, final boolean required) {
        return addOption(s, l, descr, false, (char) 0, false, false, 0, false,
            false, 0, null, required);
    }

    /**
     *
     */
    public PropertiesConfiguration getDefaultConfiguration() {
        PropertiesConfiguration cfg = null;
        //check for commandline argument/environment variable maltcms.home
        final String maltcmsHome = System.getProperty("maltcms.home");
        if (maltcmsHome != null) {
            File propertyFile = new File(maltcmsHome, "cfg/default.properties");
            //is it a file?
            if (propertyFile.exists() && propertyFile.isFile()) {
                try {
                    cfg = new PropertiesConfiguration(propertyFile);
                    log.debug("Using default.properties at {}", propertyFile.getAbsolutePath());
                } catch (final ConfigurationException e) {
                    log.warn("Configuration Exception for file " + propertyFile.getAbsolutePath() + ":", e);
                }
            }
        }
        if (cfg == null) {
            //try to locate default.properties from user.dir
            File f = new File(System.getProperty("user.dir"), "cfg/default.properties");
            if (f.exists()) {
                try {
                    cfg = new PropertiesConfiguration(f);
                    log.info("Using default.properties below {} at {}", System.getProperty("user.dir"), f.getAbsolutePath());
                } catch (ConfigurationException ex) {
                    log.warn("Configuration Exception for file " + f.getAbsolutePath() + ":", ex);
                }
            } else {
                log.warn("Could not locate default.properties, using defaults from classpath!");
                try {
                    cfg = new PropertiesConfiguration(getClass().getClassLoader().getResource("cfg/default.properties"));
                    log.info("Using default.properties from class path at {}", cfg.getPath());
                } catch (ConfigurationException ex) {
                    log.warn("Configuration Exception for class path resource " + cfg.getPath() + ":", ex);
                }
            }
        }
        if (cfg == null) {
            throw new NullPointerException("Could not locate default configuration!");
        } else {
            return cfg;
        }
    }

    /**
     *
     * @param urls
     */
    public void initClassLoader(String[] urls) {
        URL maltcmsLocation = getClass().getProtectionDomain().getCodeSource().
            getLocation();
        File baseDir = new File(".");
        log.info("current working dir is " + baseDir);
        try {
            baseDir = new File(maltcmsLocation.toURI()).getParentFile();

        } catch (URISyntaxException ex) {
            java.util.logging.Logger.getLogger(Maltcms.class
                .getName()).
                log(Level.SEVERE, null, ex);
        }
        log.info("Maltcms is executing from {}", baseDir);
        ClassLoader contextClassLoader = Thread.currentThread().
            getContextClassLoader();
        List<URL> contextUrls = new LinkedList<URL>();
        for (int i = 0; i < urls.length; i++) {
            URL url = null;
            File file = new File(urls[i]);
            if (file.exists() && (file.isFile() || file.isDirectory() && file.canRead())) {
                try {
                    url = file.toURI().toURL();
                } catch (MalformedURLException ex) {
                    log.warn("Invalid URL: {}", urls[i]);
                }
            } else {
                try {
                    url = new URL(urls[i]);
                } catch (MalformedURLException mue) {
                    log.warn("Invalid URL: {}", urls[i]);
                }
            }
            if (url != null) {
                log.info("Adding {} to module path!", url);
                contextUrls.add(url);

            }
        }
        // Create the class loader by using the given URL
        // Use contextClassLoader as parent to maintain current visibility
        ClassLoader urlCl
            = URLClassLoader.newInstance(contextUrls.toArray(new URL[contextUrls.size()]), contextClassLoader);

        try {
            // Save the class loader so that you can restore it later
            Thread.currentThread().setContextClassLoader(urlCl);

        } catch (SecurityException e) {
            log.error(
                "Setting of URL class loader is not allowed!\nPlease extend the classpath by adding additional jars to the -cp command line argument! Alternative: Change the security policy for this program. See http://download.oracle.com/javase/tutorial/security/tour2/step3.html for details!");
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
     * Builds the final composite configuration from all available configuration
     * sources. CLI-Logging override default options, default options override
     * system options.
     *
     * @param args the usual java arguments
     * @return the readily configured and composed configuration
     */
    public CompositeConfiguration parseCommandLine(final String[] args) {
        EvalTools.notNull(args, this);
        this.log.debug("{}", Arrays.toString(args));
        final CommandLineParser clp = new GnuParser();
        final CompositeConfiguration cfg = new CompositeConfiguration();
        final PropertiesConfiguration defaultCfg = getDefaultConfiguration();
        final PropertiesConfiguration cmdLineCfg = new PropertiesConfiguration();
        boolean printOptions = false;
        String[] beans = null;
        try {
            final CommandLine cl = clp.parse(this.o, args);
            final Option[] opts = cl.getOptions();
            boolean printHelp = false;
            if (args.length == 0) {
                printHelp = true;
            }
            if (cl.hasOption("a")) {
                cmdLineCfg.setProperty("anchors.use", "true");
                cmdLineCfg.setProperty("anchors.location",
                    cl.getOptionValues("a"));
                log.info("Using anchors from location: {}", cl.getOptionValues("a"));
            }
            if (cl.hasOption("e")) {
                initClassLoader(cl.getOptionValues("e"));
            }
            if (cl.hasOption("s")) {
                String[] showPropertiesOptions = cl.getOptionValues("s");
                if (showPropertiesOptions != null) {
                    handleShowProperties(showPropertiesOptions);
                }

            }
            if (cl.hasOption("l")) {
                String[] listServiceProviders = cl.getOptionValues("l");
                if (listServiceProviders != null) {
                    handleListServiceProviders(listServiceProviders);
                }
            }
            if (cl.hasOption("b")) {
                beans = cl.getOptionValues("b");
            }
            if (cl.hasOption("i")) {
                cmdLineCfg.setProperty("input.basedir", cl.getOptionValue("i"));
            } else {
                cmdLineCfg.setProperty("input.basedir",
                    System.getProperty("user.dir", "."));
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
                if (o1.getOpt().equals("o")) {
                    cmdLineCfg.setProperty("output.basedir", o1.getValue());
                }
                if (o1.getOpt().equals("f")) {
                    String[] inputFiles = cl.getOptionValues("f");
                    log.info("Received input files: {}", Arrays.toString(inputFiles));
                    if (inputFiles.length == 0 || inputFiles[0].isEmpty()) {
                        throw new ExitVmException("Please supply at least one input file!");
                    }
                    cmdLineCfg.setProperty("input.dataInfo",
                        inputFiles);
                }
            }
            if (printHelp) {
                printHelp(defaultCfg);
            }
            // add system configuration, -D options override default options
            cfg.addConfiguration(new SystemConfiguration());
            if (cl.hasOption("c")) {
                try {
                    File userConfigLocation = new File(cl.getOptionValue("c"));
                    if (!userConfigLocation.isFile()) {
                        throw new ExitVmException("Configuration file '" + userConfigLocation + "' is not a valid file!");
                    }
                    if (!userConfigLocation.exists()) {
                        throw new ExitVmException("Configuration file '" + userConfigLocation + "' does not exist!");
                    }
                    if (!userConfigLocation.isAbsolute()) {
                        // try to add config given by parameter c
                        userConfigLocation = new File(new File(System.getProperty(
                            "user.dir")), cl.getOptionValue("c"));
                    }
                    cfg.setProperty("pipeline.properties", userConfigLocation.getAbsolutePath());
                    cfg.setProperty("config.basedir", userConfigLocation.getParentFile().getAbsoluteFile().toURI().getPath());
                    //user options override default options
                    cfg.addConfiguration(new PropertiesConfiguration(cl.getOptionValue("c")));
                } catch (final ConfigurationException e) {
                    this.log.error(e.getLocalizedMessage());
                }
            } else if (!cl.hasOption("b")) {
                throw new ExitVmException("Please supply a configuration file!");
            }
            // cmdLine options override default options and system options
            cfg.addConfiguration(cmdLineCfg);
            // add defaults as fallback
            cfg.addConfiguration(defaultCfg);
        } catch (final ParseException e) {
            this.log.error("Error reading commandline!");
            this.log.error(e.getLocalizedMessage());
            printHelp(defaultCfg);
            System.exit(1);
        }
        if (beans != null) {
            handleCreateBeanXML(cfg, beans);
            System.exit(0);
        }
        if (printOptions) {
            printOptions(cfg);
            System.exit(1);
        }
        return new CompositeConfiguration(Arrays.asList(cfg));
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
                    this.log.info("{}", rv);
                }

                Collection<String> optVars = AnnotationInspector.getOptionalRequiredVariables(c);
                if (!optVars.isEmpty()) {
                    this.log.info("Optional variables: ");
                }
                for (String rv : optVars) {
                    this.log.info("{}", rv);
                }

                Collection<String> provVars = AnnotationInspector.getProvidedVariables(c);
                if (!provVars.isEmpty()) {
                    this.log.info("Provided variables: ");
                }
                for (String rv : provVars) {
                    this.log.info("{}", rv);
                }

                Collection<String> keys = AnnotationInspector.getRequiredConfigKeys(c);
                if (!keys.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Configuration keys: \n");
                    for (String key : keys) {
                        sb.append(
                            key
                            + " = "
                            + AnnotationInspector.getDefaultValueFor(c, key) + "\n");
                        String descr = AnnotationInspector.getDescriptionFor(c, key);
                        sb.append("\t").append(descr).append("\n");
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
                        this.log.info("{}", o.getClass().getName());
                    } else {
                        this.log.info("null");
                    }
                }
                this.log.info(
                    "Call Maltcms with -s my.service.provider to see available configuration keys and default values!");
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
        formatter.setWidth(80);
        formatter.printHelp(
            "call " + defaultConfig.getString("application.name")
            + " with the following arguments", " Version "
            + defaultConfig.getString("application.version"),
            this.o, Maltcms.licence);
        System.exit(0);
    }

    /**
     * Simply print out all config options currently known
     *
     * @param cfg
     */
    protected void printOptions(final Configuration cfg) {
        EvalTools.notNull(cfg, this);
        log.info("Current properties:");
        log.info("{}", ConfigurationUtils.toString(cfg));
        System.exit(0);
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
            log.debug("Building String: " + s[i]);
            builder.append(prefix);
            builder.append(s[i]);
            if (i < s.length - 1) {
                builder.append(",");
            }
        }
        log.debug("Adding Variables {}", builder.toString());
        cfg.addProperty("input.dataInfo", builder.toString());

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    /**
     *
     * @param t
     * @param e
     */
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        Maltcms.handleRuntimeException(log, e, null);
    }

    private void handleCreateBeanXML(Configuration cfg, String... beans) {
        File outputDir = new File(cfg.getString("output.basedir", "."), "pipelines");
        outputDir.mkdirs();
        File xmlDir = new File(outputDir, "xml");
        xmlDir.mkdirs();
        LinkedList<String> beanList = new LinkedList<String>();
        if (beans.length == 1 && beans[0].equals("*")) {
            Class<?> c;
            try {
                c = Class.forName("cross.commands.fragments.AFragmentCommand");
                ServiceLoader<?> sl = ServiceLoader.load(c);
                for (Object o : sl) {
                    File contextFile = new File(xmlDir, o.getClass().getSimpleName() + ".xml");
                    ReflectionApplicationContextGenerator.createContextXml(contextFile, "3.0", "prototype", o.getClass().getCanonicalName());
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            File contextFile = new File(xmlDir, "customContext.xml");
            //        beanList.add(IWorkflow.class.getCanonicalName());
            //        beanList.add(ICommandSequence.class.getCanonicalName());
            beanList.addAll(Arrays.asList(beans));
            ReflectionApplicationContextGenerator.createContextXml(contextFile, "3.0", "prototype", beanList.toArray(new String[beanList.size()]));
            try {
                PropertiesConfiguration pc = new PropertiesConfiguration(new File(outputDir, "customPipeline.mpl"));
                pc.setProperty("pipeline.xml", "file:${config.basedir}/xml/" + contextFile.getName());
                pc.save();

            } catch (ConfigurationException ex) {
                java.util.logging.Logger.getLogger(Maltcms.class
                    .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
