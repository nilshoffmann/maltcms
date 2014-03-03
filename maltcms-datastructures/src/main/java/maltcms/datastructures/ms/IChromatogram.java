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

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import ucar.ma2.Array;

/**
 * Interface representing a Chromatogram.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IChromatogram extends IConfigurable {

    public IFileFragment getParent();

    public int getNumberOfScans();

    public List<Array> getIntensities();

    public List<Array> getMasses();

    public String getScanAcquisitionTimeUnit();

    public Array getScanAcquisitionTime();

    public Tuple2D<Double, Double> getTimeRange();

    public Tuple2D<Double, Double> getMassRange();

    public int getIndexFor(double scan_acquisition_time);

}
