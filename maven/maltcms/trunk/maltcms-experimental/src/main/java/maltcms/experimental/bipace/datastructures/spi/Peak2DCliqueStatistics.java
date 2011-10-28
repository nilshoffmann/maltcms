/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.datastructures.spi;

import maltcms.experimental.bipace.datastructures.api.CliqueStatistics;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Peak2DCliqueStatistics extends CliqueStatistics<Peak2D> {

    @Override
    public void selectCentroid() {
        double mindist = Double.POSITIVE_INFINITY;
        double[] dists = new double[getClique().getPeakList().size()];
        int i = 0;
        List<Peak2D> peaks = new ArrayList<Peak2D>(getClique().getPeakList());
        for (Peak2D peak : peaks) {
            for (Peak2D peak1 : peaks) {
                dists[i] += Math.pow(
                        peak.getRetentionTime1()
                        - peak1.getRetentionTime1(), 2.0d);
                dists[i] += Math.pow(peak.getRetentionTime2() - peak1.
                        getRetentionTime2(), 2.0d);
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
    public double getDistanceToCentroid(Peak2D p) {
        double[] mean = getCliqueMean();
        double d = 0.0d;
        d += Math.pow(mean[0] - p.getRetentionTime1(), 2);
        d += Math.pow(mean[1] - p.getRetentionTime2(), 2);
        return Math.sqrt(d);
    }

    @Override
    public double[] getCliqueVariance() {
        double[] cliqueMean = getCliqueMean();
        double n = getClique().getPeakList().size() - 1;
        double sumOfSquaredDifferences0 = 0;
        double sumOfSquaredDifferences1 = 0;
        for (Peak2D p : getClique().getPeakList()) {
            sumOfSquaredDifferences0 += Math.pow(
                    p.getRetentionTime1() - cliqueMean[0], 2.0d);
            sumOfSquaredDifferences1 += Math.pow(
                    p.getRetentionTime2() - cliqueMean[1], 2.0d);
        }
        return new double[]{sumOfSquaredDifferences0 / n,
                    sumOfSquaredDifferences1 / n};
    }

    @Override
    public double[] getCliqueMean() {
        double sum0 = 0.0d;
        double sum1 = 0.0d;
        double n = getClique().getPeakList().size();
        for (Peak2D p : getClique().getPeakList()) {
            sum0 += p.getRetentionTime1();
            sum1 += p.getRetentionTime2();
        }
        return new double[]{sum0 / n, sum1 / n};
    }

    @Override
    public void update() {
        setCliqueMean(getCliqueMean());
        setCliqueVariance(getCliqueVariance());
        selectCentroid();
    }

    @Override
    public String[] getFeatureNames() {
        return new String[]{"first_column_time","second_column_time"};
    }
}
