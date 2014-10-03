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

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import ucar.ma2.Array;

/**
 * Interface representing a Chromatogram.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IChromatogram extends IConfigurable {

    /**
     * <p>getParent.</p>
     *
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public IFileFragment getParent();

    /**
     * <p>getNumberOfScans.</p>
     *
     * @return a int.
     */
    public int getNumberOfScans();

    /**
     * <p>getIntensities.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Array> getIntensities();

    /**
     * <p>getMasses.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Array> getMasses();

    /**
     * <p>getScanAcquisitionTimeUnit.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getScanAcquisitionTimeUnit();

    /**
     * <p>getScanAcquisitionTime.</p>
     *
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array getScanAcquisitionTime();

    /**
     * <p>getTimeRange.</p>
     *
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<Double, Double> getTimeRange();

    /**
     * <p>getMassRange.</p>
     *
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<Double, Double> getMassRange();

    /**
     * <p>getIndexFor.</p>
     *
     * @param scan_acquisition_time a double.
     * @return a int.
     */
    public int getIndexFor(double scan_acquisition_time);

}
