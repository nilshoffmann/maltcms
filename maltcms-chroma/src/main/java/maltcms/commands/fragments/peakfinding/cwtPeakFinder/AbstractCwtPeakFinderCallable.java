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
package maltcms.commands.fragments.peakfinding.cwtPeakFinder;

import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.commands.filters.array.wavelet.MexicanHatWaveletFilter;
import maltcms.commands.fragments.peakfinding.io.Peak1DUtilities;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.PeakFinderWorkerResult;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.Peak1D.Peak1DBuilder;
import maltcms.datastructures.peak.PeakType;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.datastructures.rank.Rank;
import maltcms.datastructures.ridge.Ridge;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;

/**
 * <p>
 * AbstractCwtPeakFinderCallable class.</p>
 *
 * @author Nils Hoffmann
 */
@Data
@Slf4j
public abstract class AbstractCwtPeakFinderCallable implements Callable<PeakFinderWorkerResult>, Serializable {

    @Configurable
    private int minScale = 5;
    @Configurable
    private int maxScale = 20;
    @Configurable
    private URI input;
    @Configurable
    private URI output;
    @Deprecated
    @Configurable(description="Deprecated. Is not used internally.")
    private double minPercentile = 5.0d;
    @Configurable
    private boolean integratePeaks = true;
    @Configurable
    private boolean storeScaleogram = true;
    @Configurable
    private List<IPeakNormalizer> peakNormalizers = Collections.emptyList();

    private final Peak1DUtilities peakUtilities = new Peak1DUtilities();
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    protected List<Peak1D> createPeaksForRidges(IFileFragment f, Array tic,
            List<Ridge> r, MexicanHatWaveletFilter mhwf, PeakType peakType) {
        int index = 0;
        Index tidx = tic.getIndex();
        Array sat = f.getChild("scan_acquisition_time").getArray();
        Index sidx = sat.getIndex();
        List<Peak1D> peaks = new LinkedList<>();
        for (Ridge ridge : r) {
            log.debug("Processing Ridge " + (index + 1) + " "
                    + r.size());
            Peak1D p = Peak1D.builder1D().
                startIndex(ridge.getGlobalScanIndex()).
                apexIndex(ridge.getGlobalScanIndex()).
                stopIndex(ridge.getGlobalScanIndex()).
                file(f.getName()).
                apexIntensity(tic.getDouble(tidx.set(ridge.getGlobalScanIndex()))).
                apexTime(sat.getDouble(sidx.set(ridge.getGlobalScanIndex()))).build();
                        
            int[] scaleBounds = mhwf.getContinuousWaveletTransform().
                    getBoundsForWavelet(p.getApexIndex(), ridge.
                            getIndexOfMaximum(), tic.getShape()[0]);
            //TODO area integration raw vs filtered (wavelet peak shapes)
            double area = 0.0d;
            for (int i = scaleBounds[0]; i <= scaleBounds[1]; i++) {
                area += tic.getDouble(i);
            }
            p.setStartIndex(scaleBounds[0]);
            p.setStopIndex(scaleBounds[1]);
            p.setSnr(Double.NaN);
            p.setArea(area);
            p.setStartTime(sat.getDouble(scaleBounds[0]));
            p.setStopTime(sat.getDouble(scaleBounds[1]));
            p.setPeakType(peakType);
            peaks.add(p);
            index++;
        }
        return peaks;
    }

    protected void filterRidgesByResponse(List<Rank<Ridge>> ranks, Array tic) {
        // List<Ridge> rr = new ArrayList<Ridge>();
        for (Rank<Ridge> rank : ranks) {
            Ridge r = rank.getRidge();
            int x = (int) r.getRidgePoints().get(0).getFirst().getX();
            double val = tic.getDouble(x);
            // if (val >= minPercentile) {
            rank.addRank("response", -val);

        }
    }

