/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.ms;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromatogramFactory implements IConfigurable {

    private Configuration cfg = new PropertiesConfiguration();

    @Override
    public void configure(final Configuration cfg) {
        this.cfg = new PropertiesConfiguration();
        ConfigurationUtils.copy(cfg, this.cfg);
    }

    public IChromatogram1D createChromatogram1D(IFileFragment f) {
        Chromatogram1D c = new Chromatogram1D(f);
        c.configure(this.cfg);
        return c;
    }

    public ProfileChromatogram1D createProfileChromatogram1D(IFileFragment f) {
        ProfileChromatogram1D c = new ProfileChromatogram1D(f);
        c.configure(this.cfg);
        return c;
    }

    public IChromatogram2D createChromatogram2D(IFileFragment f) {
        Chromatogram2D c = new Chromatogram2D(f);
        c.configure(this.cfg);
        return c;
    }
}
