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

import cross.annotations.Configurable;
import java.util.ArrayList;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

/**
 * Estimates a baseline from a an array of function values using local minima to
 * fit a Loess polynomial spline function.
 *
 * @author nilshoffmann
 */
@Slf4j
@Data
public class LoessMinimaBaselineEstimator implements IBaselineEstimator {

    @Configurable
    private double bandwidth = LoessInterpolator.DEFAULT_BANDWIDTH;
    @Configurable
    private double accuracy = LoessInterpolator.DEFAULT_ACCURACY;
    @Configurable
    private int robustnessIterations = LoessInterpolator.DEFAULT_ROBUSTNESS_ITERS;
    @Configurable
    private int minimaWindow = 100;

    @Override
    public PolynomialSplineFunction findBaseline(double[] values) {
        final ArrayList<Integer> ts = new ArrayList<Integer>();
        for (int i = 0; i < values.length; i++) {
            log.debug("i=" + i);
            PeakFinderUtils.checkMinimum(values, ts, i,
                    minimaWindow);
        }
        log.info("Found {} minima for baseline estimation!", ts.size());
        //add the first index
        if (!ts.get(0).equals(Integer.valueOf(0))) {
            ts.add(0, 0);
        }
        //add the last index
        if (!ts.get(ts.size() - 1).equals(Integer.valueOf(values.length - 1))) {
            ts.add(values.length - 1);
        }
        double[] xvalues = new double[ts.size()];
        double[] yvalues = new double[ts.size()];
        int arrayIdx = 0;
        for (Integer idx : ts) {
            xvalues[arrayIdx] = idx;
            yvalues[arrayIdx] = values[idx];
            arrayIdx++;
        }
        LoessInterpolator lip;
        try {
            lip = new LoessInterpolator(bandwidth, robustnessIterations, accuracy);
            return lip.interpolate(xvalues, yvalues);
        } catch (MathException ex) {
            log.warn("{}", ex);
        }
        return null;
    }
}