    protected List<Integer> getPeakMaxima(ArrayDouble.D2 scaleogram, int row) {
        double[] scaleResponse = (double[]) scaleogram.slice(1, row).
                get1DJavaArray(double.class);
        FirstDerivativeFilter fdf = new FirstDerivativeFilter();
        double[] res = (double[]) fdf.apply(Array.factory(scaleResponse)).
                get1DJavaArray(double.class);
        List<Integer> peakMaxima = new LinkedList<>();
        for (int i = 1; i < scaleResponse.length - 1; i++) {
            if (res[i - 1] >= 0 && res[i + 1] <= 0) {
                // remove peaks, which are not true maxima
                peakMaxima.add(i);
            }
        }
        return peakMaxima;
    }

    protected HashMap<Integer, Ridge> buildRidges(List<Integer> seeds,
            int scaleIdx, ArrayDouble.D2 scaleogram) {
        // log.info("Peak maxima: "+seeds);
        HashMap<Integer, Ridge> l = new LinkedHashMap<>();
        for (Integer itg : seeds) {
            Ridge r = new Ridge(new Point2D.Double(itg, scaleIdx),
                    scaleogram.get(itg, scaleIdx));
            // log.info("Adding ridge: "+r);
            l.put(itg, r);
        }
        return l;
    }

    protected double[] fillSeeds(int size, List<Integer> seeds,
            ArrayDouble.D2 scaleogram, int scaleIdx) {
        double[] b = new double[size];
        for (Integer itg : seeds) {
            int idx = itg;
            b[idx] = scaleogram.get(idx, scaleIdx);
        }
        return b;
    }

    protected List<Ridge> followRidgesBottomUp(double percentile,
            ArrayDouble.D2 scaleogram, List<Double> scales, int minScale,
            int maxScale) {
        int columns = scaleogram.getShape()[0];
        // get peak maxima for first scale
        List<Integer> seeds = getPeakMaxima(scaleogram, 0);
        HashMap<Integer, Ridge> ridges = buildRidges(seeds, 0, scaleogram);
        // build array for maxima
        // TODO this could be done more space efficient
        // double[] seedlings = fillSeeds(columns,seeds,scaleogram,0);
        for (int i = 1; i < maxScale; i++) {
            // double scale = scales.get(i);
            List<Integer> maxima = getPeakMaxima(scaleogram, i);
            int scaleDiff = 1;// 2 * (int) (scales.get(i) - scales.get(i - 1));
            // log.info("Checking scale " + scales.get(i)
            // + " with max trace diff " + scaleDiff);
            double[] newSeedlings = fillSeeds(columns, maxima, scaleogram, i);
            List<Integer> ridgesToRemove = new LinkedList<>();
            for (Integer key : ridges.keySet()) {
                Ridge r = ridges.get(key);
                if (r.addPoint(scaleDiff, i, newSeedlings)) {
                    // log.info("Extended ridge: " + r);
                } else {
                    // log.info("Marking ridge for removal: " + r);
                    ridgesToRemove.add(key);
                }
            }
            for (Integer key : ridgesToRemove) {
                // log.info("Removing ridge: " + ridges.get(key));
                Ridge r = ridges.get(key);
                if (r.getSize() < minScale) {
                    ridges.remove(key);
                }
            }
            ridgesToRemove.clear();
            // log.info("Maxima at scale: " + maxima);
            // ridges.put(Integer.valueOf(i),findDiffs(seedlings,newSeedlings,i));

            // swap
            // seedlings = newSeedlings;
        }
        // put all Ridges with size>=maxScale into return list
        List<Ridge> l = new LinkedList<>();
        for (Integer key : ridges.keySet()) {
            Ridge r = ridges.get(key);
            // log.info("Testing ridge with " + r.getSize()
            // + " elements!");
            if (r.getSize() >= minScale
                    && r.getRidgePoints().get(0).getSecond() >= percentile) {
                // log.info("RidgePenalty: " + r.getRidgePenalty());
                l.add(r);
            }
        }
        return l;
    }
}
