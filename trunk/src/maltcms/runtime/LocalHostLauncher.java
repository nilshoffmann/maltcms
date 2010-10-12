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
package maltcms.runtime;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowFileResult;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.tools.EvalTools;
import cross.tools.FileTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class LocalHostLauncher implements Thread.UncaughtExceptionHandler,
        IListener<IEvent<IWorkflowResult>>, Runnable, PropertyChangeListener,
        HyperlinkListener {

	public class LocalHostMaltcmsProcess extends
	        SwingWorker<IWorkflow, IWorkflowResult> implements
	        IListener<IEvent<IWorkflowResult>>, IEventSource<IWorkflowResult> {

		private Configuration cfg = null;

		private final EventSource<IWorkflowResult> es = new EventSource<IWorkflowResult>();

		public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
			this.es.addListener(l);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public IWorkflow doInBackground() throws Exception {
			LocalHostLauncher.this.log.info("Starting up Maltcms!");
			LocalHostLauncher.this.log.info("Running Maltcms version {}",
			        this.cfg.getString("application.version"));
			LocalHostLauncher.this.log.info("Configuring Factory");

			Factory.getInstance().configure(this.cfg);
			// Set up the command sequence
			LocalHostLauncher.this.log.info("Setting up command sequence");
			final ICommandSequence cs = Factory.getInstance()
			        .createCommandSequence();
			LocalHostLauncher.this.startup = cs.getIWorkflow().getStartupDate();
			final AFragmentCommand[] commands = cs.getCommands().toArray(
			        new AFragmentCommand[] {});
			final float nsteps = commands.length;
			cs.getIWorkflow().addListener(this);
			EvalTools.notNull(cs, cs);
			float step = 0;
			// Evaluate until empty
			LocalHostLauncher.this.log.info("Executing command sequence");
			int progress = 0;
			while (cs.hasNext()) {
				if (isCancelled()) {
					LocalHostLauncher.this.log
					        .warn("Thread was cancelled, bailing out");
					Factory.getInstance().shutdownNow();
					throw new InterruptedException();
				}
				progress = (int) ((step / nsteps) * 100.0f);
				final int progv = progress;
				LocalHostLauncher.this.log.debug("Progress: {}", progress);
				final Runnable r = new Runnable() {

					@Override
					public void run() {
						setProgress(progv);
					}
				};
				SwingUtilities.invokeLater(r);
				cs.next();
				step++;
			}
			progress = (int) ((step / nsteps) * 100.0f);
			final int progv = progress;
			final Runnable r = new Runnable() {

				@Override
				public void run() {
					setProgress(progv);
				}
			};
			SwingUtilities.invokeLater(r);
			LocalHostLauncher.this.log.info("Progress: {}", progress);
			LocalHostLauncher.shutdown(30, LocalHostLauncher.this.log);
			// Save configuration
			Factory.dumpConfig("runtime.properties",
			        LocalHostLauncher.this.startup);
			// Save workflow
			final IWorkflow iw = cs.getIWorkflow();
			iw.save();
			return iw;
		}

		public void fireEvent(final IEvent<IWorkflowResult> e) {
			this.es.fireEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see cross.event.IListener#listen(cross.event.IEvent)
		 */
		/**
		 * Relay method, calling all registered Listeners, if an event is
		 * received from a Workflow.
		 */
		@Override
		public void listen(final IEvent<IWorkflowResult> v) {
			this.publish(v.get());
			this.es.fireEvent(v);
		}

		public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
			this.es.removeListener(l);
		}

		public void setConfiguration(final Configuration cfg) {
			this.cfg = cfg;
			LocalHostLauncher.this.log.debug("Using configuration");
			LocalHostLauncher.this.log.debug("{}", ConfigurationUtils
			        .toString(cfg));
		}

	};

	protected enum State {
		UNDEFINED, INIT, MONITORING, FINISHED, FAILURE
	}

	private static void handleRuntimeException(final Logger log,
	        final Throwable npe, final LocalHostLauncher lhl, final Date d) {
		int ecode;
		LocalHostLauncher.shutdown(1, log);
		log.error("Caught Throwable, returning to console!");
		log.error(npe.getLocalizedMessage());
		npe.printStackTrace(System.err);
		ecode = 1;
		// Save configuration
		Factory.dumpConfig("runtime.properties", d);
		final JEditorPane jpa = new JEditorPane("text/html", "");
		final StringBuffer sb = new StringBuffer();
		final StringWriter sw = new StringWriter();
		npe.printStackTrace(new PrintWriter(sw));
		final String npes = sw.toString().replaceAll("\\n", "<br />");
		sb.append("<h2>Caught the following exception:</h2>");
		sb.append("<p>" + npes + "</p>");
		sb
		        .append("<h2>Please read the exception carefully, it usually gives a good hint to what has happened.</h2>");
		sb
		        .append("<p>If the exception has not printed anything useful, please submit a bug report including the exception's output to:</p>");
		sb
		        .append("<p><a href=\"https://sourceforge.net/tracker/?func=add&group_id=251287&atid=1127435\">https://sourceforge.net/tracker/?func=add&group_id=251287&atid=1127435</a></p>");
		final File configFile = new File(FileTools
		        .prependDefaultDirsWithPrefix("", Factory.class, d),
		        "runtime.properties");
		sb.append("<p>Please attach a copy of <a href=\""
		        + configFile.getAbsolutePath()
		        + "\">the current configuration</a> (at "
		        + configFile.getAbsolutePath() + ") to your report!<br />");
		sb.append("Your report will help improve ChromA, thank you!</p>");
		jpa.setText(sb.toString());
		jpa.addHyperlinkListener(lhl);
		jpa.setEditable(false);
		JOptionPane.showMessageDialog(null, new JScrollPane(jpa),
		        "Error Report", JOptionPane.ERROR_MESSAGE);
		System.exit(ecode);
	}

	private static void shutdown(final long seconds, final Logger log) {
		// Shutdown application thread
		Factory.getInstance().shutdown();
		try {
			Factory.awaitTermination(seconds, TimeUnit.MINUTES);
		} catch (final InterruptedException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	private final PropertiesConfiguration cfg;
	private PropertiesConfiguration defaultConfig;
	private final Logger log = Logging.getLogger(this);
	private JProgressBar progressBar = null;
	private JList jl = null;
	private DefaultListModel dlm = null;
	private final Vector<IWorkflowFileResult> buffer = new Vector<IWorkflowFileResult>(
	        5);
	private LocalHostMaltcmsProcess lhmp = null;
	private JTextArea jt;

	private Container jf = null;

	private JPanel progress;

	private Date startup = null;

	public LocalHostLauncher(final PropertiesConfiguration userConfig,
	        final Container jf) {
		this.cfg = userConfig;
		this.jf = jf;
		this.lhmp = new LocalHostMaltcmsProcess();
		this.lhmp.setConfiguration(getDefaultConfig());
	}

	/**
	 * Builds a composite configuration, adding default configuration and then
	 * the system configuration.
	 * 
	 * @return
	 */
	public Configuration getDefaultConfig() {
		URL defaultConfigLocation = null;
		defaultConfigLocation = getClass().getClassLoader().getResource(
		        "cfg/default.properties");
		try {
			defaultConfigLocation = new File("cfg/default.properties").toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Using default config location: "
		        + defaultConfigLocation.toString());
		try {
			this.defaultConfig = new PropertiesConfiguration(
			        defaultConfigLocation);
		} catch (final ConfigurationException e1) {
			this.log.warn(e1.getLocalizedMessage());
		}
		final CompositeConfiguration cfg = new CompositeConfiguration();
		cfg.addConfiguration(this.cfg);
		cfg.addConfiguration(this.defaultConfig);
		cfg.addConfiguration(new SystemConfiguration());
		// add defaults last, so if first config redeclares a property it is
		// used.

		return cfg;
	}

	public LocalHostMaltcmsProcess getProcess() {
		return this.lhmp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event
	 * .HyperlinkEvent)
	 */
	@Override
	public void hyperlinkUpdate(final HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(e.getURL().toURI());
				} catch (final IOException e1) {
					this.log.error("{}", e1.getLocalizedMessage());
				} catch (final URISyntaxException e1) {
					this.log.error("{}", e1.getLocalizedMessage());
				}
			} else {
				JOptionPane.showMessageDialog(null, "Please view link at "
				        + e.getURL().toString());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.event.IListener#listen(cross.event.IEvent)
	 */
	@Override
	public void listen(final IEvent<IWorkflowResult> v) {
		if (v.get() instanceof DefaultWorkflowResult) {
			final DefaultWorkflowResult dwr = ((DefaultWorkflowResult) v.get());
			this.log.debug("Received event: {}", dwr.getFile()
			        .getAbsolutePath());
			if (this.dlm != null) {
				final Runnable r = new Runnable() {

					@Override
					public void run() {
						if (!LocalHostLauncher.this.buffer.isEmpty()) {
							for (final IWorkflowFileResult iwr : LocalHostLauncher.this.buffer) {
								LocalHostLauncher.this.dlm.addElement(iwr
								        .getWorkflowSlot()
								        + ": "
								        + iwr.getIWorkflowElement().getClass()
								                .getSimpleName()
								        + " created "
								        + iwr.getFile().getName());
							}
							LocalHostLauncher.this.buffer.clear();
						} else {
							LocalHostLauncher.this.dlm.addElement(dwr
							        .getWorkflowSlot()
							        + ": "
							        + dwr.getIWorkflowElement().getClass()
							                .getSimpleName()
							        + " created "
							        + dwr.getFile().getName());
						}

					}
				};
				SwingUtilities.invokeLater(r);

			} else {
				this.buffer.add(dwr);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		this.log.debug("Received PropertyChangeEvent {}", evt);
		if ("progress" == evt.getPropertyName()) {
			final int progress = (Integer) evt.getNewValue();
			this.log.debug("progress event with value: {}", progress);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					LocalHostLauncher.this.progressBar.setValue(progress);

				}
			});

		}

	}

	public void run() {
		if (this.progressBar == null) {
			this.progressBar = new JProgressBar(0, 100);
			this.progressBar.setStringPainted(true);
			this.progress = new JPanel();
			final JPanel jp = new JPanel(new GridLayout(2, 1));
			this.jl = new JList();
			this.dlm = new DefaultListModel();
			this.jl.setModel(this.dlm);
			jp.setBorder(BorderFactory
			        .createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
			jp.add(new JScrollPane(this.jl));
			this.progress.add(new JLabel("Overall progress:"),
			        BorderLayout.SOUTH);
			this.progress.add(this.progressBar, BorderLayout.SOUTH);
			jp.add(this.progress);
			this.jf.add(jp);
			if (this.jf instanceof JSplitPane) {
				((JSplitPane) this.jf).setDividerLocation(0.5);
			}
		}

		this.lhmp.addPropertyChangeListener(this);
		this.lhmp.addListener(this);
		this.lhmp.execute();
		IWorkflow iw;
		final Date d = null;
		try {
			iw = getProcess().get();
			final File result = new File(iw.getName()).getParentFile();
			if (Desktop.isDesktopSupported()) {
				try {
					if (result.exists()) {
						Desktop.getDesktop().browse(result.toURI());
					} else {
						Desktop.getDesktop().browse(
						        result.getParentFile().toURI());
					}
				} catch (final IOException e) {
					this.log.error("{}", "Could not access Desktop object");
				}
			} else {
				JOptionPane
				        .showMessageDialog(null, "Please view your results at "
				                + result.getParentFile());
			}
			System.exit(0);
		} catch (final CancellationException e1) {
			this.jl.setModel(new DefaultListModel());
			this.progressBar.setValue(0);
			this.log.error(e1.getLocalizedMessage());
			return;
		} catch (final InterruptedException e1) {
			LocalHostLauncher.handleRuntimeException(this.log, e1, this,
			        this.startup);
		} catch (final ExecutionException e1) {
			LocalHostLauncher.handleRuntimeException(this.log, e1, this,
			        this.startup);
		}
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
		LocalHostLauncher.handleRuntimeException(this.log, e, this,
		        this.startup);
	}

}
