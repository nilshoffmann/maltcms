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
package cross.ui;

import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.ui.actions.LoadConfiguration;
import cross.ui.actions.SaveConfiguration;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Visual Editor to assemble pipelines of FragmentCommands, save them, reload
 * them etc.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public class ConfigurationEditor extends JTable implements
        IListener<IEvent<Configuration>> {

    /**
     *
     */
    private static final long serialVersionUID = 7876131594811327381L;
    private JTable jTable = null;

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final ConfigurationEditor pe = new ConfigurationEditor();
                JFrame jf = new JFrame();
                pe.createMenuBar(jf);
                pe.createConfigTable(null);
                jf.add(pe);
                jf.setBounds(0, 0, 640, 480);
                jf.setVisible(true);
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                // pe.pack();
            }
        });
    }

    private JTable createConfigTable(final TableModel tm) {
        if (this.jTable == null) {
            this.jTable = new JTable(tm);
            add(new JScrollPane(this.jTable));
        }
        this.jTable.setModel(tm);
        return this.jTable;
    }

    // private JTabbedPane createJTabbedPane() {
    // if (this.tabs == null) {
    // this.tabs = new JTabbedPane();
    // }
    // return this.tabs;
    // }
    private void createMenuBar(JFrame ce) {
        final JMenuBar jmb = new JMenuBar();
        final JMenu jm1 = new JMenu("File");
        final IEventSource<Configuration> load = new LoadConfiguration("Load");
        load.addListener(this);
        final IEventSource<Configuration> save = new SaveConfiguration("Save");
        save.addListener(this);
        final JMenuItem jmi1 = new JMenuItem((AbstractAction) load);
        final JMenuItem jmi2 = new JMenuItem((AbstractAction) save);
        jmb.add(jm1);
        jm1.add(jmi1);
        jm1.add(jmi2);
        ce.setJMenuBar(jmb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.event.IListener#listen(cross.event.IEvent)
     */
    @Override
    public void listen(final IEvent<Configuration> v) {
        System.out.println("Received event!");
        final Configuration cfg = v.get();
        final String title = (cfg instanceof PropertiesConfiguration) ? ((PropertiesConfiguration) cfg)
                .getFileName()
                : "" + cfg.hashCode();
        final DefaultTableModel dtm = new DefaultTableModel(new Object[]{
                    "Key", "Value(s)"}, 0);
        final Iterator<?> iter = cfg.getKeys();
        while (iter.hasNext()) {
            final String key = (String) iter.next();
            dtm.addRow(new Object[]{key, cfg.getProperty(key)});
        }
        System.out.println("Set DefaultTableModel!");
        this.jTable.setModel(dtm);
    }
}
