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
package net.sf.maltcms.evaluation.api;

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
