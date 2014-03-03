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
package maltcms.commands.fragments2d.preprocessing.default2dVarLoader;

import cross.datastructures.StatsMap;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.scanners.ArrayStatsScanner;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.Range;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Slf4j
@Data
public class ModulationTimeEstimatorTask implements Callable<Double>,
    Serializable {

    private URI input;
    private int numberOfScans = 5000;
    private int offset = 0;

    /**
     *
     * @return
     */
    @Override
    public Double call() {
        return estimateModulationTime(new FileFragment(input));
    }

    /**
     *
     * @param source
     * @return
     */
    protected double estimateModulationTime(final IFileFragment source) {
        IVariableFragment totalIntensity = source.getChild("total_intensity",
            true);
        Range r = totalIntensity.getRange()[0];
        int minIndex = r.first();
        int maxIndex = r.last();
        int startIndex = Math.max(minIndex, Math.max(0, offset));
        int stopIndex = Math.max(startIndex, Math.min(offset + numberOfScans - 1,
            maxIndex));
        try {
            totalIntensity.setRange(new Range[]{new Range(startIndex,
                stopIndex)});
        } catch (InvalidRangeException ex) {
            Logger.getLogger(ModulationTimeEstimatorTask.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        Array ticPart = totalIntensity.getArray();
        final StatsMap[] sma = new ArrayStatsScanner().apply(
            new Array[]{ticPart});
        final double mean = sma[0].get(cross.datastructures.Vars.Mean.toString());
        final double variance = sma[0].get(cross.datastructures.Vars.Variance.
            toString());
        final ArrayDouble.D1 acr = new ArrayDouble.D1(numberOfScans);
        for (int lag = 1; lag < numberOfScans - 1; lag++) {
            acr.setDouble(lag - 1, calcEstimatedAutoCorrelation(ticPart, mean,
                variance, lag));
        }

        ArrayList<Integer> maxIndices = new ArrayList<Integer>();
        Tuple2D<ArrayDouble.D1, ArrayInt.D1> t = findMaxima(acr,
            maxIndices);
        ArrayDouble.D1 maximaA = t.getFirst();
        ArrayInt.D1 maximaDiff = t.getSecond();
        return MAMath.getMaximum(maximaDiff);
    }

    /**
     * Find maxima in array a, returning an array containing all maxima, with
     * the same shape as a, and an array maximaDiff, which contains all
     * differences between maxima, of size (#of maxima - 1).
     *
     * @param a
     * @param maximaIndices
     * @return
     */
    public Tuple2D<ArrayDouble.D1, ArrayInt.D1> findMaxima(
        final Array a, final ArrayList<Integer> maximaIndices) {
        log.info("Looking for maxima!");
        int lastExtrIdx = 0;
        double lastExtr = 0.0d;
        double prev, current, next;
        double meanSoFar = 0.0d;
        final Index idx = a.getIndex();
        int nMaxima = 0;
        int lastMax = 0;
        final ArrayDouble.D1 maxima = new ArrayDouble.D1(a.getShape()[0]);
        for (int i = 1; i < a.getShape()[0] - 1; i++) {
            prev = a.getDouble(idx.set(i - 1));
            current = a.getDouble(idx.set(i));
            next = a.getDouble(idx.set(i + 1));
            if (isCandidate(prev, current, next) && (current > 0.4d)) {
                final double maxDev = 5 * (meanSoFar) / 100.0d;
                log.info("Current deviation {}, Maximum deviation: {}",
                    ((i - lastMax) - meanSoFar), maxDev);
                if (((i - lastMax) - meanSoFar) / meanSoFar <= maxDev) {
                    log.info(
                        "Maximum within 5% range of mean {} at lag {}",
                        current, i);
                    final int diff = i - lastExtrIdx;
                    final double vdiff = current - lastExtr;
                    log.info("Difference to last index {}, value {}",
                        diff, vdiff);
                    log.info("Number of scans between maxima: {}",
                        (i - lastMax));
                    lastExtrIdx = i;
                    lastExtr = current;
                    maxima.set(i, current);
                    maximaIndices.add(i);
                    nMaxima++;
                    if (meanSoFar == 0.0d) {
                        lastMax = i;
                        meanSoFar = i;
                    } else {
                        meanSoFar = ((i - lastMax) - meanSoFar) / (nMaxima + 1)
                            + (meanSoFar);
                        lastMax = i;

                    }
                    log.info("Mean so far: {}", meanSoFar);
                }
            }
        }
        final ArrayInt.D1 maximaDiff = new ArrayInt.D1(maximaIndices.size());
        int lastI = 0;
        int cnt = 0;
        for (final Integer maxI : maximaIndices) {
            maximaDiff.set(cnt++, (maxI - lastI));
            lastI = maxI;
        }
        return new Tuple2D<ArrayDouble.D1, ArrayInt.D1>(maxima, maximaDiff);

    }

    /**
     *
     * @param prev
     * @param current
     * @param next
     * @return
     */
    public boolean isCandidate(final double prev, final double current,
        final double next) {
        final boolean b = (prev < current) && (current > next);
        // log.info("Found candidate, checking additional constraints!");
        return b;
    }

    /**
     *
     * @param a
     * @param mean
     * @param variance
     * @param lag
     * @return
     */
    public double calcEstimatedAutoCorrelation(final Array a,
        final double mean, final double variance, final int lag) {
        EvalTools.eqI(a.getRank(), 1, this);
        final int n = a.getShape()[0];
        final int d = n - lag;
        final double norm = (d) * variance;
        // log.info("Norm={}",1.0d/norm);
        // log.info("d={}",d);
        double res = 0.0d;
        final Index ind = a.getIndex();
        for (int i = 0; i < d; i++) {
            res += (a.getDouble(ind.set(i)) - mean)
                * (a.getDouble(ind.set(i + lag)) - mean);
        }
        final double v = res / norm;
        return v;
//        log.debug("R'({})= {}", lag, v);
        // return v;
    }
}
