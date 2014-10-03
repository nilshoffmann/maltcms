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
package net.sf.maltcms.evaluation.api;

import maltcms.datastructures.array.IFeatureVector;

/**
 * <p>PeakRTFeatureVectorComparator class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class PeakRTFeatureVectorComparator implements IFeatureVectorComparator {

    private final double delta;

    /**
     * <p>Constructor for PeakRTFeatureVectorComparator.</p>
     *
     * @param delta a double.
     */
    public PeakRTFeatureVectorComparator(double delta) {
        this.delta = delta;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isFN(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
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
     * {@inheritDoc}
     *
     * NaN's are treated as 0, giving a large distance for x!=y and 0 for x=y,
     * with either x=NaN and y!=NaN, x!=NaN and y==NaN, or x=NaN=y
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
            double diff = Math.pow((lhsRT - rhsRT), 2.0d);
//            log.info("DiffSq: "+diff);
            return diff;
        }
        return Double.NaN;
    }
}
