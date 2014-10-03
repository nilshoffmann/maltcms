/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.peak;

import java.util.List;
import java.util.UUID;
import ucar.ma2.Array;

/**
 * <p>IPeak interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IPeak {

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    /**
     * <p>getFeature.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link ucar.ma2.Array} object.
     */
    Array getFeature(String name);

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    /**
     * <p>getFeatureNames.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getFeatureNames();

    /**
     * <p>getScanAcquisitionTime.</p>
     *
     * @return a double.
     */
    double getScanAcquisitionTime();

    /**
     * <p>getScanIndex.</p>
     *
     * @return a int.
     */
    int getScanIndex();

    /**
     * <p>getAssociation.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getAssociation();

    /**
     * <p>setName.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    void setName(String name);

    /**
     * <p>getName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();

    /**
     * <p>getPeakIndex.</p>
     *
     * @return a int.
     */
    int getPeakIndex();

    /**
     * <p>setPeakIndex.</p>
     *
     * @param index a int.
     */
    void setPeakIndex(int index);

    /**
     * <p>getUniqueId.</p>
     *
     * @return a {@link java.util.UUID} object.
     */
    UUID getUniqueId();
}
