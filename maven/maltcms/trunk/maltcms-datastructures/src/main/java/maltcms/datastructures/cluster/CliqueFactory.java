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
package maltcms.datastructures.cluster;

import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import maltcms.datastructures.peak.Peak1D;


import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
@Slf4j
public class CliqueFactory {

    public static Clique<Peak1D> createPeakClique() {
        Comparator<Peak1D> comp = new Comparator<Peak1D>() {
            @Override
            public int compare(Peak1D o1, Peak1D o2) {
                return o1.getFile().compareTo(o2.getFile());
            }
        };
        ICliqueMemberCriterion<Peak1D> icmc = new Peak1DBidiBestHitsCriterion();
        ICliqueUpdater<Peak1D> icu = new ICliqueUpdater<Peak1D>() {
            @Override
            public void update(IClique<Peak1D> c, Peak1D p) {
                int n = 0;
                Array marray = c.getArrayStatsMap().getFeature("RT_MEAN");
                double mean = 0;
                if (marray != null) {
                    mean = marray.getDouble(Index.scalarIndexImmutable);
                } else {
                    marray = new ArrayDouble.D0();
                }
                Array varray = c.getArrayStatsMap().getFeature("RT_VARIANCE");
                double var = 0;
                if (varray != null) {
                    var = varray.getDouble(Index.scalarIndexImmutable);
                } else {
                    varray = new ArrayDouble.D0();
                }
                log
                        .debug(
                        "Clique variance before adding peak: {}, clique mean before: {}",
                        var, mean);
                double delta = 0;
                double rt = p.getApexTime();
                n = c.size() + 1;
                delta = rt - mean;
                if (n > 0) {
                    mean = mean + delta / n;
                }
                if (n > 2) {
                    var = (var + (delta * (rt - mean))) / ((double) (n - 2));
                }
                marray.setDouble(Index.scalarIndexImmutable, mean);
                varray.setDouble(Index.scalarIndexImmutable, var);
                c.getArrayStatsMap().addFeature("RT_MEAN", marray);
                c.getArrayStatsMap().addFeature("RT_VARIANCE", varray);
                log
                        .debug(
                        "Clique variance after adding peak: {}, clique mean before: {}",
                        var, mean);
            }

            @Override
            public void setCentroid(IClique<Peak1D> c) {
                double mindist = Double.POSITIVE_INFINITY;
                double[] dists = new double[c.size()];
                int i = 0;
                List<Peak1D> peaks = c.getFeatureVectorList();
                for (Peak1D peak : peaks) {
                    for (Peak1D peak1 : peaks) {
                        dists[i] += Math.pow(peak.getApexTime()
                                - peak1.getApexTime(), 2.0d);
                    }
                    i++;
                }
                int mindistIdx = 0;
                for (int j = 0; j < dists.length; j++) {
                    if (dists[j] < mindist) {
                        mindist = dists[j];
                        mindistIdx = j;
                    }
                }
                log.debug("Clique centroid is {}", peaks.get(mindistIdx));
                c.setCentroid(peaks.get(mindistIdx));
            }
        };
        Clique<Peak1D> c = new Clique<Peak1D>(comp, icmc, icu);
        return c;
    }
}
