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
