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
package maltcms.commands.fragments.peakfinding.ticPeakFinder;

import java.util.List;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;

/**
 * <p>PeakPositionsResultSet class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class PeakPositionsResultSet {

    private final double[] snrValues;
    private final List<Integer> ts;
    private final ArrayInt.D1 peakPositions;
    private final Array correctedValues;
    private final PolynomialSplineFunction baselineEstimator;

    /**
     * <p>Constructor for PeakPositionsResultSet.</p>
     *
     * @param correctedTIC a {@link ucar.ma2.Array} object.
     * @param peakPositions a {@link ucar.ma2.ArrayInt.D1} object.
     * @param snrValues an array of double.
     * @param ts a {@link java.util.List} object.
     * @param baselineEstimator a {@link org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction} object.
     */
    public PeakPositionsResultSet(Array correctedTIC,
            ArrayInt.D1 peakPositions, double[] snrValues, List<Integer> ts, PolynomialSplineFunction baselineEstimator) {
        this.snrValues = snrValues;
        this.ts = ts;
        this.peakPositions = peakPositions;
        this.correctedValues = correctedTIC;
        this.baselineEstimator = baselineEstimator;
    }

    /**
     * <p>Getter for the field <code>peakPositions</code>.</p>
     *
     * @return a {@link ucar.ma2.ArrayInt.D1} object.
     */
    public ArrayInt.D1 getPeakPositions() {
        return peakPositions;
    }

    /**
     * <p>Getter for the field <code>snrValues</code>.</p>
     *
     * @return an array of double.
     */
    public double[] getSnrValues() {
        return snrValues;
    }

    /**
     * <p>Getter for the field <code>ts</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Integer> getTs() {
        return ts;
    }

    /**
     * <p>Getter for the field <code>correctedTIC</code>.</p>
     *
     * @return a {@link ucar.ma2.Array} object.
     */
    public Array getCorrectedTIC() {
        return correctedValues;
    }

    /**
     * <p>Getter for the field <code>baselineEstimator</code>.</p>
     *
     * @return a {@link org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction} object.
     */
    public PolynomialSplineFunction getBaselineEstimator() {
        return baselineEstimator;
    }
}
