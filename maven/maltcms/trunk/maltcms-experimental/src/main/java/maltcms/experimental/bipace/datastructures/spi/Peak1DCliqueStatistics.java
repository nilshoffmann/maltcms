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
package maltcms.experimental.bipace.datastructures.spi;

import maltcms.experimental.bipace.datastructures.api.CliqueStatistics;
import java.util.ArrayList;
import java.util.List;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Peak1DCliqueStatistics extends CliqueStatistics<Peak> {

    @Override
    public void selectCentroid() {
        double mindist = Double.POSITIVE_INFINITY;
        double[] dists = new double[getClique().getPeakList().size()];
        int i = 0;
        List<Peak> peaks = new ArrayList<Peak>(getClique().getPeakList());
        for (Peak peak : peaks) {
            for (Peak peak1 : peaks) {
                dists[i] += Math.pow(
                        peak.getScanAcquisitionTime()
                        - peak1.getScanAcquisitionTime(), 2.0d);
            }
            dists[i] = Math.sqrt(dists[i]);
            i++;
        }
        int mindistIdx = 0;
        for (int j = 0; j < dists.length; j++) {
            if (dists[j] < mindist) {
                mindist = dists[j];
                mindistIdx = j;
            }
        }
//        log.debug("Clique centroid is {}", peaks.get(mindistIdx));
        setCentroid(peaks.get(mindistIdx));
    }

    @Override
    public double getDistanceToCentroid(Peak p) {
        double[] mean = getCliqueMean();
        double d = 0.0d;
        d += Math.pow(mean[0] - p.getScanAcquisitionTime(), 2);
        return Math.sqrt(d);
    }

    @Override
    public double[] getCliqueVariance() {
        double[] cliqueMean = getCliqueMean();
        double n = getClique().getPeakList().size() - 1;
        double sumOfSquaredDifferences = 0;
        for (Peak p : getClique().getPeakList()) {
            sumOfSquaredDifferences += Math.pow(
                    p.getScanAcquisitionTime() - cliqueMean[0], 2.0d);
        }
        return new double[]{sumOfSquaredDifferences / n};
    }

    @Override
    public double[] getCliqueMean() {
        double sum = 0.0d;
        double n = getClique().getPeakList().size();
        for (Peak p : getClique().getPeakList()) {
            sum += p.getScanAcquisitionTime();
        }
        return new double[]{sum / n};
    }

    @Override
    public void update() {
        setCliqueMean(getCliqueMean());
        setCliqueVariance(getCliqueVariance());
        selectCentroid();
    }

    @Override
    public String[] getFeatureNames() {
        return new String[]{"scan_acquisition_time"};
    }
}
