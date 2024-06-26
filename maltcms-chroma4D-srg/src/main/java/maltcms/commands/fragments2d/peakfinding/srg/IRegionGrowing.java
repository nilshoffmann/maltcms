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
package maltcms.commands.fragments2d.peakfinding.srg;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import java.awt.Point;
import java.util.List;
import maltcms.datastructures.ms.IChromatogram2D;
import maltcms.datastructures.peak.PeakArea2D;

/**
 * <p>IRegionGrowing interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IRegionGrowing extends IConfigurable {

    /**
     * <p>getAreasFor.</p>
     *
     * @param seeds a {@link java.util.List} object.
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param chrom a {@link maltcms.datastructures.ms.IChromatogram2D} object.
     * @return a {@link java.util.List} object.
     */
    List<PeakArea2D> getAreasFor(List<Point> seeds, IFileFragment ff, IChromatogram2D chrom);

}
