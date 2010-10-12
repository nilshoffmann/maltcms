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
 * $Id$
 */

package cross;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import maltcms.datastructures.ms.ExperimentFactory;
import maltcms.datastructures.ms.IExperiment;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;
import org.slf4j.Logger;

import ucar.nc2.NetcdfFileCache;
import annotations.AnnotationInspector;
import annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.threads.ExecutorsManager;
import cross.datastructures.threads.ExecutorsManager.ExecutorType;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.exception.ConstraintViolationException;
import cross.io.DataSourceFactory;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.StringTools;

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

	private static ArrayList<AFragmentCommand> list = new ArrayList<AFragmentCommand>();

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
		NetcdfFileCache.clearCache(true);
		NetcdfFileCache.exit();
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
		final File location = FileTools.prependDefaultDirs(filename,
		        Factory.class, d);
		Factory.getInstance().log.error("Saving configuration to: ");
		Factory.getInstance().log.error("{}", location.getAbsolutePath());
		Factory.saveConfiguration(cfg, location);
	}

	/**
	 * Returns those files, that were given on the command line.
	 * 
	 * @return
	 */
	public static List<IFileFragment> getInitialFiles() {
		return Factory.initialFiles;
	}

	public static Factory getInstance() {
		if (Factory.factory == null) {
			Factory.factory = new Factory();
		}
		return Factory.factory;
	}

	protected static void initFile(final String s) {
		Factory.getInstance().log.debug("Adding: " + s);
		final IFileFragment inputFragment = FileFragmentFactory.getInstance()
		        .fromString(s);
		if (!(new File(inputFragment.getAbsolutePath()).exists())) {
			throw new ConstraintViolationException("Input file "
			        + inputFragment.getAbsolutePath() + " does not exist!");
		}
		// FileFragment ff = FragmentTools.create(FileTools.prependDefaultDirs(
		// inputFragment.getName(), ArrayFactory.class), ArrayFactory.class);
		// ff.addSourceFile(inputFragment);
		final IExperiment iexp = ExperimentFactory
		        .createExperiment(inputFragment);
		// ff.save();
		// Experiment1D e = new Experiment1D(ff);
		Factory.initialFiles.add(iexp);
	}

	/**
	 * Preprocess input data (files and variables).
	 * 
	 * @return
	 */
	public static TupleND<IFileFragment> prepareInputData() {
		Factory.getInstance().log.info("Preparing Input data!");
		final Configuration config = Factory.getInstance().getConfiguration();
		Factory.initialFiles = new ArrayList<IFileFragment>();
		if (config.containsKey("input.dataInfo")) {
			Factory.getInstance().log.debug("Unsplit:"
			        + Arrays.deepToString(Factory.getInstance()
			                .getConfiguration()
			                .getStringArray("input.dataInfo")));
			final String[] input = config.getStringArray("input.dataInfo");
			Factory.getInstance().log.debug("{}", input);
			if (input.length == 1) {
				// if (input[0].equals("*.cdf") || input[0].startsWith("*.cdf"))
				// {
				// final File dir = new File(Factory.getInstance()
				// .getConfiguration().getString("input.basedir", ""));
				// String variables = "";
				// if (input[0].startsWith("*.cdf") && input[0].contains(">")) {
				// variables = input[0].substring(input[0].indexOf(">"));
				// }
				// Factory.getInstance().log.debug(
				// "Setting variables {} for all input files!",
				// variables);
				// final FilenameFilter filter = new FilenameFilter() {
				// public boolean accept(final File dir1, final String name) {
				// return name.endsWith(".cdf");
				// }
				// };
				// final String[] children = dir.list(filter);
				// if (children == null) {
				// Factory.getInstance().log
				// .error("Could not locate directory "
				// + dir.getAbsolutePath() + ", aborting!");
				// System.exit(-1);
				// } else {
				// for (int i = 0; i < children.length; i++) {
				// Factory.getInstance().log.info("Adding file "
				// + (i + 1) + "/" + children.length + ": "
				// + children[i]);
				// // File f = new File(dir, children[i]);
				// Factory.initFile(children[i] + variables);
				// // FileFragment inputFragment =
				// // FragmentTools.create(
				// // new File(dir, children[i]), null);
				// // FileFragment ff = FragmentTools
				// // .create(ArrayFactory.class);
				// // ff.addSourceFile(inputFragment);
				// // Experiment1D e = new Experiment1D(inputFragment);
				// // MaltcmsTools.addExperiment(inputFragment, e);
				// // ArrayFactory.initialFiles.add(inputFragment);
				// }
				// }
				// return new TupleND<IFileFragment>(Factory.initialFiles);
				// }
				if (input[0].startsWith("*")) {
					final File dir = new File(Factory.getInstance()
					        .getConfiguration().getString("input.basedir", ""));
					final boolean recurse = Factory.getInstance()
					        .getConfiguration().getBoolean(
					                "input.basedir.recurse", false);
					final FilenameFilter filter = new FilenameFilter() {
						public boolean accept(final File dir1, final String name) {
							return name.endsWith(StringTools
							        .getFileExtension(input[0]));
						}
					};
					final FilenameFilter subdirfilter = new FilenameFilter() {
						public boolean accept(final File dir1, final String name) {
							return new File(dir, name).isDirectory();
						}
					};
					final ArrayList<String> files = new ArrayList<String>();
					if (recurse) {
						final ArrayList<String> subdirs = new ArrayList<String>();
						subdirs.add(dir.getAbsolutePath());
						while (!subdirs.isEmpty()) {
							final File directory = new File(subdirs.remove(0));
							Factory.getInstance().log.debug("Checking dir: {}",
							        directory);
							final String[] dirs = directory.list(subdirfilter);
							final String[] fils = directory.list(filter);
							if (fils != null) {
								for (String s : fils) {
									Factory.getInstance().log.debug(
									        "Adding file {}", s);
									files.add(new File(directory, s)
									        .getAbsolutePath());
								}
							}
							if (dirs != null) {
								for (String s : dirs) {
									Factory.getInstance().log.debug(
									        "Adding subdirectory {}", s);
									subdirs.add(new File(directory, s)
									        .getAbsolutePath());
								}
							}
						}
					} else {
						Factory.getInstance().log
						        .debug("Checking dir: {}", dir);
						String[] fls = dir.list(filter);
						for (String s : fls) {
							Factory.getInstance().log
							        .debug("Adding file {}", s);
							files.add(new File(dir, s).getAbsolutePath());
						}
					}

					final String[] children = files.toArray(new String[] {});// dir.list(filter);
					if (children == null) {
						Factory.getInstance().log
						        .error("Could not locate directory "
						                + dir.getAbsolutePath() + ", aborting!");
						System.exit(-1);
					} else if (children.length == 0) {
						Factory.getInstance().log
						        .error("Could not locate files in "
						                + dir.getAbsolutePath() + ", aborting!");
						System.exit(-1);
					} else {
						for (int i = 0; i < children.length; i++) {
							Factory.getInstance().log.info("Adding file "
							        + (i + 1) + "/" + children.length + ": "
							        + children[i]);
							Factory.initFile(children[i]);
						}
					}
					return new TupleND<IFileFragment>(Factory.initialFiles);
				}
			}
			if (input.length == 0) {
				Factory.getInstance().log.error("Could not split dataInfo!");
				System.exit(-1);
			}

			int cnt = 0;
			for (final String s : input) {
				Factory.getInstance().log.info("Adding file " + (cnt + 1) + "/"
				        + input.length + ": " + s);
				cnt++;
				Factory.initFile(s);
			}
			return new TupleND<IFileFragment>(Factory.initialFiles);
		} else {
			Factory.getInstance().log.error("No input data given, aborting!");
			System.exit(-1);
		}
		return null;
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

	public final transient Logger log = Logging.getLogger(Factory.class);

	private transient CompositeConfiguration objconfig;

	private transient ExecutorService es;

	private static List<IFileFragment> initialFiles = new ArrayList<IFileFragment>(
	        2);

	@Configurable(name = "cross.Factory.maxthreads")
	private int maxthreads = 1;

	private transient ExecutorService auxPool;

	/**
	 * Listen to ConfigurationEvents.
	 */
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
		// instantiateSingleton(DataSourceFactory.class);
		DataSourceFactory.getInstance().configure(config1);
	}

	private void configureThreadPool(final Configuration cfg) {
		this.maxthreads = cfg.getInt("cross.Factory.maxthreads", 1);
		final int numProcessors = Runtime.getRuntime().availableProcessors();
		this.log.info("{} processors available to current runtime",
		        numProcessors);
		this.maxthreads = (this.maxthreads < numProcessors) ? this.maxthreads
		        : numProcessors;
		cfg.setProperty("cross.Factory.maxthreads", this.maxthreads);
		cfg.setProperty("maltcms.pipelinethreads", this.maxthreads);
		this.log.info("Starting with Thread-Pool of size: " + this.maxthreads);
		initThreadPools();
		// this.es = Executors.newFixedThreadPool(maxthreads);
		// this.auxPool = Executors.newFixedThreadPool(maxthreads);
	}

	private <T> void configureType(final T t) {
		if (t instanceof IConfigurable) {
			Factory.getInstance().log.debug(
			        "Instance of type {} is configurable!", t.getClass()
			                .toString());
			final Collection<String> requiredKeys = AnnotationInspector
			        .getRequiredConfigKeys(t);
			Factory.getInstance().log.debug("Required keys for class {}", t
			        .getClass());
			Factory.getInstance().log.debug("{}", requiredKeys);
			// TODO this is temporary, as long as not all classes use the
			// new
			// Configurable Annotations
			// if (requiredKeys.isEmpty()) {
			Factory.getInstance().log
			        .debug("Configuring with full configuration!");
			((IConfigurable) t).configure(getConfiguration());
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
	}

	/**
	 * Build the command sequence, aka pipeline for command execution.
	 * 
	 * @return a command sequence initialized according to current configuration
	 */
	public ICommandSequence createCommandSequence() {

		final ICommandSequence cd = instantiate(CommandPipeline.class);
		// new CommandPipeline(ArrayFactory.prepareInputData(),
		// new ArrayList<AFragmentCommand<?>>(ArrayFactory.list));
		// cd.configure(ArrayFactory.getConfiguration());
		cd.setIWorkflow(instantiate(DefaultWorkflow.class));
		// cd.getIWorkflow().setStartupDate(new Date());
		cd.getIWorkflow().setConfiguration(getConfiguration());
		cd.getIWorkflow().setName(
		        FileTools.prependDefaultDirs("workflow.xml", null,
		                cd.getIWorkflow().getStartupDate()).getAbsolutePath());
		cd.setInput(Factory.prepareInputData());
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

	private void initThreadPools() {
		this.es = new ExecutorsManager(this.maxthreads);// Executors.newFixedThreadPool(this.maxthreads);
		this.auxPool = new ExecutorsManager(ExecutorType.SINGLETON);// Executors.newFixedThreadPool(this.maxthreads);
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
		try {
			final T t = c.newInstance();
			configureType(t);
			return t;
		} catch (final InstantiationException e) {
			Factory.getInstance().log.error(e.getLocalizedMessage());
		} catch (final IllegalAccessException e) {
			Factory.getInstance().log.error(e.getLocalizedMessage());
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

	public <T> T instantiateSingleton(final Class<T> c) {
		if ((c.getComponentType() == null) && (c.getConstructors().length == 0)
		        && !(c.isPrimitive())) {
			Factory.getInstance().log
			        .debug(
			                "Class of type {} has no component type, no public constructors and is not primitive!",
			                c.getClass().toString());
			try {
				final Method m = c.getMethod("getInstance", new Class<?>[0]);
				final Class<?> returnType = m.getReturnType();
				Factory.getInstance().log.debug("Return type of method is {}",
				        returnType.getName());
				final Class<? extends T> d = returnType.asSubclass(c);
				Factory.getInstance().log.debug(
				        "Return type as subclass is {}", d.getName());
				final Object o = m.invoke(null, new Object[0]);
				final T t = d.cast(o);
				configureType(t);
				return t;
			} catch (final SecurityException e) {
				Factory.getInstance().log.error(e.getLocalizedMessage());
			} catch (final NoSuchMethodException e) {
				Factory.getInstance().log.error(e.getLocalizedMessage());
			} catch (final IllegalArgumentException e) {
				e.printStackTrace();
				Factory.getInstance().log.error(e.getLocalizedMessage());
			} catch (final IllegalAccessException e) {
				Factory.getInstance().log.error(e.getLocalizedMessage());
			} catch (final InvocationTargetException e) {
				Factory.getInstance().log.error(e.getLocalizedMessage());
			}
		}
		throw new IllegalArgumentException(
		        "Class c: "
		                + c.getName()
		                + " could not be instantiated as a singleton by invoking static method getInstance()!");
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
		EvalTools.notNull(name, Factory.getInstance());
		final Configuration config = Factory.getInstance().getConfiguration();
		EvalTools.notNull(config, Factory.getInstance());
		if (config == null) {
			Factory.getInstance().log
			        .error("ArrayFactory has not been configured yet!");
			System.exit(-1);
		}
		// URL[] urls = null;
		Class<?> cls = null;
		try {
			// String usrCP = config.getString("user.class.path", "");
			// String javaCP = config.getString("java.class.path", "");
			// //final String[] t =
			// usrCP.split(System.getProperty("path.separator"));
			// final String[] s =
			// javaCP.split(System.getProperty("path.separator"));
			// int l = s.length;// + t.length;
			// urls = new URL[l];
			// int i = 0;
			// for (String str : t) {
			// urls[i] = new URL("file://" + str);
			// i++;
			// }
			// for (String str : s) {
			// urls[i] = new URL("file://" + str);
			// i++;
			// }
			// URLClassLoader scl = URLClassLoader.newInstance(urls, ClassLoader
			// .getSystemClassLoader());
			Factory.getInstance().log.debug("Loading class {}", name);
			cls = Class.forName(name);// , true, scl);
			EvalTools.notNull(cls, Factory.getInstance());
			return cls;
			// } catch (MalformedURLException e) {
			// Factory.getInstance().log.error(e.getLocalizedMessage());
		} catch (final ClassNotFoundException e) {
			Factory.getInstance().log.error(e.getLocalizedMessage());
		}
		return cls;
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
		NetcdfFileCache.clearCache(true);
		NetcdfFileCache.exit();
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
	 * Submit a Runnable job to the ArrayFactory
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
