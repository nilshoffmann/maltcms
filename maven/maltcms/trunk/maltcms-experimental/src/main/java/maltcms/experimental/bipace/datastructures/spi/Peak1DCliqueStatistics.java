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
package maltcms.experimental.bipace.datastructures.spi;

import maltcms.experimental.bipace.datastructures.api.CliqueStatistics;
import java.util.ArrayList;
import java.util.List;
import maltcms.datastructures.peak.IPeak;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Peak1DCliqueStatistics extends CliqueStatistics<IPeak> {

    @Override
    public void selectCentroid() {
        double mindist = Double.POSITIVE_INFINITY;
        double[] dists = new double[getClique().getPeakList().size()];
        int i = 0;
        List<IPeak> peaks = new ArrayList<IPeak>(getClique().getPeakList());
        for (IPeak peak : peaks) {
            for (IPeak peak1 : peaks) {
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
    public double getDistanceToCentroid(IPeak p) {
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
        for (IPeak p : getClique().getPeakList()) {
            sumOfSquaredDifferences += Math.pow(
                    p.getScanAcquisitionTime() - cliqueMean[0], 2.0d);
        }
        return new double[]{sumOfSquaredDifferences / n};
    }

    @Override
    public double[] getCliqueMean() {
        double sum = 0.0d;
        double n = getClique().getPeakList().size();
        for (IPeak p : getClique().getPeakList()) {
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
