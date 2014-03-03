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
package maltcms.commands.fragments2d.peakfinding.bbh;

import cross.IConfigurable;
import java.awt.Point;
import java.util.List;
import maltcms.commands.fragments2d.peakfinding.SeededRegionGrowing;
import maltcms.datastructures.peak.Peak2D;

/**
 * Interface for an BBH algorithm used by {@link SeededRegionGrowing}.
 *
 * @author Mathias Wilhelm
 */
public interface IBidirectionalBestHit extends IConfigurable {

    /**
     * Getter.
     *
     *
     * @param peaklists list of peak list for all chromatograms
     * @return a list of all bidirectional best hits. List contains the indices
     *         of peak in the peaklist.
     */
    List<List<Point>> getBidiBestHitList(List<List<Peak2D>> peaklists);

    double sim(Peak2D p1, Peak2D p2);

    void clear();
}
