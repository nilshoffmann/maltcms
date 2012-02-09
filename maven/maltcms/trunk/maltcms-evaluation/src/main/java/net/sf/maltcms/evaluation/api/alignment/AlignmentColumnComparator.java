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
package net.sf.maltcms.evaluation.api.alignment;

import cross.datastructures.tools.EvalTools;
import ucar.ma2.IndexIterator;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
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
    
    public static final String[] names = new String[]{"TP","TN","FP","FN"};

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
                }else{
                    //WP
                    results[FP]++;
                }
            }

        }
        return results;
    }
}
