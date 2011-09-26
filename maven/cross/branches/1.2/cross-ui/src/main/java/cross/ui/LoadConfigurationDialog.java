/*
 * 
 *
 * $Id$
 */

package cross.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class LoadConfigurationDialog {

	private File cwd = null;

	private static LoadConfigurationDialog lcd = null;

	public static LoadConfigurationDialog getInstance() {
		if (LoadConfigurationDialog.lcd == null) {
			LoadConfigurationDialog.lcd = new LoadConfigurationDialog();
		}
		return LoadConfigurationDialog.lcd;
	}

	private final Logger log = LoggerFactory.getLogger(LoadConfigurationDialog.class);

	private LoadConfigurationDialog() {

	}

	public Configuration loadConfiguration(final FileFilter ff) {
		final JFileChooser jfc = new JFileChooser(this.cwd);
		PropertiesConfiguration pc = new PropertiesConfiguration();
		jfc.setFileFilter(ff);
		final int ret = jfc.showOpenDialog(null);
		switch (ret) {
			case JFileChooser.APPROVE_OPTION: {
				final File f = jfc.getSelectedFile();
				this.cwd = f.getParentFile();
				try {
					pc = new PropertiesConfiguration(f);
				} catch (final ConfigurationException e) {
					this.log.error(e.getLocalizedMessage());
				}

			}

		}
		jfc.setVisible(false);
		return pc;
	}
}
