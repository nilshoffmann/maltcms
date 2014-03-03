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
package maltcms.commands.fragments.peakfinding.cwtEicPeakFinder;

import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
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
import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.wavelet.MexicanHatWaveletFilter;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.peak.PeakType;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.datastructures.rank.Rank;
import maltcms.datastructures.ridge.Ridge;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Data
@Slf4j
public class CwtTicPeakFinderCallable implements Callable<URI>, Serializable {

    @Configurable
    private int minScale = 5;
    @Configurable
    private int maxScale = 20;
    @Configurable
    private URI input;
    @Configurable
    private URI output;
    @Configurable
    private double minPercentile = 5.0d;
    @Configurable
    private boolean integratePeaks = true;
    @Configurable
    private boolean integrateRawTic = true;
    @Configurable
    private boolean storeScaleogram = true;
    @Configurable
    private List<IPeakNormalizer> peakNormalizers = Collections.emptyList();

    @Override
    public String toString() {
        return getClass().getName();
    }

    private List<Peak1D> createPeaksForRidges(IFileFragment f, Array tic,
        List<Ridge> r, MexicanHatWaveletFilter mhwf) {
        int index = 0;
        Index tidx = tic.getIndex();
        Array sat = f.getChild("scan_acquisition_time").getArray();
        Index sidx = sat.getIndex();
        List<Peak1D> p2 = new LinkedList<Peak1D>();
        for (Ridge ridge : r) {
            log.debug("Processing Ridge " + (index + 1) + " "
                + r.size());
            Peak1D p = new Peak1D();
            p.setApexIndex(ridge.getGlobalScanIndex());
            p.setFile(f.getName());
            p.setApexIntensity(tic.getDouble(tidx.set(p.getApexIndex())));
            p.setApexTime(sat.getDouble(sidx.set(p.getApexIndex())));
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
            //TODO SNR for wavelet ridges
            p.setSnr(Double.NaN);
            p.setArea(area);
            p.setStartTime(sat.getDouble(scaleBounds[0]));
            p.setStopTime(sat.getDouble(scaleBounds[1]));
            p.setPeakType(PeakType.TIC_RAW);
            p2.add(p);
            index++;
        }
        return p2;
    }

    @Override
    public URI call() {
        FileFragment ff = new FileFragment(input);
        Array values = ff.getChild("total_intensity").getArray();
        ArrayStatsScanner ass = new ArrayStatsScanner();
        StatsMap sm = ass.apply(new Array[]{values})[0];
        //normalize to 0..1
        MultiplicationFilter mf = new MultiplicationFilter(
            1.0 / (sm.get(Vars.Max.name()) - sm.get(Vars.Min.name())));
        values = mf.apply(values);
        Percentile p = new Percentile(minPercentile);
        double fivePercent = p.evaluate((double[]) values.get1DJavaArray(
            double.class));
        MexicanHatWaveletFilter cwt = new MexicanHatWaveletFilter();

        List<Double> scales = new LinkedList<Double>();

        final ArrayDouble.D2 scaleogram = new ArrayDouble.D2(
            values.getShape()[0],
            maxScale);
        for (int i = 1; i <= maxScale; i++) {
            double scale = ((double) i);
            // System.out.println("Scale: " + scale);
            cwt.setScale(scale);
            Array res = cwt.apply(values);
            Index resI = res.getIndex();
            for (int j = 0; j < res.getShape()[0]; j++) {
                scaleogram.set(j, i - 1, res.getDouble(resI.set(j)));
            }
            scales.add(scale);
        }
        List<Ridge> ridges = followRidgesBottomUp(fivePercent,
            scaleogram, scales, minScale, maxScale);

        List<Rank<Ridge>> ranks = new LinkedList<Rank<Ridge>>();
        for (Ridge r : ridges) {
            ranks.add(new Rank<Ridge>(r));
        }
        //FIXME remove equal ridges

        filterRidgesByResponse(ranks, values);
        Collections.sort(ranks);

        log.info("Found " + ridges.size() + " ridges at minScale=" + minScale + ", maxScale="
            + maxScale);

        List<Peak1D> peaks = createPeaksForRidges(ff, values, ridges, cwt);
        FileFragment outf = new FileFragment(output);
        if (storeScaleogram) {
            VariableFragment scaleogramVar = new VariableFragment(outf, "cwt_scaleogram");
            scaleogramVar.setArray(scaleogram);
        }
        outf.addSourceFile(ff);
        Peak1D.append(outf, peakNormalizers, peaks, values, "tic_peaks", "tic_filtered");
        outf.save();
        return output;
    }

