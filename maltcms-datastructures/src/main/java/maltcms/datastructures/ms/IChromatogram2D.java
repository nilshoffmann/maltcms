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

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import maltcms.datastructures.caches.IScanLine;

/**
 * Interface representing a 2-dimensional chromatogram.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IChromatogram2D extends IChromatogram, IScan2DProvider {

    /**
     * <p>getScan2D.</p>
     *
     * @param globalScan a int.
     * @param localScan a int.
     * @return a {@link maltcms.datastructures.ms.IScan2D} object.
     */
    public IScan2D getScan2D(int globalScan, int localScan);
    
    /**
     * <p>getNumberOfModulations.</p>
     *
     * @return a int.
     */
    public int getNumberOfModulations();

    /**
     * <p>getNumberOfScansPerModulation.</p>
     *
     * @return a int.
     */
    public int getNumberOfScansPerModulation();

    /**
     * <p>getNumberOf2DScans.</p>
     *
     * @return a int.
     */
    public int getNumberOf2DScans();

    /**
     * <p>getModulationDuration.</p>
     *
     * @return a double.
     */
    public double getModulationDuration();

    /**
     * <p>getSecondColumnScanAcquisitionTimeUnit.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSecondColumnScanAcquisitionTimeUnit();

    /**
     * <p>getPointFor.</p>
     *
     * @param scan a int.
     * @return a {@link java.awt.Point} object.
     */
    public Point getPointFor(int scan);

    /**
     * <p>getPointFor.</p>
     *
     * @param scan_acquisition_time a double.
     * @return a {@link java.awt.Point} object.
     */
    public Point getPointFor(double scan_acquisition_time);

//    /**
//     * <p>getPointFor.</p>
//     *
//     * @param firstColumnTime a double.
//     * @param secondColumnTime a double.
//     * @return a {@link java.awt.Point} object.
//     */
//    public Point getPointFor(double firstColumnTime, double secondColumnTime);
//    
    /**
     * <p>subsetByMsLevel.</p>
     *
     * @param msLevel a short.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<IScan2D> subsetByMsLevel(short msLevel);
    
    /**
     * <p>Returns the rectangular time range of this 2D chromatogram</p>.
     * @return the bounding box of retention times.
     */
    public Rectangle2D getTimeRange2D();
    
    /**
     * Call for explicit access to the underlying IScanLine implementation.
     *
     * @return a {@link maltcms.datastructures.caches.IScanLine} object.
     */
    public IScanLine getScanLineImpl();
}
