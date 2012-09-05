/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
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
 * @author Nils Hoffmann
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
