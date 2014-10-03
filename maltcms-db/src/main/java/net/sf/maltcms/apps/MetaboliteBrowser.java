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
package net.sf.maltcms.apps;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ext.DatabaseFileLockedException;
import com.db4o.ext.DatabaseReadOnlyException;
import com.db4o.ext.Db4oIOException;
import com.db4o.ext.IncompatibleFileFormatException;
import com.db4o.ext.InvalidPasswordException;
import com.db4o.ext.OldFormatException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import maltcms.db.ui.MetaboliteQueryPanel;
import maltcms.db.ui.MetaboliteView;
import maltcms.db.ui.MetaboliteViewModel;
//import net.sf.maltcms.apps.Maltcms;

/**
 * <p>MetaboliteBrowser class.</p>
 *
 * @author hoffmann
 * 
 */
public final class MetaboliteBrowser extends JFrame {

    private HashMap<Integer, JCheckBoxMenuItem> selectTableHeaders = new HashMap<>();
    private MetaboliteViewModel mvm;

    /**
     * <p>Constructor for MetaboliteBrowser.</p>
     */
    public MetaboliteBrowser() {
    }

    private class Credentials {

        private String location = null;
        private String passwd = null;

        void setLocation(String loc) {
            this.location = loc;
        }

        void setPassword(String passwd) {
            this.passwd = passwd;
        }

        String getLocation() {
            return this.location;
        }

        String getPassword() {
            return this.passwd;
        }
    }

    /**
     * <p>getObjectContainer.</p>
     *
     * @param c a {@link net.sf.maltcms.apps.MetaboliteBrowser.Credentials} object.
     * @return a {@link com.db4o.ObjectContainer} object.
     * @throws java.net.MalformedURLException if any.
     * @throws com.db4o.ext.Db4oIOException if any.
     * @throws com.db4o.ext.DatabaseFileLockedException if any.
     * @throws com.db4o.ext.IncompatibleFileFormatException if any.
     * @throws com.db4o.ext.OldFormatException if any.
     * @throws com.db4o.ext.DatabaseReadOnlyException if any.
     * @throws com.db4o.ext.InvalidPasswordException if any.
     * @throws java.net.URISyntaxException if any.
     */
    public static final ObjectContainer getObjectContainer(Credentials c)
            throws MalformedURLException, Db4oIOException,
            DatabaseFileLockedException, IncompatibleFileFormatException,
            OldFormatException, DatabaseReadOnlyException,
            InvalidPasswordException, URISyntaxException {
        ObjectContainer oc = null;
        // final String[] methods = new String[args.length-1];
        // System.arraycopy(args, 1, methods, 0, methods.length);
        URL url = null;
        File f = null;
        try {
            url = new URL(c.getLocation());
            f = new File(url.toURI());
        } catch (MalformedURLException e) {
            if (f == null) {
                f = new File(c.getLocation());
            }
        }

        if (f.exists()) {
            System.out.println("Opening DB locally as file!");
            oc = Db4o.openFile(f.getAbsolutePath());// oc.query(llap);
        } else {
            // System.out.println(url.getAuthority());
            // System.out.println(url.getHost());
            // System.out.println(url.getFile());
            // System.out.println(url.getDefaultPort());
            // System.out.println(url.getPath());
            // System.out.println(url.getPort());
            // System.out.println(url.getProtocol());
            // System.out.println(url.getQuery());
            // System.out.println(url.getRef());
            // System.out.println(url.getUserInfo());
            System.out.println("Opening DB via Client!");
            oc = Db4o.openClient(url.getHost(), url.getPort(), url.getUserInfo(), c.getPassword());
        }
        return oc;
    }

