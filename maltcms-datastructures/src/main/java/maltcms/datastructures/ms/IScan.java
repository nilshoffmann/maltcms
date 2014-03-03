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

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;

/**
 * Interface representing a Scan (usually an MS scan).
 *
 * @author Nils Hoffmann
 *
 */
public interface IScan extends IFeatureVector {

    /**
     * Returns ths intensity values of this mass spectrum.
     *
     * @return the intensitiy values
     */
    public Array getIntensities();

    /**
     * Returns the m/z values of this mass spectrum. Individual m/z values
     * are usually stored as double or float values.
     *
     * @return the m/z values
     */
    public Array getMasses();

    /**
     * Returns the scan acquisition time in seconds of this mass spectrum.
     *
     * @return the scan acquisition time
     */
    public double getScanAcquisitionTime();

    /**
     * Returns the scan index of this mass spectrum. Must be greater or equal to zero.
     *
     * @return the scan index
     */
    public int getScanIndex();

    /**
     * Returns the total intensity (sum) over all measured m/z and intensity pairs.
     *
     * @return the total intensity
     */
    public double getTotalIntensity();

    /**
     * Returns the ms fragmentation level of this scan. Must be greater or
     * equal to 1.
     *
     * @return the mass spectrum's ion fragmentation level
     */
    public short getMsLevel();

    /**
     * Returns the integer precursor charge. Double.NaN indicates that
     * no precursor charge has been recorded for this scan.
     *
     * @return the precursor charge
     */
    public double getPrecursorCharge();

    /**
     * Returns the precursor m/z. Double.NaN indicates that
     * no precursor m/z has been recorded for this scan.
     *
     * @return the precursor m/z
     */
    public double getPrecursorMz();

    /**
     * Returns the precursor intensity. Double.NaN indicates that
     * no precursor intensity has been recorded for this scan.
     *
     * @return the precursor intensity
     */
    public double getPrecursorIntensity();
}
