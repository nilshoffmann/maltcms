/**
 * 
 */
package maltcms.experimental.bipace.datastructures;

import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import maltcms.datastructures.ms.IScan1D;
import maltcms.datastructures.peak.Peak1D;


import maltcms.experimental.bipace.datastructures.api.IClique;

import maltcms.experimental.bipace.datastructures.api.ICliqueMemberCriterion;
import maltcms.experimental.bipace.datastructures.api.ICliqueUpdater;
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

//    public static Clique<IScan1D> createScan1DClique() {
//        ICliqueMemberCriterion<IScan1D> icmc = new Scan1DBidiBestHitsCriterion();
//        ICliqueUpdater<IScan1D> icu = new ICliqueUpdater<IScan1D>() {
//
//            @Override
//            public void update(IClique<IScan1D> c, IScan1D p) {
//                int n = 0;
//                Array marray = c.getArrayStatsMap().getFeature("RT_MEAN");
//                double mean = 0;
//                if (marray != null) {
//                    mean = marray.getDouble(Index.scalarIndexImmutable);
//                } else {
//                    marray = new ArrayDouble.D0();
//                }
//                Array varray = c.getArrayStatsMap().getFeature("RT_VARIANCE");
//                double var = 0;
//                if (varray != null) {
//                    var = varray.getDouble(Index.scalarIndexImmutable);
//                } else {
//                    varray = new ArrayDouble.D0();
//                }
//                log.debug(
//                        "Clique variance before adding peak: {}, clique mean before: {}",
//                        var, mean);
//                double delta = 0;
//                double rt = p.getScanAcquisitionTime();
//                n = c.size() + 1;
//                delta = rt - mean;
//                if (n > 0) {
//                    mean = mean + delta / n;
//                }
//                if (n > 2) {
//                    var = (var + (delta * (rt - mean))) / ((double) (n - 2));
//                }
//                marray.setDouble(Index.scalarIndexImmutable, mean);
//                varray.setDouble(Index.scalarIndexImmutable, var);
//                c.getArrayStatsMap().addFeature("RT_MEAN", marray);
//                c.getArrayStatsMap().addFeature("RT_VARIANCE", varray);
//                log.debug(
//                        "Clique variance after adding peak: {}, clique mean before: {}",
//                        var, mean);
//            }
//
//            @Override
//            public void setCentroid(IClique<IScan1D> c) {
//                double mindist = Double.POSITIVE_INFINITY;
//                double[] dists = new double[c.size()];
//                int i = 0;
//                List<IScan1D> peaks = c.getFeatureVectorList();
//                for (IScan1D peak : peaks) {
//                    for (IScan1D peak1 : peaks) {
//                        dists[i] += Math.pow(peak.getScanAcquisitionTime()
//                                - peak1.getScanAcquisitionTime(), 2.0d);
//                    }
//                    i++;
//                }
//                int mindistIdx = 0;
//                for (int j = 0; j < dists.length; j++) {
//                    if (dists[j] < mindist) {
//                        mindist = dists[j];
//                        mindistIdx = j;
//                    }
//                }
//                log.debug("Clique centroid is {}", peaks.get(mindistIdx));
//                c.setCentroid(peaks.get(mindistIdx));
//            }
//        };
//        Clique<IScan1D> c = new Clique<IScan1D>(comp, icmc, icu);
//        return c;
//    }
}