    private void filterRidgesByResponse(List<Rank<Ridge>> ranks, Array tic) {
        // List<Ridge> rr = new ArrayList<Ridge>();
        for (Rank<Ridge> rank : ranks) {
            Ridge r = rank.getRidge();
            int x = (int) r.getRidgePoints().get(0).getFirst().getX();
            double val = tic.getDouble(x);
            // if (val >= minPercentile) {
            rank.addRank("response", -val);

        }
    }

    private List<Integer> getPeakMaxima(ArrayDouble.D2 scaleogram, int row) {
        double[] scaleResponse = (double[]) scaleogram.slice(1, row).
            get1DJavaArray(double.class);
        FirstDerivativeFilter fdf = new FirstDerivativeFilter();
        double[] res = (double[]) fdf.apply(Array.factory(scaleResponse)).
            get1DJavaArray(double.class);
        List<Integer> peakMaxima = new LinkedList<Integer>();
        for (int i = 1; i < scaleResponse.length - 1; i++) {
            if (res[i - 1] >= 0 && res[i + 1] <= 0) {
                // remove peaks, which are not true maxima
                peakMaxima.add(i);
            }
        }
        return peakMaxima;
    }

    private HashMap<Integer, Ridge> buildRidges(List<Integer> seeds,
        int scaleIdx, ArrayDouble.D2 scaleogram) {
        // System.out.println("Peak maxima: "+seeds);
        HashMap<Integer, Ridge> l = new LinkedHashMap<Integer, Ridge>();
        for (Integer itg : seeds) {
            Ridge r = new Ridge(new Point2D.Double(itg, scaleIdx),
                scaleogram.get(itg, scaleIdx));
            // System.out.println("Adding ridge: "+r);
            l.put(itg, r);
        }
        return l;
    }

    private double[] fillSeeds(int size, List<Integer> seeds,
        ArrayDouble.D2 scaleogram, int scaleIdx) {
        double[] b = new double[size];
        for (Integer itg : seeds) {
            int idx = itg.intValue();
            b[idx] = scaleogram.get(idx, scaleIdx);
        }
        return b;
    }

    private List<Ridge> followRidgesBottomUp(double percentile,
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
            // System.out.println("Checking scale " + scales.get(i)
            // + " with max trace diff " + scaleDiff);
            double[] newSeedlings = fillSeeds(columns, maxima, scaleogram, i);
            List<Integer> ridgesToRemove = new LinkedList<Integer>();
            for (Integer key : ridges.keySet()) {
                Ridge r = ridges.get(key);
                if (r.addPoint(scaleDiff, i, newSeedlings)) {
                    // System.out.println("Extended ridge: " + r);
                } else {
                    // System.out.println("Marking ridge for removal: " + r);
                    ridgesToRemove.add(key);
                }
            }
            for (Integer key : ridgesToRemove) {
                // System.out.println("Removing ridge: " + ridges.get(key));
                Ridge r = ridges.get(key);
                if (r.getSize() < minScale) {
                    ridges.remove(key);
                }
            }
            ridgesToRemove.clear();
            // System.out.println("Maxima at scale: " + maxima);
            // ridges.put(Integer.valueOf(i),findDiffs(seedlings,newSeedlings,i));

            // swap
            // seedlings = newSeedlings;
        }
        // put all Ridges with size>=maxScale into return list
        List<Ridge> l = new LinkedList<Ridge>();
        for (Integer key : ridges.keySet()) {
            Ridge r = ridges.get(key);
            // System.out.println("Testing ridge with " + r.getSize()
            // + " elements!");
            if (r.getSize() >= minScale
                && r.getRidgePoints().get(0).getSecond() >= percentile) {
                // System.out.println("RidgePenalty: " + r.getRidgePenalty());
                l.add(r);
            }
        }
        return l;
    }
}
