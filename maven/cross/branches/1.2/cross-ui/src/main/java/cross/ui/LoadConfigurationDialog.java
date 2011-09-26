/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
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
 */
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
