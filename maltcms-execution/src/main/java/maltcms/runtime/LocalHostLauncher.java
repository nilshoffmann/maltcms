/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.runtime;

import cross.Factory;
import cross.datastructures.tools.FileTools;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowFileResult;
import cross.datastructures.workflow.IWorkflowResult;
import cross.event.IEvent;
import cross.event.IListener;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;

/**
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Deprecated
public class LocalHostLauncher implements Thread.UncaughtExceptionHandler,
    IListener<IEvent<IWorkflowResult>>, Runnable, PropertyChangeListener,
    HyperlinkListener {

    protected enum State {

        UNDEFINED, INIT, MONITORING, FINISHED, FAILURE
    }

    private static void handleRuntimeException(final Logger log,
        final Throwable npe, final LocalHostLauncher lhl, final Date d) {
        int ecode;
        LocalHostLauncher.shutdown(1, log);
        log.error("Caught Throwable, aborting execution!");
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
        sb.append("<h2>Please read the exception carefully, it usually gives a good hint to what has happened.</h2>");
        sb.append("<p>If the exception has not printed anything useful, please submit a bug report including the exception's output to:</p>");
        sb.append("<p><a href=\"https://sourceforge.net/tracker/?func=add&group_id=251287&atid=1127435\">https://sourceforge.net/tracker/?func=add&group_id=251287&atid=1127435</a></p>");
        final File configFile = new File(FileTools.prependDefaultDirsWithPrefix("", Factory.class, d),
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
        if (lhl.isExitOnException()) {
            System.exit(ecode);
        }
    }

    private static void shutdown(final long seconds, final Logger log) {
        // Shutdown application thread
        Factory.getInstance().shutdown();
        Factory.getInstance().awaitTermination(seconds, TimeUnit.MINUTES);
    }
    private final PropertiesConfiguration cfg;
    private PropertiesConfiguration defaultConfig;
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
    private boolean exitOnException = false;
    private boolean navigateToResults = true;

    public LocalHostLauncher(final PropertiesConfiguration userConfig,
        final Container jf, final boolean exitOnException) {
        this.cfg = userConfig;
        this.jf = jf;
        this.lhmp = new LocalHostMaltcmsProcess();
        this.lhmp.setConfiguration(getDefaultConfig());
        this.exitOnException = exitOnException;
    }

    public void setNavigateToResults(boolean b) {
        this.navigateToResults = b;
    }

    public boolean isNavigateToResults() {
        return this.navigateToResults;
    }

    public boolean isExitOnException() {
        return this.exitOnException;
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
            this.defaultConfig = new PropertiesConfiguration(defaultConfigLocation);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            try {
                defaultConfigLocation = new File("cfg/default.properties").toURI().toURL();
                this.defaultConfig = new PropertiesConfiguration(
                    defaultConfigLocation);
            } catch (MalformedURLException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            } catch (final ConfigurationException e1) {
                this.log.warn(e1.getLocalizedMessage());
            }
        }

        System.out.println("Using default config location: "
            + defaultConfigLocation.toString());

        final CompositeConfiguration ccfg = new CompositeConfiguration();
        ccfg.addConfiguration(this.cfg);
        ccfg.addConfiguration(this.defaultConfig);
        ccfg.addConfiguration(new SystemConfiguration());
        // add defaults last, so if first config redeclares a property it is
        // used.

        return ccfg;
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
            this.log.debug("Received event: {}", dwr.getFile().getAbsolutePath());
            if (this.dlm != null) {
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (!LocalHostLauncher.this.buffer.isEmpty()) {
                            for (final IWorkflowFileResult iwr : LocalHostLauncher.this.buffer) {
                                LocalHostLauncher.this.dlm.addElement(iwr.getWorkflowSlot()
                                    + ": "
                                    + iwr.getWorkflowElement().getClass().getSimpleName()
                                    + " created "
                                    + iwr.getFile().getName());
                            }
                            LocalHostLauncher.this.buffer.clear();
                        } else {
                            LocalHostLauncher.this.dlm.addElement(dwr.getWorkflowSlot()
                                + ": "
                                + dwr.getWorkflowElement().getClass().getSimpleName()
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
            jp.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
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
            if (isNavigateToResults()) {
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
                    JOptionPane.showMessageDialog(null, "Please view your results at "
                        + result.getParentFile());
                }
                shutdown(30, log);
            }
            //System.exit(0);
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
