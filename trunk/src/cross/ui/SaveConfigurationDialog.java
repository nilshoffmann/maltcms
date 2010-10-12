/**
 * 
 */
package cross.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class SaveConfigurationDialog {
	private File cwd = null;

	private static SaveConfigurationDialog scd = null;

	public static SaveConfigurationDialog getInstance() {
		if (SaveConfigurationDialog.scd == null) {
			SaveConfigurationDialog.scd = new SaveConfigurationDialog();
		}
		return SaveConfigurationDialog.scd;
	}

	private final Logger log = Logging.getLogger(SaveConfigurationDialog.class);

	private SaveConfigurationDialog() {

	}

	public Configuration saveConfiguration(final Configuration cfg,
	        final FileFilter ff) {
		final JFileChooser jfc = new JFileChooser(this.cwd);
		jfc.setFileFilter(ff);
		final int ret = jfc.showSaveDialog(null);
		switch (ret) {
			case JFileChooser.APPROVE_OPTION: {
				final File f = jfc.getSelectedFile();
				this.cwd = f.getParentFile();
				Factory.saveConfiguration(cfg, f);
			}
		}
		jfc.setVisible(false);
		return cfg;
	}
}
