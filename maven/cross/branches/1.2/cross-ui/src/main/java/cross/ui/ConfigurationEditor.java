/*
 * $license$
 *
 * $Id$
 */

package cross.ui;

import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.ui.actions.LoadConfiguration;
import cross.ui.actions.SaveConfiguration;

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
		if(this.jTable==null) {
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
		final DefaultTableModel dtm = new DefaultTableModel(new Object[] {
		        "Key", "Value(s)" }, 0);
		final Iterator<?> iter = cfg.getKeys();
		while (iter.hasNext()) {
			final String key = (String) iter.next();
			dtm.addRow(new Object[] { key, cfg.getProperty(key) });
		}
		System.out.println("Set DefaultTableModel!");
		this.jTable.setModel(dtm);
	}

}
