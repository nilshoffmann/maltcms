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
package maltcms.mpaxs;

import java.io.File;
import java.net.URI;
import net.sf.mpaxs.api.concurrent.ConfigurableRunnable;
import net.sf.mpaxs.api.job.Progress;

/**
 *
 * @author nilshoffmann
 */
public class MaltcmsConfigurableRunnable implements ConfigurableRunnable<URI> {

    @Override
    public URI get() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void configure(File pathToConfig) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Progress getProgress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
