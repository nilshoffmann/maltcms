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
package maltcms.datastructures.ms;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;

import cross.IConfigurable;
import cross.Logging;
import cross.ObjectFactory;
import cross.datastructures.fragments.IFileFragment;

public class ChromatogramFactory implements IConfigurable {

	private final Logger log = Logging.getLogger(ObjectFactory.class);

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
