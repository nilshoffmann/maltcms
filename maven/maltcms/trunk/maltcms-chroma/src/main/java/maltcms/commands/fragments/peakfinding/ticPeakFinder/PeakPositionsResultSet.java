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
package maltcms.commands.fragments.peakfinding.ticPeakFinder;

import java.util.List;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;

/**
 *
 * @author nilshoffmann
 */
public class PeakPositionsResultSet {

    private final double[] snrValues;
    private final List<Integer> ts;
    private final ArrayInt.D1 peakPositions;
    private final Array correctedTIC;
    private final PolynomialSplineFunction baselineEstimator;

    public PeakPositionsResultSet(Array correctedTIC,
            ArrayInt.D1 peakPositions, double[] snrValues, List<Integer> ts, PolynomialSplineFunction baselineEstimator) {
        this.snrValues = snrValues;
        this.ts = ts;
        this.peakPositions = peakPositions;
        this.correctedTIC = correctedTIC;
        this.baselineEstimator = baselineEstimator;
    }

    public ArrayInt.D1 getPeakPositions() {
        return peakPositions;
    }

    public double[] getSnrValues() {
        return snrValues;
    }

    public List<Integer> getTs() {
        return ts;
    }

    public Array getCorrectedTIC() {
        return correctedTIC;
    }

    public PolynomialSplineFunction getBaselineEstimator() {
        return baselineEstimator;
    }
}
