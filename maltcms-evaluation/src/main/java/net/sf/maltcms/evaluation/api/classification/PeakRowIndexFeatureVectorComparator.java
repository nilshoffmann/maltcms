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
package net.sf.maltcms.evaluation.api.classification;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class PeakRowIndexFeatureVectorComparator implements IFeatureVectorComparator<IRowIndexNamedPeakFeatureVector> {

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isFN(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isFN(IRowIndexNamedPeakFeatureVector gt, IRowIndexNamedPeakFeatureVector test) {
        if (gt instanceof IRowIndexNamedPeakFeatureVector && test instanceof IRowIndexNamedPeakFeatureVector) {
            final int lhsRT = ((IRowIndexNamedPeakFeatureVector) gt).getRowIndex();
            final int rhsRT = ((IRowIndexNamedPeakFeatureVector) test).getRowIndex();
            if (lhsRT!=-1 && rhsRT==-1) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isFP(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isFP(IRowIndexNamedPeakFeatureVector gt, IRowIndexNamedPeakFeatureVector test) {
        if (gt instanceof IRowIndexNamedPeakFeatureVector && test instanceof IRowIndexNamedPeakFeatureVector) {
            final int lhsRT = ((IRowIndexNamedPeakFeatureVector) gt).getRowIndex();
            final int rhsRT = ((IRowIndexNamedPeakFeatureVector) test).getRowIndex();
            //if gt is NaN, but test has a value, if FP=true
            if (lhsRT==-1 && rhsRT!=-1) {
                return true;
            }
            if (lhsRT!=-1 && rhsRT!=-1 && lhsRT!=rhsRT) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isTN(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isTN(IRowIndexNamedPeakFeatureVector gt, IRowIndexNamedPeakFeatureVector test) {
        if (gt instanceof IRowIndexNamedPeakFeatureVector && test instanceof IRowIndexNamedPeakFeatureVector) {
            final int lhsRT = ((IRowIndexNamedPeakFeatureVector) gt).getRowIndex();
            final int rhsRT = ((IRowIndexNamedPeakFeatureVector) test).getRowIndex();
            //both are NaNs
            if (lhsRT==-1 && rhsRT==-1) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see maltcms.experimental.eval.IFeatureVectorComparator#isTP(maltcms.datastructures.array.IFeatureVector, maltcms.datastructures.array.IFeatureVector)
     */
    @Override
    public boolean isTP(IRowIndexNamedPeakFeatureVector gt, IRowIndexNamedPeakFeatureVector test) {
        if (gt instanceof IRowIndexNamedPeakFeatureVector && test instanceof IRowIndexNamedPeakFeatureVector) {
            final int lhsRT = ((IRowIndexNamedPeakFeatureVector) gt).getRowIndex();
            final int rhsRT = ((IRowIndexNamedPeakFeatureVector) test).getRowIndex();
            //if either is a NaN or both, TP=false
            if ((lhsRT==-1 && rhsRT!=-1) || (lhsRT!=-1 && rhsRT==-1)) {
                return false;
            }
            //true positive if both have a value, and their difference is <=delta
            if (lhsRT!=-1 && rhsRT!=-1 && lhsRT == rhsRT) {
                return true;
            }
        }
        return false;
    }

    /**
     * NaN's are treated as 0, giving a large distance for x!=y and 0 for x=y,
     * with either x=NaN and y!=NaN, x!=NaN and y==NaN, or x=NaN=y
     *
     * @param gt
     * @param test
     * @return
     */
    @Override
    public double getSquaredDiff(IRowIndexNamedPeakFeatureVector gt, IRowIndexNamedPeakFeatureVector test) {
//        if (gt instanceof IRowIndexNamedPeakFeatureVector && test instanceof IRowIndexNamedPeakFeatureVector) {
//            double lhsRT = ((IRowIndexNamedPeakFeatureVector) gt).getRowIndex();
//            double rhsRT = ((IRowIndexNamedPeakFeatureVector) test).getRowIndex();
//            boolean lhsRTNaN = false, rhsRTNaN = false;
//            lhsRTNaN = Double.isNaN(lhsRT);
//            rhsRTNaN = Double.isNaN(rhsRT);
//            lhsRT = (lhsRTNaN) ? 0 : lhsRT;
//            rhsRT = (rhsRTNaN) ? 0 : rhsRT;
//            if (lhsRTNaN && rhsRTNaN) {
//                return 0;
//            }
//            double diff = Math.pow((lhsRT - rhsRT), 2.0d);
////            System.out.println("DiffSq: "+diff);
//            return diff;
//        }
        return Double.NaN;
    }
}
