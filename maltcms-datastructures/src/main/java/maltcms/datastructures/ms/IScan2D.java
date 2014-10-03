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
package maltcms.datastructures.ms;

/**
 * <p>IScan2D interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IScan2D extends IScan1D {

    /**
     * Returns the first column scan acquisition time of this scan in seconds.
     *
     * @return the first column scan acquisition time
     */
    double getFirstColumnScanAcquisitionTime();

    /**
     * Returns the first column scan index, aka the index of the modulation
     * containing this scan.
     *
     * May be -1 if no fixed modulation period was used.
     *
     * @return the first column scan index of this scan, or -1
     */
    int getFirstColumnScanIndex();

    /**
     * Returns the second column scan acquisition time of this scan in seconds.
     *
     * @return the second column scan acquisition time
     */
    double getSecondColumnScanAcquisitionTime();

    /**
     * Returns the second column scan index, aka the relative index of the scan
     * within its modulation.
     *
     * May be -1 if no fixed modulation period was used.
     *
     * @return the second column scan index of this scan, or -1
     */
    int getSecondColumnScanIndex();

    /**
     * <p>setFirstColumnScanAcquisitionTime.</p>
     *
     * @param sat a double.
     */
    void setFirstColumnScanAcquisitionTime(final double sat);

    /**
     * <p>setFirstColumnScanIndex.</p>
     *
     * @param a a int.
     */
    void setFirstColumnScanIndex(final int a);

    /**
     * <p>setSecondColumnScanAcquisitionTime.</p>
     *
     * @param sat a double.
     */
    void setSecondColumnScanAcquisitionTime(final double sat);

    /**
     * <p>setSecondColumnScanIndex.</p>
     *
     * @param a a int.
     */
    void setSecondColumnScanIndex(final int a);
}
