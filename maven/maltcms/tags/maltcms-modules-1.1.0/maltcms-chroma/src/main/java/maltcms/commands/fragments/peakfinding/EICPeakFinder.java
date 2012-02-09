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
package maltcms.commands.fragments.peakfinding;

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import org.apache.commons.configuration.Configuration;


import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.peakfinding.TICPeakFinder.PeakPositionsResultSet;
import maltcms.datastructures.peak.Peak1D;

/**
 * Work in progress. EIC peak finder, EIC are individual ion channels.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
@Data
public class EICPeakFinder extends AFragmentCommand {

    protected static double calcEstimatedCrossCorrelation(final Array a,
            final Array b, final double meana, final double meanb,
            final double variancea, final double varianceb) {
        EvalTools.eqI(a.getShape()[0], b.getShape()[0], EICPeakFinder.class);
        double res = 0.0d;
        final int n = a.getShape()[0];
        final Index inda = a.getIndex();
        final Index indb = b.getIndex();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                res += (a.getDouble(inda.set(i)) - meana)
                        * (b.getDouble(indb.set(j)) - meanb)
                        / Math.sqrt(variancea * varianceb);
            }
        }
        final double v = res / (n - 1.0d);
        return v;
        // log.debug("R'({})= {}", lag, v);
        // return v;
    }
    @Configurable(value = "0.01d")
    private double peakThreshold = 0.01d;
    @Configurable(value = "20")
    private int filterWindow = 20;

    @Override
    public String toString() {
        return getClass().getName();
    }

    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        EvalTools.notNull(t, this);
        for (final IFileFragment f : t) {
            final ExecutorService es = Executors.newFixedThreadPool(10);
            final ArrayList<Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>>> solvers = new ArrayList<Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>>>();
            final Tuple2D<List<Array>, List<Array>> mzis = MaltcmsTools.
                    getBinnedMZIs(f);
            log.info("Retrieving data from {}", f.getAbsolutePath());
            final List<ArrayDouble.D1> al = ArrayTools.tiltD1(ArrayTools.
                    convertArrays(mzis.getSecond()));
            List<Double> bins = new ArrayList<Double>(al.size());
            Array masses = mzis.getFirst().get(0);
            for (int i = 0; i < masses.getShape()[0]; i++) {
                bins.add(masses.getDouble(i));
            }
            final ArrayStatsScanner ass = new ArrayStatsScanner();
            final StatsMap[] sm = ass.apply(al.toArray(new ArrayDouble.D1[]{}));

            TICPeakFinder tpf = new TICPeakFinder();
            tpf.setPeakThreshold(peakThreshold);
            tpf.setSnrWindow(filterWindow);
            tpf.setWorkflow(getWorkflow());
            List<List<Peak1D>> peaks = new ArrayList<List<Peak1D>>();
            int k = 0;
            for (ArrayDouble.D1 arr : al) {
                PeakPositionsResultSet pprs = tpf.findPeakPositions(arr);
//                List<Peak1D> l = tpf.findPeakAreas(pprs.getTs(),f.getName(), 0, arr, f.getChild("scan_acquisition_time").getArray());
//                for(Peak1D p1:l) {
//                    p1.setMw(bins.get(k));
//                }
//                peaks.add(l);
                k++;
            }
//			final CSVWriter csvw = new CSVWriter();
//			csvw.setWorkflow(getWorkflow());
//			csvw.writeOneFilePerArray(getWorkflow().getOutputDirectory(this)
//			        .getAbsolutePath(), StringTools.removeFileExt(f.getName())
//			        + "_eic.csv", ArrayTools.generalizeList(al));
//			log.info("ArrayStatsScanner returned {} StatsMaps", sm.length);
//            final ArrayCos ac = new ArrayCos();
            final ArrayDouble.D2 corrs = new ArrayDouble.D2(al.size(), al.size());
            // ArrayDouble.D2 crossCorrs = new
            // ArrayDouble.D2(al.size(),al.size());
            // for(int i=0; i<al.size();i++) {
            // log.info("Row {}",i);
            // for(int j = 0; j<al.size();j++) {
            // //log.info("Column {}",j);
            // ArrayDouble.D1 a = al.get(i);
            // ArrayDouble.D1 b = al.get(j);
            // StatsMap sma = sm[i];
            // EvalTools.notNull(sma, this);
            // double meana = sma.get(Vars.Mean.toString());
            // double vara = sma.get(Vars.Variance.toString());
            // StatsMap smb = sm[j];
            // EvalTools.notNull(smb, this);
            // double meanb = smb.get(Vars.Mean.toString());
            // double varb = smb.get(Vars.Variance.toString());
            // //double v =
            // calcEstimatedCrossCorrelation(a,b,meana,meanb,vara,varb);
            // solvers.add(createCallable(i, j,a, b, meana, meanb, vara, varb));
            //
            // }
            // }
            //
            // try {
            // calcCrossCorr(es, solvers, crossCorrs);
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch (ExecutionException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            for (int i = 0; i < al.size(); i++) {
                log.info("Row {}", i);
                for (int j = 0; j < al.size(); j++) {
                    corrs.set(i, j, getMaxAutocorrelation(al.get(i), al.get(j)).
                            getSecond().doubleValue());// calcEstimatedCrossCorrelation(al.get(i),
                    // al
                    // .get(j), 0, 0, 1, 1));
                    // ac
                    // .apply(0, 0, -1, -1, al.get(i), al.get(j)));
                }
            }
            final IFileFragment iff = Factory.getInstance().
                    getFileFragmentFactory().create(
                    new File(getWorkflow().getOutputDirectory(this),
                    StringTools.removeFileExt(f.getName())
                    + "-eic_correlation.cdf"));

            final IVariableFragment ivf = FragmentTools.createVariable(iff,
                    "eic-correlation", null);
            ivf.setArray(corrs);
            final ArrayList<Integer> heights = new ArrayList<Integer>();
            final ArrayList<String> eics = new ArrayList<String>();
            for (int i = 0; i < al.size(); i++) {
                heights.add(Integer.valueOf(20));
                eics.add("EIC " + i);
            }
            final RenderedImage bi = ImageTools.makeImage2D(corrs, 1024,
                    Double.POSITIVE_INFINITY);
            ImageTools.saveImage(bi, "eic_correlation_"
                    + StringTools.removeFileExt(iff.getName()), "png",
                    getWorkflow().getOutputDirectory(this), this);
            // IVariableFragment ivf2 = FragmentTools.createVariable(iff,
            // "eic-crosscorrelation", null);
            // ivf2.setArray(crossCorrs);
            iff.save();
            final DefaultWorkflowResult fileMapRes = new DefaultWorkflowResult(
                    new File(iff.getAbsolutePath()), this,
                    WorkflowSlot.PEAKFINDING, iff);
            getWorkflow().append(fileMapRes);
        }
        EvalTools.notNull(t, this);
        return t;
    }

    private void calcCrossCorr(
            final Executor e,
            final Collection<Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>>> solvers,
            final ArrayDouble.D2 a2d) throws InterruptedException,
            ExecutionException {
        final CompletionService<Tuple2D<Tuple2D<Integer, Integer>, Double>> ecs = new ExecutorCompletionService<Tuple2D<Tuple2D<Integer, Integer>, Double>>(
                e);
        for (final Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>> s : solvers) {
            ecs.submit(s);
        }
        final int n = solvers.size();
        for (int i = 0; i < n; i++) {
            final Tuple2D<Tuple2D<Integer, Integer>, Double> r = ecs.take().get();
            if (r != null) {
                final Integer i1 = r.getFirst().getFirst();
                final Integer i2 = r.getFirst().getSecond();
                a2d.set(i1, i2, r.getSecond());
                a2d.set(i2, i1, r.getSecond());
            }
        }

    }

    @Override
    public void configure(Configuration cfg) {
        this.peakThreshold = cfg.getDouble(this.getClass().getName()
                + ".peakThreshold", 1.0d);
        this.filterWindow = cfg.getInt(this.getClass().getName()
                + ".filterWindow", 10);
    }

    protected void calcEstimatedAutoCorrelation(final Array a, final Array b,
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
        final Index indb = b.getIndex();
        for (int i = 0; i < d; i++) {
            res += (a.getDouble(ind.set(i)) - mean)
                    * (b.getDouble(indb.set(i + lag)) - mean);
        }
        final double v = res / norm;
        acr.set(lag, v);
        log.debug("R'({})= {}", lag, v);
        // return v;
    }

    private Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>> createCallable(
            final int i, final int j, final ArrayDouble.D1 ad1,
            final ArrayDouble.D1 ad2, final double meana, final double meanb,
            final double vara, final double varb) {
        final Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>> c = new Callable<Tuple2D<Tuple2D<Integer, Integer>, Double>>() {

            @Override
            public Tuple2D<Tuple2D<Integer, Integer>, Double> call()
                    throws Exception {

                return new Tuple2D<Tuple2D<Integer, Integer>, Double>(
                        new Tuple2D<Integer, Integer>(i, j), EICPeakFinder.
                        calcEstimatedCrossCorrelation(ad1, ad2, meana,
                        meanb, vara, varb));

            }
        };
        return c;
    }

    @Override
    public String getDescription() {
        return "Finds peaks within on mass channels.";
    }

    protected Tuple2D<Integer, Double> getMaxAutocorrelation(final Array a,
            final Array b) {
        final Index ia = a.getIndex();
        final Index ib = b.getIndex();
        final ArrayDouble.D1 autoCorr = new ArrayDouble.D1(a.getShape()[0]);
        for (int lag = 0; lag < a.getShape()[0]; lag++) {
            calcEstimatedAutoCorrelation(a, b, 0, 1, lag, autoCorr);
        }
        double max = autoCorr.get(0);
        int maxindex = 0;
        for (int i = 0; i < autoCorr.getShape()[0]; i++) {
            if (autoCorr.get(i) > max) {
                max = autoCorr.get(i);
                maxindex = i;
            }
        }
        return new Tuple2D<Integer, Double>(Integer.valueOf(maxindex), Double.
                valueOf(max));
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }
}
