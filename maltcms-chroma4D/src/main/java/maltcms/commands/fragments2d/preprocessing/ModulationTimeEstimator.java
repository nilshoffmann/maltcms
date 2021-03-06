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
package maltcms.commands.fragments2d.preprocessing;

import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.CombinedDomainXYChart;
import maltcms.ui.charts.HeatMapChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYChart;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.XYPlot;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayInt.D1;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

/**
 * ModulationTimeEstimator tries to find the spike-like modulation peaks in
 * GCxGC MS data (Leco exported to netcdf ANDIMS), in order to estimate the
 * second retention time parameter.
 *
 * Use {
 *
 * @see Default2DVarLoader} instead with flag estimateModulationTime=true
 * @author Nils Hoffmann
 * 
 */
@Slf4j
@Data
//@ServiceProvider(service=AFragmentCommand.class)
@Deprecated
public class ModulationTimeEstimator extends AFragmentCommand {

    private String tic_var = "total_intensity";
    private String mass_var = "mass_values";
    private String inten_var = "intensity_values";
    private final double secondColumnTime = 5.0d;
    private final double scanRate = 200.0d;
    private double doubleFillValue;
    private double threshold;

    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final ArrayList<IFileFragment> ret = new ArrayList<>();
        for (final IFileFragment ff : t) {
            final IFileFragment fret = new FileFragment(
                    new File(getWorkflow().getOutputDirectory(this),
                            ff.getName()));

            findSecondRetentionTimes(ff, fret);
            fret.save();
            ret.add(fret);
        }
        return new TupleND<>(ret);
    }

    private Tuple2D<ArrayDouble.D1, ArrayDouble.D1> buildScanAcquisitionTime(
            final int scansPerModulation, final int numberOfScans,
            final double avgSat) {
        final ArrayDouble.D1 fstColTime = new ArrayDouble.D1(numberOfScans
                / scansPerModulation);
        final ArrayDouble.D1 sndColTime = new ArrayDouble.D1(scansPerModulation);
        for (int j = 0; j < scansPerModulation; j++) {
            sndColTime.set(j, j * avgSat);
        }
        for (int i = 0; i < fstColTime.getShape()[0]; i++) {
            fstColTime.set(i, ((double) i * scansPerModulation) * avgSat);
        }
        return new Tuple2D<>(fstColTime,
                sndColTime);
    }

    /**
     * <p>buildSecondRetentionTime.</p>
     *
     * @param tic a {@link ucar.ma2.ArrayInt.D1} object.
     * @param maxima a {@link java.util.ArrayList} object.
     * @return a {@link ucar.ma2.ArrayInt.D2} object.
     */
    protected ArrayInt.D2 buildSecondRetentionTime(final ArrayInt.D1 tic,
            final ArrayList<Integer> maxima) {
        return null;
    }

    private ArrayList<Array> buildTIC2D(final int scansPerModulation,
            final Array tic) {
        log.info("Number of tics {}", tic.getShape()[0]);
        log.info("Number of scans {}", scansPerModulation);
        final int size = tic.getShape()[0] / scansPerModulation;
        log
                .info("Building TIC arrays with fixed number of scans per modulation");
        log.info("Reconstructing {} scans", size);
        final ArrayList<Array> al = new ArrayList<>(size);
        int offset = 0;
        final int len = scansPerModulation;
        for (int i = 0; i < size; i++) {
            log.info("Range for scan {}: Offset {}, Length: {}",
                    new Object[]{i, offset, len});
            try {
                if ((offset + len) < tic.getShape()[0]) {
                    final Array a = tic.section(new int[]{offset},
                            new int[]{len});
                    // log.info("Scan " + (i + 1));
                    // log.info(a.toString());
                    al.add(a);
                } else {
                    log.warn("Omitting rest! Scan {}, offset {}, len {}",
                            new Object[]{i, offset, len});
                }
            } catch (final InvalidRangeException e) {
   
                log.warn(e.getLocalizedMessage());
            }
            offset += len;
        }
        return al;
    }

    private ArrayList<Array> buildTIC2D(final int scansPerModulation,
            final D1 msi, final Array tic) {
        final int size = msi.getShape()[0];
        final ArrayList<Array> al = new ArrayList<>(size);
        int offset = 0;
        final int len = scansPerModulation;
        for (int i = 0; i < size; i++) {
            offset = msi.get(i);
            log.info("Range for scan {}: Offset {}, Length: {}",
                    new Object[]{i, offset, len});
            try {
                if ((offset + len) < tic.getShape()[0]) {
                    final Array a = tic.section(new int[]{offset},
                            new int[]{len});
                    // log.info("Scan " + (i + 1));
                    // log.info(a.toString());
                    al.add(a);
                } else {
                    log.warn("Omitting rest! Scan {}, offset {}, len {}",
                            new Object[]{i, offset, len});
                }
            } catch (final InvalidRangeException e) {
   
                log.warn(e.getLocalizedMessage());
            }
        }
        return al;
    }

    /**
     * <p>calcEstimatedAutoCorrelation.</p>
     *
     * @param a
     * @param acr
     * @param mean a double.
     * @param variance a double.
     * @param lag a int.
     * @param acr a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    protected void calcEstimatedAutoCorrelation(final Array a,
            final double mean, final double variance, final int lag,
            final ArrayDouble.D1 acr) {
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
        acr.set(lag - 1, v);
        log.debug("R'({})= {}", lag, v);
        // return v;
    }

    /**
     * <p>checkDeltas.</p>
     *
     * @param al a {@link java.util.ArrayList} object.
     */
    protected void checkDeltas(final ArrayList<Tuple2D<Integer, Double>> al) {
        Tuple2D<Integer, Double> tple = al.remove(0);
        final ArrayInt.D1 deltas = new ArrayInt.D1(al.size() - 1);
        int i = 0;
        for (final Tuple2D<Integer, Double> t : al) {
            final int d = t.getFirst() - tple.getFirst();
            log.info("d = {}", d);
            deltas.set(i++, d);
            tple = t;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        this.tic_var = cfg.getString("var.total_intensity", "total_intensity");
        this.mass_var = cfg.getString("var.mass_values", "mass_values");
        this.inten_var = cfg.getString("var.intensity_values",
                "intensity_values");
        this.doubleFillValue = cfg.getDouble(
                "ucar.nc2.NetcdfFile.fillValueDouble", 9.9692099683868690e+36);
        this.threshold = cfg.getDouble("images.thresholdLow", 0.0d);
    }

    /**
     * Find maxima in array a, returning an array containing all maxima, with
     * the same shape as a, and an array maximaDiff, which contains all
     * differences between maxima, of size (#of maxima - 1).
     *
     * @param a a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param maximaIndices a {@link java.util.ArrayList} object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    protected Tuple2D<ArrayDouble.D1, ArrayInt.D1> findMaxima(
            final ArrayDouble.D1 a, final ArrayList<Integer> maximaIndices) {
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
            prev = a.get(idx.set(i - 1));
            current = a.get(idx.set(i));
            next = a.get(idx.set(i + 1));
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
        return new Tuple2D<>(maxima, maximaDiff);

    }

    /**
     * <p>findSecondRetentionTimes.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param fret a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    protected void findSecondRetentionTimes(final IFileFragment ff,
            final IFileFragment fret) {
        // Tuple2D<ArrayList<Array>,ArrayList<Array>> t =
        // MaltcmsTools.getMZIs(ff);
        Array tic = MaltcmsTools.getTIC(ff);
        int maxIndex = 0;
        final IndexIterator ii = tic.getIndexIterator();
        while (ii.hasNext()) {
            if (ii.getDoubleNext() == this.doubleFillValue) {
                break;
            } else {
                maxIndex++;
            }
        }
        if (maxIndex < tic.getShape()[0]) {
            try {
                tic = tic.section(new int[]{0}, new int[]{maxIndex},
                        new int[]{1});
            } catch (final InvalidRangeException e) {
                log.warn(e.getLocalizedMessage());
            }
        }
        final Array sat = ff.getChild("scan_acquisition_time").getArray();
        final ArrayDouble.D1 satDiff = new ArrayDouble.D1(tic.getShape()[0] - 1);
        final Index sati = sat.getIndex();
        for (int i = 1; i < tic.getShape()[0]; i++) {
            satDiff.set(i - 1, sat.getDouble(sati.set(i))
                    - sat.getDouble(sati.set(i - 1)));
        }
        EvalTools.eqI(tic.getRank(), 1, this);
        final ArrayStatsScanner ass = new ArrayStatsScanner();
        final StatsMap[] sma = ass.apply(new Array[]{tic, satDiff});
        final double mean = sma[0].get(cross.datastructures.Vars.Mean
                .toString());
        final double variance = sma[0].get(cross.datastructures.Vars.Variance
                .toString());
        final int ubound = Math.min((tic.getShape()[0] - 1) / 10,
                tic.getShape()[0] - 1);
        final ArrayDouble.D1 acr = new ArrayDouble.D1(ubound);

        final double satMax = sma[1].get(cross.datastructures.Vars.Max
                .toString());
        final double satMin = sma[1].get(cross.datastructures.Vars.Min
                .toString());
        final double satMean = sma[1].get(cross.datastructures.Vars.Mean
                .toString());
        log.info("scan_acquisition_time deltas: Max: {} Min: {} Mean: {}",
                new Object[]{satMax, satMin, satMean});
        final double satMeanInv = 1.0d / satMean;
        log.info("Estimated number of scans per second: {}", satMeanInv);
        log
                .info(
                        "Estimated scans per modulation with {}s second column time: {}",
                        this.secondColumnTime, this.secondColumnTime
                        * satMeanInv);
        // ArrayList<Tuple2D<Integer, Double>> maxima = new
        // ArrayList<Tuple2D<Integer, Double>>();
        // double min = Double.POSITIVE_INFINITY;
        // int minindex = 0;
        final ArrayInt.D1 domain = new ArrayInt.D1(ubound);
        final int dindex = 0;
        // double current, next, prev;
        // for (int lag = 1; lag < ubound; lag++) {
        // domain.set(dindex++, lag);
        // calcEstimatedAutoCorrelation(tic, mean, variance, lag, acr);
        // }
        //
        // log.info("Autocorrelation: ");
        // // log.info("{}",acr);
        // double max = MAMath.getMaximum(acr);
        // log.info("Maximum autocorrelation value: {}", max);
        // VariableFragment ac = FragmentTools
        // .getVariable(fret, "autocorrelation");
        // ac.setArray(acr);
        ArrayList<Integer> maxIndices = new ArrayList<>();
        Tuple2D<ArrayDouble.D1, ArrayInt.D1> t = findMaxima(acr,
                maxIndices);
        ArrayDouble.D1 maximaA = t.getFirst();
        // ArrayInt.D1 maximaDiff = t.getSecond();
        // VariableFragment acdomain = FragmentTools.getVariable(fret,
        // "autocorrelation_domain");
        // acdomain.setArray(domain);
        // Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times =
        // getModulationTime(fret,satMean,sat,
        // maximaA, maximaDiff);
        // log.info("{}",fret.toString());
        // //ArrayDouble.D1 secondColumnTime = times.getSecond();
        // //ArrayDouble.D1 modulationTime = times.getFirst();
        final IVariableFragment total_intensity = new VariableFragment(fret,
                "total_intensity");
        total_intensity.setArray(tic);// ff.getChild("total_intensity");
        // VariableFragment modulation_scan_index =
        // fret.getChild("modulation_scan_index");
        // //total_intensity.setIndex(modulation_scan_index);
        // ArrayInt.D1 msi = (ArrayInt.D1)modulation_scan_index.getArray();
        // log.info("{}",msi.getShape()[0]);
        // log.info("{}",tic.getShape()[0]);

        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(Factory.getInstance()
                .getConfiguration().getString("images.colorramp",
                        "res/colorRamps/bw.csv"));

        // int scansPerMod =
        // ((ArrayInt.D0)fret.getChild("scans_per_modulation").getArray()).get();
        // ArrayList<Array> tic2D = buildTIC2D(scansPerMod,msi,tic);
        // ArrayList<Array> tic2D = buildTIC2D(
        // (int) (secondColumnTime * satMeanInv), tic);
        final ArrayList<Array> tic2D = buildTIC2D(
                (int) (this.secondColumnTime * this.scanRate), tic);

        XYChart xyc = new XYChart("Autocorrelation within "
                + ff.getName(),
                new String[]{"Autocorrelation"}, new Array[]{acr},
                new ArrayInt[]{domain}, "Lag", "Autocorrelation");
        XYChart xym = new XYChart("Autocorrelation within "
                + ff.getName(),
                new String[]{"Maxima"}, new Array[]{maximaA},
                new ArrayInt[]{domain}, "Lag", "Autocorrelation");
        ArrayList<XYPlot> alp = new ArrayList<>();
        alp.add(xyc.create());
        alp.add(xym.create());
        CombinedDomainXYChart cdc = new CombinedDomainXYChart(
                "Combined Domains", "autocorrelation", true, alp);

        PlotRunner pr = new PlotRunner(cdc.create(), "Autocorrelation within "
                + ff.getName(), ff.getName() + "_autocorrelation", getWorkflow().getOutputDirectory(this));
        pr.configure(Factory.getInstance().getConfiguration());
        try {
            pr.call();
            // ArrayFactory.submitJob(pr);
        } catch (Exception ex) {
            Logger.getLogger(ModulationTimeEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }

        final BufferedImage bi = ImageTools.fullSpectrum(ff.getName(), tic2D,
                (int) (this.secondColumnTime * this.scanRate), colorRamp, 1024,
                true, this.threshold);
        ImageTools.saveImage(bi, ff.getName() + "-chromatogram", "png",
                getWorkflow().getOutputDirectory(this), this);
        final HeatMapChart hmc = new HeatMapChart(bi, "time 1 [s]",
                "time 2 [s]", buildScanAcquisitionTime(
                        (int) (this.secondColumnTime * this.scanRate), tic
                        .getShape()[0], satMean), ff.getUri().getPath());
        final PlotRunner pl = new PlotRunner(hmc.create(), "Chromatogram of "
                + ff.getUri().getPath(), "chromatogram-" + ff.getName(),
                getWorkflow().getOutputDirectory(this));
        pl.configure(Factory.getInstance().getConfiguration());
        try {
            pl.call();
        } catch (Exception ex) {
            Logger.getLogger(ModulationTimeEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }
//        Factory.getInstance().submitJob(pl);
        // checkDeltas(maxima);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Reconstructs second retention time of 2DGC-MS chromatograms from given scan rate and second column time.";
    }

    /**
     * We need: All Tics, scan acquisition times for every scan, all maxima of
     * the tic (modulation peaks of solvent), number of scans in between maxima.
     *
     * @param fret a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param satMean a double.
     * @param satMean
     * @param sat a {@link ucar.ma2.Array} object.
     * @param maximaA a {@link ucar.ma2.ArrayDouble.D1} object.
     * @param maximaDiff a ucar$ma2$ArrayInt$D1 object.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    protected Tuple2D<ArrayDouble.D1, ArrayDouble.D1> getModulationTime(
            final IFileFragment fret, final double satMean, final Array sat,
            final ArrayDouble.D1 maximaA, final ucar.ma2.ArrayInt.D1 maximaDiff) {
        final ArrayStatsScanner ass = new ArrayStatsScanner();
        final StatsMap[] smd = ass.apply(new Array[]{maximaDiff, sat});
        final double satmax = smd[1].get(Vars.Max.toString());
        final double satmin = smd[1].get(Vars.Min.toString());
        final double diffMin = smd[0].get(Vars.Min.toString());
        final double diffMax = smd[0].get(Vars.Max.toString());
        log.info("Scans per modulation: Min {} Max {} Mean {}",
                new Object[]{smd[0].get(Vars.Min.toString()),
                    smd[0].get(Vars.Max.toString()),
                    smd[0].get(Vars.Mean.toString())});
        log.info("Estimated acquisition_time per modulation: {}", smd[0]
                .get(Vars.Max.toString())
                / satMean);
        final Index sati = sat.getIndex();
        int globalScanIndex = 0;
        double time = 0.0d;
        double globtime = 0.0d;
        final ArrayDouble.D1 sctimes = new ArrayDouble.D1(sat.getShape()[0]);
        final ArrayDouble.D1 modtimes = new ArrayDouble.D1(maximaDiff
                .getShape()[0] + 1);
        final ArrayInt.D1 modindex = new ArrayInt.D1(
                maximaDiff.getShape()[0] + 1);
        for (int i = 0; i < maximaDiff.getShape()[0] + 1; i++) {
            time = 0.0d;
            // int nscans = maximaDiff.get(i);
            final int nscans = (int) diffMax;
            modtimes.set(i, globtime);
            modindex.set(i, globalScanIndex);
            for (int j = 0; j < nscans; j++) {
                final double saTime = sat.getDouble(sati.set(globalScanIndex));
                globtime += saTime;
                time += saTime;
                sctimes.set(globalScanIndex, time);
                globalScanIndex++;
            }
        }

        final IVariableFragment sctimesV = new VariableFragment(fret,
                "modulation_scan_acquisition_time");
        sctimesV.setArray(sctimes);
        final IVariableFragment modulationtimesV = new VariableFragment(fret,
                "modulation_time");
        modulationtimesV.setArray(modtimes);
        final IVariableFragment modulationScanIndex = new VariableFragment(
                fret, "modulation_scan_index");
        modulationScanIndex.setArray(modindex);
        final IVariableFragment scansPerModulation = new VariableFragment(fret,
                "scans_per_modulation");
        final ArrayInt.D0 spm = new ArrayInt.D0();
        spm.set((int) diffMax);
        scansPerModulation.setArray(spm);
        return new Tuple2D<>(modtimes, sctimes);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }

    /**
     * <p>isCandidate.</p>
     *
     * @param prev a double.
     * @param current a double.
     * @param next a double.
     * @return a boolean.
     */
    protected boolean isCandidate(final double prev, final double current,
            final double next) {
        final boolean b = (prev < current) && (current > next);
        // log.info("Found candidate, checking additional constraints!");
        return b;
    }
}