    /**
     * <p>create.</p>
     *
     * @param oc a {@link com.db4o.ObjectContainer} object.
     * @param mb a {@link net.sf.maltcms.apps.MetaboliteBrowser} object.
     */
    public static final void create(final ObjectContainer oc,
            final MetaboliteBrowser mb) {
//        Maltcms m = Maltcms.getInstance();
//        Factory.getInstance().configure(m.getDefaultConfig());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Using the following methods "
                        + "for data display:");
                final MetaboliteViewModel mvm = new MetaboliteViewModel(oc);
                mb.setModel(mvm);
                mb.setupUI();
            }
        });
    }

    /**
     * <p>setModel.</p>
     *
     * @param mvm2 a {@link maltcms.db.ui.MetaboliteViewModel} object.
     */
    protected void setModel(MetaboliteViewModel mvm2) {
        this.mvm = mvm2;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {

        // opening dialog
        final MetaboliteBrowser mb = new MetaboliteBrowser();
        final Credentials c = mb.new Credentials();
        // if (args.length == 0) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JPanel jp1 = new JPanel();
                JLabel jl = new JLabel("Enter file or server url:");
                final JTextField jtf = new JTextField(
                        "http://default@127.0.0.1:8888");
                jp1.add(jl, BorderLayout.WEST);
                jp1.add(jtf, BorderLayout.CENTER);
                final JButton openFile = new JButton(new AbstractAction("...") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        JFileChooser jfc = new JFileChooser();
                        jfc.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                if (file.isDirectory()) {
                                    return true;
                                }
                                return file.getName().toLowerCase().endsWith("db4o");
                            }

                            @Override
                            public String getDescription() {
                                return "db4o";
                            }
                        });
                        int res = jfc.showOpenDialog(mb);
                        if (res == JFileChooser.APPROVE_OPTION) {
                            if (jfc.getSelectedFile().isDirectory()) {
                                jfc.setVisible(false);
                                actionPerformed(ae);
                            }
                            jtf.setText(jfc.getSelectedFile().getAbsolutePath());
                        }
                    }
                });
                jp1.add(openFile, BorderLayout.EAST);
                JPanel jp2 = new JPanel();
                final JPasswordField jpf = new JPasswordField(10);
                JLabel jpfLabel = new JLabel("Password:");
                jp2.add(jpfLabel, BorderLayout.WEST);
                jp2.add(jpf, BorderLayout.CENTER);
                JPanel jp = new JPanel();
                jp.add(jp1, BorderLayout.NORTH);
                final JFrame jf = new JFrame("Open database");
                jf.setLayout(new GridLayout(3, 1));
                //jp.add(jp2, BorderLayout.CENTER);
                JButton jb = new JButton(new AbstractAction("Open") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                c.setPassword(new String(jpf.getPassword()));
                                c.setLocation(jtf.getText());
                                mb.setTitle("MetaboliteBrowser: " + jtf.getText());
                                if (c.getLocation() == null) {
                                    System.err.println("No db selected, exiting!");
                                    System.exit(1);
                                }
                                ObjectContainer oc;
                                try {
                                    oc = MetaboliteBrowser.getObjectContainer(c);
                                    jf.setVisible(false);
                                    // opening JFrame
                                    MetaboliteBrowser.create(oc, mb);
                                } catch (MalformedURLException | Db4oIOException | DatabaseFileLockedException | IncompatibleFileFormatException | OldFormatException | DatabaseReadOnlyException | InvalidPasswordException | URISyntaxException ex) {
                                    ex.printStackTrace();
                                }

                            }
                        };
                        SwingUtilities.invokeLater(r);
                    }
                });

