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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger log = LoggerFactory.getLogger(SaveConfigurationDialog.class);

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
                try {
                    ConfigurationUtils.dump(cfg,new PrintWriter(new FileWriter(f)));
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(SaveConfigurationDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        jfc.setVisible(false);
        return cfg;
    }
}
