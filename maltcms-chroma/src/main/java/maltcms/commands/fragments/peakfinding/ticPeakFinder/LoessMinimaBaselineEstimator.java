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

import cross.annotations.Configurable;
import java.util.ArrayList;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Estimates a baseline from a an array of function values using local minima to
 * fit a Loess polynomial spline function.
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
@ServiceProvider(service=IBaselineEstimator.class)
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
    public PolynomialSplineFunction findBaseline(double[] xvalues, double[] yvalues) {
        final ArrayList<Integer> ts = new ArrayList<>();
        for (int i = 0; i < yvalues.length; i++) {
            log.debug("i=" + i);
            PeakFinderUtils.checkMinimum(yvalues, ts, i,
                    minimaWindow);
        }
        log.info("Found {} minima for baseline estimation!", ts.size());
        //add the first index
        if (!ts.get(0).equals(0)) {
            ts.add(0, 0);
        }
        //add the last index
        if (!ts.get(ts.size() - 1).equals(yvalues.length - 1)) {
            ts.add(yvalues.length - 1);
        }
        double[] xvalues1 = new double[ts.size()];
        double[] yvalues1 = new double[ts.size()];
        int arrayIdx = 0;
        for (Integer idx : ts) {
            xvalues1[arrayIdx] = xvalues[idx];
            yvalues1[arrayIdx] = yvalues[idx];
            arrayIdx++;
        }
//        double[] xvalues = new double[yvalues.length];
        LoessInterpolator lip;
        try {
            lip = new LoessInterpolator(bandwidth, robustnessIterations, accuracy);
            return lip.interpolate(xvalues1, yvalues1);
        } catch (MathException ex) {
            log.warn("{}", ex);
        }
        return null;
    }

    @Override
    public LoessMinimaBaselineEstimator copy() {
        LoessMinimaBaselineEstimator lmbe = new LoessMinimaBaselineEstimator();
        lmbe.setAccuracy(accuracy);
        lmbe.setBandwidth(bandwidth);
        lmbe.setMinimaWindow(minimaWindow);
        lmbe.setRobustnessIterations(robustnessIterations);
        return lmbe;
    }
}
