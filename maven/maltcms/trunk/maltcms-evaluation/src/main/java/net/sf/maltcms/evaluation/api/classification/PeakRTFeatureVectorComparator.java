/**
 * 
 */
package net.sf.maltcms.evaluation.api.classification;

import net.sf.maltcms.evaluation.spi.classification.PeakRTFeatureVector;
import maltcms.datastructures.array.IFeatureVector;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class PeakRTFeatureVectorComparator implements IFeatureVectorComparator {

    private final double delta;

    public PeakRTFeatureVectorComparator(double delta) {
        this.delta = delta;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isFN(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isFN(IFeatureVector gt, IFeatureVector test) {
        if (gt instanceof PeakRTFeatureVector && test instanceof PeakRTFeatureVector) {
            final double lhsRT = ((PeakRTFeatureVector) gt).getRT();
            final double rhsRT = ((PeakRTFeatureVector) test).getRT();
            if (!Double.isNaN(lhsRT) && Double.isNaN(rhsRT)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isFP(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isFP(IFeatureVector gt, IFeatureVector test) {
        if (gt instanceof PeakRTFeatureVector && test instanceof PeakRTFeatureVector) {
            final double lhsRT = ((PeakRTFeatureVector) gt).getRT();
            final double rhsRT = ((PeakRTFeatureVector) test).getRT();
            //if gt is NaN, but test has a value, if FP=true
            if (Double.isNaN(lhsRT) && !Double.isNaN(rhsRT)) {
                return true;
            }
            if (Math.abs(lhsRT - rhsRT) > delta) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isTN(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isTN(IFeatureVector gt, IFeatureVector test) {
        if (gt instanceof PeakRTFeatureVector && test instanceof PeakRTFeatureVector) {
            final double lhsRT = ((PeakRTFeatureVector) gt).getRT();
            final double rhsRT = ((PeakRTFeatureVector) test).getRT();
            //both are NaNs
            if (Double.isNaN(lhsRT) && Double.isNaN(rhsRT)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isTP(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isTP(IFeatureVector gt, IFeatureVector test) {
        if (gt instanceof PeakRTFeatureVector && test instanceof PeakRTFeatureVector) {
            final double lhsRT = ((PeakRTFeatureVector) gt).getRT();
            final double rhsRT = ((PeakRTFeatureVector) test).getRT();
            //if either is a NaN or both, TP=false
            if ((Double.isNaN(lhsRT) && !Double.isNaN(rhsRT)) || (!Double.isNaN(lhsRT) && Double.isNaN(rhsRT))) {
                return false;
            }
            //true positive if both have a value, and their difference is <=delta
            if (Math.abs(lhsRT - rhsRT) <= delta) {
                return true;
            }
        }
        return false;
    }

    /**
     * NaN's are treated as 0, giving a large distance for x!=y and 0 for x=y,
     * with either x=NaN and y!=NaN, x!=NaN and y==NaN, or x=NaN=y
     * @param gt
     * @param test
     * @return
     */
    @Override
    public double getSquaredDiff(IFeatureVector gt, IFeatureVector test) {
        if (gt instanceof PeakRTFeatureVector && test instanceof PeakRTFeatureVector) {
            double lhsRT = ((PeakRTFeatureVector) gt).getRT();
            double rhsRT = ((PeakRTFeatureVector) test).getRT();
            boolean lhsRTNaN = false, rhsRTNaN = false;
            lhsRTNaN = Double.isNaN(lhsRT);
            rhsRTNaN = Double.isNaN(rhsRT);
            lhsRT = (lhsRTNaN) ? 0 : lhsRT;
            rhsRT = (rhsRTNaN) ? 0 : rhsRT;
            if (lhsRTNaN && rhsRTNaN) {
                return 0;
            }
            double diff = Math.pow((lhsRT - rhsRT),2.0d);
//            System.out.println("DiffSq: "+diff);
            return diff;
        }
        return Double.NaN;
    }
}
