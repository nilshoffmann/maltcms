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

import cross.datastructures.tools.EvalTools;
import cross.tools.MathTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;

/**
 * <p>PeakFinderUtils class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

public class PeakFinderUtils {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PeakFinderUtils.class);

    /**
     * <p>isAboveThreshold.</p>
     *
     * @param snrdb a double.
     * @param threshold a double.
     * @return a boolean.
     */
    public static boolean isAboveThreshold(final double snrdb, final double threshold) {
        return (snrdb >= threshold);
    }

    /**
     * <p>isCandidate.</p>
     *
     * @param index a int.
     * @param values an array of double.
     * @param window a int.
     * @return a boolean.
     */
    public static boolean isCandidate(final int index, final double[] values,
            final int window) {
        final double max = MathTools.max(values, index - window, index + window);
        final double indxVal = values[index];
        if (max == indxVal) {
            return true;
        }
        return false;
    }

    /**
     * <p>isMinCandidate.</p>
     *
     * @param index a int.
     * @param values an array of double.
     * @param window a int.
     * @return a boolean.
     */
    public static boolean isMinCandidate(final int index, final double[] values,
            final int window) {
        final double min = MathTools.min(values, index - window, index + window);
        final double indxVal = values[index];
        if (min == indxVal) {
            return true;
        }
        return false;
    }

    /**
     * <p>checkExtremum.</p>
     *
     * @param values an array of double.
     * @param snr an array of double.
     * @param ts a {@link java.util.ArrayList} object.
     * @param threshold a double.
     * @param i a int.
     * @param window a int.
     */
    public static void checkExtremum(final double[] values, final double[] snr,
            final ArrayList<Integer> ts, final double threshold, final int i,
            final int window) {
        EvalTools.notNull(new Object[]{values, i, threshold}, PeakFinderUtils.class);
        if ((values[i] > 0) && isAboveThreshold(snr[i], threshold)
                && isCandidate(i, values, window)) {
            ts.add(i);
            log.debug(
                    "Found extremum above snr threshold {} with value {} at scan: {}",
                    new Object[]{threshold, values[i], i});
        }
    }

    /**
     * <p>checkMinimum.</p>
     *
     * @param values an array of double.
     * @param ts a {@link java.util.Collection} object.
     * @param i a int.
     * @param window a int.
     */
    public static void checkMinimum(final double[] values,
            final Collection<Integer> ts, final int i,
            final int window) {
        EvalTools.notNull(new Object[]{values, i}, PeakFinderUtils.class);
        if (isMinCandidate(i, values, window)) {
            ts.add(i);
            log.debug(
                    "Found extremum with value {} at scan: {}",
                    new Object[]{values[i], i});
        }
    }

    /**
     * <p>isMinimum.</p>
     *
     * @param prev a double.
     * @param current a double.
     * @param next a double.
     * @return a boolean.
     */
    public static boolean isMinimum(final double prev, final double current,
            final double next) {
        if ((current < prev) && (current < next)) {
            return true;
        }
        if (current == 0) {
            return true;
        }
        return false;
    }

    /**
     * <p>createPeakCandidatesArray.</p>
     *
     * @param tic a {@link ucar.ma2.Array} object.
     * @param ts a {@link java.util.ArrayList} object.
     * @return a {@link ucar.ma2.ArrayInt.D1} object.
     */
    public static ArrayInt.D1 createPeakCandidatesArray(final Array tic,
            final ArrayList<Integer> ts) {
        EvalTools.notNull(ts, PeakFinderUtils.class);
        final ArrayInt.D1 extr = new ArrayInt.D1(ts.size());
        // checkUniformDistribution(tic.getShape()[0], ts);
        for (int i = 0; i < ts.size(); i++) {
            extr.set(i, ts.get(i));
        }
        return extr;
    }

    /**
     * <p>findBaseline.</p>
     *
     * @param ticValues an array of double.
     * @param baselineEstimationMinimaWindow a int.
     * @param accuracy a double.
     * @param bandwidth a double.
     * @param robustnessIterations a int.
     * @return a {@link org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction} object.
     */
    public static PolynomialSplineFunction findBaseline(double[] ticValues, int baselineEstimationMinimaWindow, double accuracy, double bandwidth, int robustnessIterations) {
        final LinkedHashSet<Integer> ts = new LinkedHashSet<>();
        for (int i = 0; i < ticValues.length; i++) {
            log.debug("i=" + i);
            PeakFinderUtils.checkMinimum(ticValues, ts, i,
                    baselineEstimationMinimaWindow);
        }

        log.info("Found {} minima for baseline estimation!", ts.size());
        ArrayList<Integer> tss = new ArrayList<>(ts);
        //add the first index
        if (!tss.get(0).equals(0)) {
            tss.add(0, 0);
        }
        //add the last index
        if (!tss.get(ts.size() - 1).equals(ticValues.length - 1)) {
            tss.add(ticValues.length - 1);
        }
        double[] xvalues = new double[ts.size()];
        double[] yvalues = new double[ts.size()];
        int arrayIdx = 0;
        for (Integer idx : ts) {
            xvalues[arrayIdx] = idx;
            yvalues[arrayIdx] = ticValues[idx];
            arrayIdx++;
        }
        LoessInterpolator lip;
        try {
            lip = new LoessInterpolator(bandwidth, robustnessIterations, accuracy);
            return lip.interpolate(xvalues, yvalues);
        } catch (MathException ex) {
            Logger.getLogger(PeakFinderUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * <p>findBaseline.</p>
     *
     * @param ticValues an array of double.
     * @param baselineEstimationMinimaWindow a int.
     * @return a {@link org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction} object.
     */
    public static PolynomialSplineFunction findBaseline(double[] ticValues, int baselineEstimationMinimaWindow) {
        return findBaseline(ticValues, baselineEstimationMinimaWindow, LoessInterpolator.DEFAULT_ACCURACY, LoessInterpolator.DEFAULT_BANDWIDTH, LoessInterpolator.DEFAULT_ROBUSTNESS_ITERS);
    }
}
