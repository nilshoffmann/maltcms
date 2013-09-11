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

import java.awt.Point;

/**
 * Interface representing a 2-dimensional chromatogram.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public interface IChromatogram2D extends IChromatogram, IScan2DProvider {

    public IScan2D getScan2D(int globalScan, int localScan);

    public int getNumberOfModulations();

    public int getNumberOfScansPerModulation();

    public int getNumberOf2DScans();

    public double getModulationDuration();

    public String getSecondColumnScanAcquisitionTimeUnit();

    public Point getPointFor(int scan);

    public Point getPointFor(double scan_acquisition_time);
	
	public Iterable<IScan2D> subsetByMsLevel(short msLevel);
}
