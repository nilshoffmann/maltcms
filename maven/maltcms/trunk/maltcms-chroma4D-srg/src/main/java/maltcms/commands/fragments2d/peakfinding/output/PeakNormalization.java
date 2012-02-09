/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.commands.fragments2d.peakfinding.output;

import java.awt.Point;
import java.util.Collection;
import java.util.List;

import maltcms.commands.fragments2d.peakfinding.bbh.BBHTools;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.Peak2DClique;
import cross.datastructures.fragments.IFileFragment;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class PeakNormalization {

    private int expectedX = -1;
    private int expectedY = -1;
    private double threshold = 3.0d;

    @Override
    public String toString() {
        return getClass().getName();
    }

    private double eucl(int x1, int y1, int x2, int y2) {
        return Math.sqrt(x1 * x2 + y1 * y2);
    }

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

    public void normalize(List<Peak2D> list, Peak2D ref) {
        for (Peak2D p : list) {
            p.normalizeTo(ref);
        }
    }

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