//                jf.add(jb, BorderLayout.SOUTH);
                jf.add(jp);
                jf.add(jp2);
                JPanel jp3 = new JPanel();
                jp3.add(jb);
                jf.add(jp3);
                jf.setVisible(true);
                jf.setSize(400, 300);
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jf.pack();
                jf.setLocationRelativeTo(null);

            }
        };
        SwingUtilities.invokeLater(r);
        // JFileChooser jfc = new JFileChooser();
        // int option = jfc.showOpenDialog(null);
        // if (option == JFileChooser.APPROVE_OPTION) {
        // c.setLocation(jfc.getSelectedFile().getAbsolutePath());
        // }
        // } else {
        // c.setLocation(args[0]);
        // }

    }
    /**
     *
     */
    private static final long serialVersionUID = 6217168349675106268L;
    private JPanel qpanel = null;

    /**
     * generates the menu bar for selecting the shown table columns
     *
     * @param mvm a {@link maltcms.db.ui.MetaboliteViewModel} object.
     */
    public void generateMenuBar(final MetaboliteViewModel mvm) {

        JMenuBar menuBar = new JMenuBar();
        JMenu tableHeaderMenu = new JMenu("Select Table Headers");
        menuBar.add(tableHeaderMenu);
        selectTableHeaders.put(0, createCheckBoxMenuItem(mvm.COMMENTS));
        selectTableHeaders.put(1, createCheckBoxMenuItem(mvm.DATE));
        selectTableHeaders.put(2, createCheckBoxMenuItem(mvm.FORMULA));
        selectTableHeaders.put(3, createCheckBoxMenuItem(mvm.ID));
        selectTableHeaders.put(4, createCheckBoxMenuItem(mvm.MASSWEIGHT));
        selectTableHeaders.put(5, createCheckBoxMenuItem(mvm.MASSSPECTRUM));
        selectTableHeaders.put(6, createCheckBoxMenuItem(mvm.MAXINTENSITY));
        selectTableHeaders.put(7, createCheckBoxMenuItem(mvm.MAXINTNORM));
        selectTableHeaders.put(8, createCheckBoxMenuItem(mvm.MAXMASS));
        selectTableHeaders.put(9, createCheckBoxMenuItem(mvm.MININTENSITY));
        selectTableHeaders.put(10, createCheckBoxMenuItem(mvm.MININTNORM));
        selectTableHeaders.put(11, createCheckBoxMenuItem(mvm.MINMASS));
        selectTableHeaders.put(12, createCheckBoxMenuItem(mvm.NAME));
        selectTableHeaders.put(13, createCheckBoxMenuItem(mvm.RETINDEX));
        selectTableHeaders.put(14, createCheckBoxMenuItem(mvm.RETTIME));
        selectTableHeaders.put(15, createCheckBoxMenuItem(mvm.RETTIMEUNIT));
        selectTableHeaders.put(16, createCheckBoxMenuItem(mvm.SP));
        selectTableHeaders.put(17, createCheckBoxMenuItem(mvm.SCANINDEX));
        selectTableHeaders.put(18, createCheckBoxMenuItem(mvm.SHORTNAME));

        int size = selectTableHeaders.size();
        for (int i = 0; i < size; i++) {
            tableHeaderMenu.add(selectTableHeaders.get(i));
        }
        this.setJMenuBar(menuBar);
    }

    /**
     * Creates a CheckBoxMenuItem with its action: Removing or adding a table
     * column of the shown table
     *
     * @param header
     * @return checkBox A Checkbox with a defined name
     */
    private JCheckBoxMenuItem createCheckBoxMenuItem(final String header) {

        JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem(header
                + " invisible");
        checkBox.setName(header);

        // Checkbox Action: Add or remove a single table header
        ActionListener checkBoxAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                    mvm.setTableColumnVisible(header, false);
                } else {
                    mvm.setTableColumnVisible(header, true);
                }
            }
        };

        checkBox.addActionListener(checkBoxAction);
        return checkBox;
    }

    /**
     * <p>getQueryPanel.</p>
     *
     * @param mvm a {@link maltcms.db.ui.MetaboliteViewModel} object.
     * @return a {@link javax.swing.JPanel} object.
     */
    public JPanel getQueryPanel(MetaboliteViewModel mvm) {
        if (this.qpanel == null) {
            this.qpanel = new MetaboliteQueryPanel(mvm);
        }
        return this.qpanel;
    }

    /**
     * <p>setupUI.</p>
     */
    public void setupUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.setTitle("MetaboliteBrowser: ");
        MetaboliteView mv = new MetaboliteView(mvm);
        mvm.setMetaboliteView(mv);
        mv.addComponents(this);
        this.add(this.getQueryPanel(mvm), BorderLayout.SOUTH);
        this.setSize(1280, 1024);
        this.setVisible(true);
        this.pack();
        this.generateMenuBar(mvm);
    }
}
