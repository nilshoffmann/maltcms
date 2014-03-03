/*
 * maltcms-io-mzml, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2013, The authors of maltcms-io-mzml. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * maltcms-io-mzml may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of maltcms-io-mzml, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * maltcms-io-mzml is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package uk.ac.ebi.jmzml.model.mzml;

import cross.datastructures.collections.CachedReadWriteList;
import java.util.List;

/**
 *
 * @author Nils Hoffmann
 */
public class CachedSpectrumList extends SpectrumList {

    private final String name;
    private final int cacheSize;

    public CachedSpectrumList(String name, int cacheSize) {
        this.name = name;
        this.cacheSize = cacheSize;
    }

    @Override
    public List<Spectrum> getSpectrum() {
        if (spectrum == null) {
            spectrum = new CachedReadWriteList<>(name, cacheSize);
        }
        return this.spectrum;
    }

}
