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
package net.sf.maltcms.evaluation.api.alignment;

import cross.datastructures.tools.EvalTools;
import ucar.ma2.IndexIterator;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class AlignmentColumnComparator {

    private final double delta;

    public AlignmentColumnComparator(double delta) {
        this.delta = delta;
    }
    //TP is true positive, a match, when both values are within delta bounds
    public final static int TP = 0;
    //TN is true negative, if both values should be absent
    public final static int TN = 1;
    //FP is false positive, if gt value is absent, but tool value is present
    public final static int FP = 2;
    //FN is false negative, if gt value is present, but tools value is absent
    public final static int FN = 3;

    public static int[] createResultsArray() {
        return new int[4];
    }
    public static final String[] names = new String[]{"TP", "TN", "FP", "FN"};

    public int[] compare(AlignmentColumn gt, AlignmentColumn test) {
        int[] results = new int[names.length];
        EvalTools.eqI(gt.getRT().getShape()[0], test.getRT().getShape()[0], this);
        IndexIterator gtIter = gt.getRT().getIndexIterator();
        IndexIterator toolIter = test.getRT().getIndexIterator();
        while (gtIter.hasNext() && toolIter.hasNext()) {
            double gtVal = gtIter.getDoubleNext();
            double toolVal = toolIter.getDoubleNext();
            if (Double.isNaN(gtVal) && Double.isNaN(toolVal)) {
                //TN
                results[TN]++;
            } else if (Double.isNaN(gtVal) && !Double.isNaN(toolVal)) {
                //FP
                results[FP]++;
            } else if (!Double.isNaN(gtVal) && Double.isNaN(toolVal)) {
                //FN
                results[FN]++;
            } else {
                if (Math.abs(gtVal - toolVal) <= delta) {
                    //TP
                    results[TP]++;
                } else {
                    //WP
                    results[FP]++;
                }
            }

        }
        return results;
    }
}
