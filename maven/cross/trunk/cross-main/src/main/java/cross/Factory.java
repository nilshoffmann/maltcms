/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross;

import cross.annotations.Configurable;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IFileFragmentFactory;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.threads.ExecutorsManager;
import cross.datastructures.threads.ExecutorsManager.ExecutorType;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowFactory;
import cross.datastructures.workflow.WorkflowFactory;
import cross.exception.ConstraintViolationException;
import cross.io.DataSourceFactory;
import cross.io.IDataSourceFactory;
import cross.io.IInputDataFactory;
import cross.io.InputDataFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.*;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for the creation of processing chains. It must be configured prior to
 * any call to <i>createCommandSequence</i>. The factory will try to figure out
 * what your intention is, by inspecting the configuration and the command-line
 * input. Alternatively, you can set up the pipeline completely on your own, but
 * beware, there is only partial requirements checking between pipeline stages
 * as of now. You have to ensure, that commands early in the chain provide the
 * data needed by those commands later in the chain. If you need branching
 * behaviour, consider setting named properties for later pipeline elements to
 * use, or set up multiple instances of Maltcms with different configurations.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class Factory implements ConfigurationListener {

    private static Factory factory;

    /**
     * NEVER SYNCHRONIZE THIS METHOD, IT WILL BLOCK EVERYHTING WITHIN THE QUEUE!
     * 
     * @param time
     *            to wait until termination of each ThreadPool
     * @param u
     *            the unit of time
     * @throws InterruptedException
     *             thrown if interruption of waiting on termination occurs
     */
    public static void awaitTermination(final long time, final TimeUnit u)
            throws InterruptedException {
        if ((Factory.getInstance().es == null)
                || (Factory.getInstance().auxPool == null)) {
            throw new IllegalArgumentException(
                    "ExecutorService not initialized!");
        }
        Factory.getInstance().auxPool.awaitTermination(time, u);
        Factory.getInstance().es.awaitTermination(time, u);
        // NetcdfFileCache.clearCache(true);
        // NetcdfFileCache.exit();
        FileFragment.clearFragments();
        // Factory.getInstance().resetDate();
    }

    /**
     * Write current configuration to file.
     * 
     * @param filename
     */
    public static void dumpConfig(final String filename, final Date d) {
        final Configuration cfg = Factory.getInstance().getConfiguration();
        final File location = new File(FileTools.prependDefaultDirsWithPrefix(
                "", Factory.class, d), filename);
        Factory.getInstance().log.error("Saving configuration to: ");
        Factory.getInstance().log.error("{}", location.getAbsolutePath());
        Factory.saveConfiguration(cfg, location);
    }

    public static Factory getInstance() {
        if (Factory.factory == null) {
            Factory.factory = new Factory();
        }
        return Factory.factory;
    }

    public static void saveConfiguration(final Configuration cfg,
            final File location) {
        if (cfg instanceof FileConfiguration) {
            try {
                ((FileConfiguration) cfg).save(location);
            } catch (final ConfigurationException e) {
                Factory.getInstance().log.error(e.getLocalizedMessage());
            }
        } else {
            try {
                ConfigurationUtils.dump(cfg, new PrintStream(location));
            } catch (final FileNotFoundException e) {
                Factory.getInstance().log.error(e.getLocalizedMessage());
            }
        }
    }
    private DataSourceFactory dsf = null;
    private WorkflowFactory wf = null;
    private InputDataFactory idf = null;
    private ObjectFactory of = null;
    private IFileFragmentFactory fff = null;
    public final transient Logger log = LoggerFactory.getLogger(Factory.class);
    private transient CompositeConfiguration objconfig = new CompositeConfiguration();
    private transient ExecutorService es;
    @Configurable(name = "cross.Factory.maxthreads")
    private int maxthreads = 1;
    private transient ExecutorService auxPool;

    public void bootstrap(final Configuration cfg) {
    }

    /**
     * Listen to ConfigurationEvents.
     */
    @Override
    public void configurationChanged(final ConfigurationEvent arg0) {
        Factory.getInstance().log.debug("Configuration changed for property: "
                + arg0.getPropertyName() + " to value "
                + arg0.getPropertyValue());

    }

    /**
     * Call configure before retrieving an instance of ArrayFactory. This
     * ensures, that the factory is instantiated with a fixed config.
     * 
     * @param config
     */
    public void configure(final Configuration config) {
        EvalTools.notNull(config, Factory.class);
        Factory.getInstance().configureMe(config);
    }

    protected void configureMe(final Configuration config1) {
        EvalTools.notNull(config1, this);
        this.objconfig = new CompositeConfiguration();
        this.objconfig.addConfiguration(config1);
        this.objconfig.addConfigurationListener(this);
        if (config1.getBoolean("maltcms.ui.charts.PlotRunner.headless", true) == true) {
            System.setProperty("java.awt.headless", "true");
        }
        configureThreadPool(this.objconfig);
        // configure ObjectFactory
        getObjectFactory().configure(config1);
        getDataSourceFactory().configure(config1);
        getWorkflowFactory().configure(config1);
        getInputDataFactory().configure(config1);
//        FileTools.inputBasedirectory = new File(config1.getString(
//                "input.basedir", ""));
//        FileTools.outputBasedirectory = new File(config1.getString(
//                "output.basedir", ""));
//        FileTools.omitUserTimePrefix = config1.getBoolean("omitUserTimePrefix",
//                false);
//        FileTools.overwrite = config1.getBoolean("output.overwrite", false);
        // instantiate DataSourceFactory
    }

    private void configureThreadPool(final Configuration cfg) {
        this.maxthreads = cfg.getInt("cross.Factory.maxthreads", 1);
        final int numProcessors = Runtime.getRuntime().availableProcessors();
        this.log.debug("{} processors available to current runtime",
                numProcessors);
        this.maxthreads = (this.maxthreads < numProcessors) ? this.maxthreads
                : numProcessors;
        cfg.setProperty("cross.Factory.maxthreads", this.maxthreads);
        cfg.setProperty("maltcms.pipelinethreads", this.maxthreads);
        this.log.debug("Starting with Thread-Pool of size: " + this.maxthreads);
        initThreadPools();
    }

    /**
     * Build the command sequence, aka pipeline for command execution.
     * 
     * @return a command sequence initialized according to current configuration
     */
    public ICommandSequence createCommandSequence() {

        return createCommandSequence(null);
    }

    /**
     * Build the command sequence, aka pipeline for command execution.
     * 
     * @return a command sequence initialized according to current configuration
     */
    public ICommandSequence createCommandSequence(final TupleND<IFileFragment> t) {
        final ICommandSequence cd = getObjectFactory().instantiate(
                CommandPipeline.class);
        final IWorkflow iw = getWorkflowFactory().getDefaultWorkflowInstance(
                new Date(), cd);
        File outputDir = new File(getConfiguration().getString(
                "output.basedir", System.getProperty("user.dir")));
        //add username and timestamp as subdirectories
        if (!getConfiguration().getBoolean("omitUserTimePrefix", false)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MM-dd-yyyy_HH-mm-ss", Locale.US);
            String userName = System.getProperty("user.name", "default");
            outputDir = new File(outputDir, userName);
            outputDir = new File(outputDir, dateFormat.format(
                    iw.getStartupDate()));
        } else if (outputDir.exists()) {
            if (outputDir.listFiles().length != 0 && getConfiguration().
                    getBoolean("output.overwrite", false)) {
                log.warn(
                        "Output in location {} already exists. Option output.overwrite=true, removing previous output!");
                try {
                    FileUtils.deleteDirectory(outputDir);
                } catch (IOException ex) {
                    throw new RuntimeException(
                            "Deletion of directory " + outputDir + " failed!",
                            ex);
                }
                outputDir.mkdirs();
            } else {
                throw new ConstraintViolationException(
                        "Output exists in " + outputDir + " but output.overwrite=false. Call maltcms with -Doutput.overwrite=true to override!");
            }
        }
        outputDir.mkdirs();
        iw.setOutputDirectory(outputDir);
        cd.setWorkflow(iw);
        if (t == null) {
            cd.setInput(getInputDataFactory().prepareInputData(getConfiguration().
                    getStringArray("input.dataInfo")));
        } else {
            cd.setInput(t);
        }
        log.info("Workflow {} output: {}", iw.getName(), iw.getOutputDirectory());
        return cd;
    }

    public Configuration getConfiguration() {
        return Factory.getInstance().getConfigurationMe();
    }

    protected Configuration getConfigurationMe() {
        if (this.objconfig == null) {
            this.log.warn("Configuration not set, creating empty one!");
            this.objconfig = new CompositeConfiguration();
        }
        // EvalTools.notNull(this.objconfig,
        // "ArrayFactory has not been configured yet!", this);
        // throw new RuntimeException("ArrayFactory has not been configured
        // yet!");
        return this.objconfig;
    }

    public IDataSourceFactory getDataSourceFactory() {
        if (this.dsf == null) {
            this.dsf = getObjectFactory().instantiate(
                    "cross.io.DataSourceFactory", DataSourceFactory.class,
                    getConfiguration());
        }
        return this.dsf;
    }

    public IFileFragmentFactory getFileFragmentFactory() {
        if (this.fff == null) {
            this.fff = getObjectFactory().instantiate(
                    "cross.datastructures.fragments.FileFragmentFactory",
                    FileFragmentFactory.class, getConfiguration());
        }
        return this.fff;
    }

    public IInputDataFactory getInputDataFactory() {
        if (this.idf == null) {
            InputDataFactory idf = getObjectFactory().instantiate(
                    "cross.io.InputDataFactory", InputDataFactory.class,
                    getConfiguration());
            idf.setInput(getConfiguration().getStringArray("input.dataInfo"));
            idf.setRecurse(getConfiguration().getBoolean("input.basedir.recurse",
                    false));
            idf.setBasedir(System.getProperty("user.dir"));
            this.idf = idf;
        }
        return this.idf;
    }

    public IObjectFactory getObjectFactory() {
        if (this.of == null) {
            this.of = new ObjectFactory();
            this.of.configure(getConfiguration());
        }
        return this.of;
    }

    public IWorkflowFactory getWorkflowFactory() {
        if (this.wf == null) {
            this.wf = getObjectFactory().instantiate(
                    "cross.datastructures.workflow.WorkflowFactory",
                    WorkflowFactory.class, getConfiguration());
        }
        return this.wf;
    }

    private void initThreadPools() {
        this.es = new ExecutorsManager(this.maxthreads);// Executors.newFixedThreadPool(this.maxthreads);
        this.auxPool = new ExecutorsManager(ExecutorType.SINGLETON);// Executors.newFixedThreadPool(this.maxthreads);
    }

    /**
     * Shutdown the factory's thread pool.
     * 
     */
    public void shutdown() {
        if ((this.es == null) || (this.auxPool == null)) {
            throw new IllegalArgumentException(
                    "ExecutorService not initialized!");
        }

        this.es.shutdown();
        this.auxPool.shutdown();

    }

    /**
     * 
     */
    public List<Runnable> shutdownNow() {
        if ((this.es == null) || (this.auxPool == null)) {
            throw new IllegalArgumentException(
                    "ExecutorService not initialized!");
        }
        final List<Runnable> l = new ArrayList<Runnable>();
        // NetcdfFileCache.clearCache(true);
        // NetcdfFileCache.exit();
        FileFragment.clearFragments();
        // resetDate();
        l.addAll(this.es.shutdownNow());
        l.addAll(this.auxPool.shutdownNow());
        return l;
    }

    /**
     * Jobs submitted via this method will be run by the auxiliary thread pool.
     * 
     * @param c
     *            the Callable of any type to submit
     * @return a Future of the same type as the Callable
     */
    public Future<?> submitJob(final Callable<?> c) {
        return this.auxPool.submit(c);
    }

    /**
     * Submit a Runnable job to the Factory
     * 
     * @param r
     *            the Runnable to submit
     */
    public void submitJob(final Runnable r) {
        submitJobMe(r);
    }

    protected void submitJobMe(final Runnable r) {
        EvalTools.notNull(r, this);
        this.es.execute(r);
    }
}
