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
package maltcms.commands.fragments2d.peakfinding.output;

import cross.datastructures.fragments.IFileFragment;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments2d.peakfinding.bbh.BBHTools;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.Peak2DClique;

@Slf4j
/**
 * <p>PeakNormalization class.</p>
 *
 * @author hoffmann
 * 
 */
@Data
public class PeakNormalization {

    private int expectedX = -1;
    private int expectedY = -1;
    private double threshold = 3.0d;

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getName();
    }

    private double eucl(int x1, int y1, int x2, int y2) {
        return Math.sqrt(x1 * x2 + y1 * y2);
    }

    /**
     * <p>findReference.</p>
     *
     * @param peakLists a {@link java.util.List} object.
     * @param bidiBestHits a {@link java.util.List} object.
     * @param f a {@link java.util.Collection} object.
     * @return a {@link java.util.List} object.
     */
    public List<Peak2D> findReference(List<List<Peak2D>> peakLists,
            List<List<Point>> bidiBestHits, Collection<IFileFragment> f) {
        final List<Peak2DClique> peakCliqueLists = BBHTools.getPeak2DCliqueList(
                f, bidiBestHits, peakLists);

        int x, y, s, minArg = -1;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < peakCliqueLists.size(); i++) {
            x = 0;
            y = 0;
            s = peakCliqueLists.get(i).getAll().size();
            for (Peak2D p : peakCliqueLists.get(i).getAll()) {
                x += p.getFirstScanIndex();
                y += p.getSecondScanIndex();
            }
            if (eucl(x / s, y / s, this.expectedX, this.expectedY) < min) {
                min = eucl(x / s, y / s, this.expectedX, this.expectedY);
                minArg = i;
            }
        }
        int bestClique = -1;
        if (min < this.threshold) {
            bestClique = minArg;
        }

        if (bestClique != -1) {
            return peakCliqueLists.get(bestClique).getAll();
        }
        return null;
    }

    /**
     * <p>normalize.</p>
     *
     * @param list a {@link java.util.List} object.
     * @param ref a {@link maltcms.datastructures.peak.Peak2D} object.
     */
    public void normalize(List<Peak2D> list, Peak2D ref) {
        for (Peak2D p : list) {
            p.normalizeTo(ref);
        }
    }

    /**
     * <p>normalize.</p>
     *
     * @param peakLists a {@link java.util.List} object.
     * @param bidiBestHits a {@link java.util.List} object.
     * @param f a {@link java.util.Collection} object.
     */
    public void normalize(List<List<Peak2D>> peakLists,
            List<List<Point>> bidiBestHits, Collection<IFileFragment> f) {
        final List<Peak2D> refs = findReference(peakLists, bidiBestHits, f);
        if (refs != null) {
            for (int i = 0; i < peakLists.size(); i++) {
                normalize(peakLists.get(i), refs.get(i));
            }
        }
    }
}
