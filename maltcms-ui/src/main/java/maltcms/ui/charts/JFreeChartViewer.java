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
package maltcms.ui.charts;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.ui.TextAnchor;
import ucar.ma2.Array;

/**
 * <p>JFreeChartViewer class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class JFreeChartViewer extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -830245788879399345L;
    private JTabbedPane jtp = null;
    private File lastOpenDir = null;

    /**
     * <p>Getter for the field <code>jtp</code>.</p>
     *
     * @return a {@link javax.swing.JTabbedPane} object.
     */
    public JTabbedPane getJtp() {
        return jtp;
    }

    /**
     * <p>Setter for the field <code>jtp</code>.</p>
     *
     * @param jtp a {@link javax.swing.JTabbedPane} object.
     */
    public void setJtp(JTabbedPane jtp) {
        this.jtp = jtp;
    }

    /**
     * <p>Getter for the field <code>jmb</code>.</p>
     *
     * @return a {@link javax.swing.JMenuBar} object.
     */
    public JMenuBar getJmb() {
        return jmb;
    }

    /**
     * <p>Setter for the field <code>jmb</code>.</p>
     *
     * @param jmb a {@link javax.swing.JMenuBar} object.
     */
    public void setJmb(JMenuBar jmb) {
        this.jmb = jmb;
    }

    /**
     * <p>Getter for the field <code>fileMenu</code>.</p>
     *
     * @return a {@link javax.swing.JMenu} object.
     */
    public JMenu getFileMenu() {
        return fileMenu;
    }

    /**
     * <p>Setter for the field <code>fileMenu</code>.</p>
     *
     * @param fileMenu a {@link javax.swing.JMenu} object.
     */
    public void setFileMenu(JMenu fileMenu) {
        this.fileMenu = fileMenu;
    }
    private JMenuBar jmb = null;
    private JMenu fileMenu = null;
    private JLabel status = null;

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link javax.swing.JLabel} object.
     */
    public JLabel getStatus() {
        return status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status1 a {@link javax.swing.JLabel} object.
     */
    public void setStatus(JLabel status1) {
        this.status = status1;
    }

    /**
     * <p>Constructor for JFreeChartViewer.</p>
     */
    public JFreeChartViewer() {
        CompositeConfiguration cfg = new CompositeConfiguration();
        cfg.addConfiguration(new SystemConfiguration());
        try {
            PropertiesConfiguration pcfg = new PropertiesConfiguration(
                    JFreeChartViewer.class.getClassLoader().getResource(
                            "cfg/default.properties"));
            cfg.addConfiguration(pcfg);
            Factory.getInstance().configure(cfg);
        } catch (ConfigurationException e) {
            
            log.warn(e.getLocalizedMessage());
        }

        this.jtp = new JTabbedPane();

        final FileOpenAction fopen = new FileOpenAction("Load Chart");
        final ChromatogramOpenAction coa = new ChromatogramOpenAction(
                "Generate Chart");
        fopen.setParent(this);
        this.jmb = new JMenuBar();
        this.fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem(fopen));
        fileMenu.add(new JMenuItem(coa));
        this.jmb.add(fileMenu);
        this.status = new JLabel("Status");
        this.add(this.status, BorderLayout.SOUTH);
        setJMenuBar(this.jmb);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(this.jtp);
    }

    /**
     * <p>addChart.</p>
     *
     * @param c a {@link org.jfree.chart.JFreeChart} object.
     * @param name a {@link java.lang.String} object.
     */
    public void addChart(JFreeChart c, String name) {
        ChartPanel cp2 = new ChartPanel(c);
        cp2.addChartMouseListener(new ChartPanelMouseListener(cp2));
        CrosshairOverlay cco = new CrosshairOverlay();
        // cp2.addChartMouseListener(cco);
        cp2.addOverlay(cco);
        cp2.setHorizontalAxisTrace(true);
        cp2.setVerticalAxisTrace(true);
        getJtp().addTab(name, cp2);
        ChartSerializeAction csa = new ChartSerializeAction("Serialize Chart");
        csa.setJfc(c);
        cp2.getPopupMenu().add(csa);
    }

    /**
     * <p>getActiveChartPanel.</p>
     *
     * @return a {@link org.jfree.chart.ChartPanel} object.
     */
    public ChartPanel getActiveChartPanel() {
        Object o = getJtp().getTabComponentAt(getJtp().getSelectedIndex());
        log.info("class: " + o.getClass().getName());
        return (ChartPanel) o;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        final JFrame jf2 = new JFreeChartViewer();
        System.setProperty("log4j.configuration", "cfg/log4j.properties");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                jf2.setVisible(true);

            }
        });

    }

    public class ChartSerializeAction extends AbstractAction {

        /**
         *
         */
        public ChartSerializeAction() {
            super();
            
        }

        /**
         * @param name
         * @param icon
         */
        public ChartSerializeAction(String name, Icon icon) {
            super(name, icon);
            
        }

        /**
         * @param name
         */
        public ChartSerializeAction(String name) {
            super(name);
            
        }
        /**
         *
         */
        private static final long serialVersionUID = 425194552644224115L;
        private JFreeChart jfc = null;

        public JFreeChart getJfc() {
            return jfc;
        }

        public void setJfc(JFreeChart jfc) {
            this.jfc = jfc;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    log.info("Running chart serialize action!");
                    JFileChooser fc = new JFileChooser(lastOpenDir);
                    fc.setMultiSelectionEnabled(false);
                    int type = fc.showSaveDialog(getParent());
                    if (type == JFileChooser.APPROVE_OPTION) {
                        final File f = fc.getSelectedFile();
                        lastOpenDir = f.getParentFile();
                        try {
                            try (ObjectOutputStream oos = new ObjectOutputStream(
                                    new BufferedOutputStream(
                                            new FileOutputStream(f)))) {
                                        oos.writeObject(getJfc());
                                        oos.flush();
                                    }
                        } catch (FileNotFoundException e) {
               
                            log.warn(e.getLocalizedMessage());
                        } catch (IOException e) {
               
                            log.warn(e.getLocalizedMessage());
                        }
                    } else {
                        log.info("Aborted by user");
                        // System.exit(-1);
                    }

                }
            };
            SwingUtilities.invokeLater(r);

        }
    }

    public class ChromatogramOpenAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 5547094237664337509L;

        /**
         *
         */
        public ChromatogramOpenAction() {
            super();
            
        }

        /**
         * @param name
         * @param icon
         */
        public ChromatogramOpenAction(String name, Icon icon) {
            super(name, icon);
            
        }

        /**
         * @param name
         */
        public ChromatogramOpenAction(String name) {
            super(name);
            
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    log.info("Running Chromatogram open action!");
                    final JFileChooser jfc = new JFileChooser(lastOpenDir);
                    jfc.setMultiSelectionEnabled(true);
                    jfc.setFileFilter(new FileFilter() {
                        @Override
                        public String getDescription() {
                            List<String> l = Factory.getInstance()
                                    .getDataSourceFactory()
                                    .getSupportedFormats();
                            StringBuilder sb = new StringBuilder();
                            for (String s : l) {
                                sb.append("." + s + ", ");
                            }
                            return sb.toString().trim();
                        }

                        @Override
                        public boolean accept(File f) {
                            if (f.isDirectory()) {
                                return true;
                            }
                            for (String s : Factory.getInstance()
                                    .getDataSourceFactory()
                                    .getSupportedFormats()) {
                                if (f.getAbsolutePath().toLowerCase().endsWith(
                                        s)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                    int type = jfc.showOpenDialog(getParent());
                    if (type == JFileChooser.APPROVE_OPTION) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                final File[] f = jfc.getSelectedFiles();
                                final String[] labels = new String[f.length];
                                final Array[] arrays = new Array[f.length];
                                final Array[] domains = new Array[f.length];
                                int i = 0;
                                VariableSelectionPanel vsp = new VariableSelectionPanel();
                                IFileFragment fragment = new FileFragment(f[0]);
                                ArrayList<String> vars = new ArrayList<>();
                                String[] cfgvars = Factory.getInstance()
                                        .getConfiguration().getStringArray(
                                                "default.vars");
                                vars.addAll(Arrays.asList(cfgvars));
                                // for(String var:vars) {
                                // try{
                                // fragment.getChild(var, true);
                                // }catch(ResourceNotAvailableException r){
                                // vars.remove(var);
                                // }
                                // }
                                vsp.setAvailableVariables(vars
                                        .toArray(new String[]{}));
                                javax.swing.JOptionPane.showMessageDialog(
                                        getParent(), vsp);
                                String domainVar = vsp
                                        .getSelectedDomainVariable();
                                String valueVar = vsp
                                        .getSelectedValuesVariable();
                                for (File file : f) {
                                    lastOpenDir = file.getParentFile();
                                    Factory.getInstance().getConfiguration()
                                            .setProperty("output.basedir",
                                                    file.getParent());
                                    Factory.getInstance().getConfiguration()
                                            .setProperty(
                                                    "user.name",
                                                    System.getProperty(
                                                            "user.name", ""));
                                    fragment = new FileFragment(
                                            file);
                                    labels[i] = fragment.getName();
                                    arrays[i] = fragment.getChild(valueVar)
                                            .getArray();
                                    // Factory.getInstance().getConfiguration().getString("var.total_intensity","total_intensity")).getArray();
                                    if (!domainVar.equals("")) {
                                        domains[i] = fragment.getChild(
                                                domainVar).getArray();
                                    }
                                    // Factory.getInstance().getConfiguration().getString("var.scan_acquisition_time","scan_acquisition_time")).getArray();
                                    i++;
                                }
                                XYChart xyc = null;
                                if (!domainVar.equals("")) {
                                    xyc = new XYChart(
                                            f.length > 1 ? lastOpenDir
                                            .getName() : f[0].getName(),
                                            labels, arrays, domains, "time[s]",
                                            "value");
                                } else {
                                    xyc = new XYChart(
                                            f.length > 1 ? lastOpenDir
                                            .getName() : f[0].getName(),
                                            labels, arrays, "time[s]", "value");
                                }
                                JFreeChart jfc2 = new JFreeChart(xyc.create());
                                addChart(jfc2, lastOpenDir.getName());
                            }
                        };
                        SwingUtilities.invokeLater(r);
                    } else {
                        log.info("Aborted by user");
                        // System.exit(-1);
                    }

                }
            };
            SwingUtilities.invokeLater(r);

        }
    }

    public class FileOpenAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -8133108907581040380L;
        private Component parent = null;

        public void setParent(Component c) {
            this.parent = c;
        }

        public Component getParent() {
            return this.parent;
        }

        /**
         *
         */
        public FileOpenAction() {
            super();
            
        }

        /**
         * @param name
         * @param icon
         */
        public FileOpenAction(String name, Icon icon) {
            super(name, icon);
            
        }

        /**
         * @param name
         */
        public FileOpenAction(String name) {
            super(name);
            
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    log.info("Running File open action!");
                    JFileChooser jfc = new JFileChooser(lastOpenDir);
                    jfc.setMultiSelectionEnabled(true);
                    jfc.setFileFilter(new FileFilter() {
                        @Override
                        public String getDescription() {
                            return "Displays files with .serialized extension";
                        }

                        @Override
                        public boolean accept(File f) {
                            if (f.getAbsolutePath().endsWith(".serialized")) {
                                return true;
                            }
                            return false;
                        }
                    });
                    int type = jfc.showOpenDialog(getParent());
                    if (type == JFileChooser.APPROVE_OPTION) {
                        final File[] f = jfc.getSelectedFiles();
                        // Runnable r = new Runnable(){
                        //
                        // @Override
                        // public void run() {
                        for (File file : f) {
                            lastOpenDir = file.getParentFile();
                            try {
                                JFreeChart jfc1;
                                try (ObjectInputStream ois = new ObjectInputStream(
                                        new BufferedInputStream(
                                                new FileInputStream(file)))) {
                                            jfc1 = (JFreeChart) ois.readObject();
                                        }
                                        addChart(jfc1, file.getName());
                            } catch (FileNotFoundException e) {
                   
                                log.warn(e.getLocalizedMessage());
                            } catch (IOException | ClassNotFoundException e) {
                   
                                log.warn(e.getLocalizedMessage());
                            }
                        }

                        // jf2.pack();
                        // }
                        // };
                        // SwingUtilities.invokeLater(r);
                    } else {
                        log.info("Aborted by user");
                        // System.exit(-1);
                    }

                }
            };
            SwingUtilities.invokeLater(r);

        }
    }

    public class XYAnnotationAdder {

        public void addAnnotation(final ChartPanel cp, final JFreeChart jfc,
                final XYItemEntity xyie, final int x, final int y) {
            final double xd = xyie.getDataset().getXValue(
                    xyie.getSeriesIndex(), xyie.getItem());
            final double yd = xyie.getDataset().getYValue(
                    xyie.getSeriesIndex(), xyie.getItem());
            // Runnable r = new Runnable() {

            // @Override
            // public void run() {
            final JPopupMenu jpm = new JPopupMenu("Add annotation");
            final JTextArea jta = new JTextArea("x=" + xd + ", y=" + yd);
            AbstractAction applyAction = new AbstractAction("Apply") {
                /**
                 *
                 */
                private static final long serialVersionUID = -135368123552109960L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!jta.getText().equals("")) {
                        final XYPointerAnnotation xya = new XYPointerAnnotation(
                                jta.getText(), xd, yd, -0.6);
                        xya.setTipRadius(0);
                        xya.setTextAnchor(TextAnchor.BASELINE_LEFT);
                        // xya.setBaseRadius(5.0);
                        // xya.setArrowLength(10.0);
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                jfc.getXYPlot().addAnnotation(xya);

                            }
                        };
                        SwingUtilities.invokeLater(r);
                    }
                    jpm.setVisible(false);
                }
            };
            jpm.add(jta);
            jpm.add(applyAction);
            jpm.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    jpm.setVisible(false);

                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
            jpm.show(cp, x, y);

            // }
            // };
            // SwingUtilities.invokeLater(r);
        }
    }

    public class ChartPanelMouseListener implements ChartMouseListener {

        private ChartPanel cp;

        public ChartPanelMouseListener(ChartPanel cp1) {
            this.cp = cp1;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.jfree.chart.ChartMouseListener#chartMouseClicked(org.jfree.chart
         * .ChartMouseEvent)
         */
        @Override
        public void chartMouseClicked(ChartMouseEvent arg0) {
            if (arg0.getEntity() != null
                    && arg0.getEntity() instanceof XYItemEntity) {
                log.info(arg0.getEntity().getClass().getName());
                XYAnnotationAdder xya = new XYAnnotationAdder();
                xya.addAnnotation(this.cp, arg0.getChart(), (XYItemEntity) arg0
                        .getEntity(), arg0.getTrigger().getX(), arg0
                        .getTrigger().getY());
            }
            log.info(arg0.getSource().getClass().getName());
            log.info(arg0.getTrigger().getClass().getName());
            log.info(arg0.getTrigger().getX() + " "
                    + arg0.getTrigger().getY());
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.jfree.chart.ChartMouseListener#chartMouseMoved(org.jfree.chart
         * .ChartMouseEvent)
         */
        @Override
        public void chartMouseMoved(final ChartMouseEvent arg0) {
            // log.info("Mouse moved");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (arg0.getEntity() != null) {
                        log.info("Found an entity");
                        getStatus().setText(
                                "Status: " + arg0.getEntity().toString());
                        // jtt.setLocation(arg0.getTrigger().getX(),
                        // arg0.getTrigger().getY());
                        // jtt.setVisible(true);
                    } else {
                        // if(jtt!=null){
                        getStatus().setText("Status: ");
                        // }
                    }

                }
            };
            SwingUtilities.invokeLater(r);

        }
    }
}
