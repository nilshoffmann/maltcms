/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package maltcms.commands.fragments.preprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.HeatMapChart;
import maltcms.ui.charts.PlotRunner;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.ArrayInt.D1;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.EvalTools;

/**
 * ModulationTimeEstimator tries to find the spike-like peaks in GCxGC MS data
 * (Leco exported to netcdf ANDIMS), in order to estimate the second retention
 * time parameter.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ModulationTimeEstimator extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this.getClass());

	private String tic_var = "total_intensity";

	private String mass_var = "mass_values";

	private String inten_var = "intensity_values";

	private final double secondColumnTime = 5.0d;

	private final double scanRate = 200.0d;

	private final ExecutorService es = Executors.newFixedThreadPool(10);

	private double doubleFillValue;

	private double threshold;

	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();
		for (final IFileFragment ff : t) {
			final IFileFragment fret = Factory.getInstance()
			        .getFileFragmentFactory().create(
			                new File(getIWorkflow().getOutputDirectory(this),
			                        ff.getName()));

			findSecondRetentionTimes(ff, fret);
			fret.save();
			ret.add(fret);
		}
		return new TupleND<IFileFragment>(ret);
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
		return new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(fstColTime,
		        sndColTime);
	}

	protected ArrayInt.D2 buildSecondRetentionTime(final ArrayInt.D1 tic,
	        final ArrayList<Integer> maxima) {
		return null;
	}

	private ArrayList<Array> buildTIC2D(final int scansPerModulation,
	        final Array tic) {
		this.log.info("Number of tics {}", tic.getShape()[0]);
		this.log.info("Number of scans {}", scansPerModulation);
		final int size = tic.getShape()[0] / scansPerModulation;
		this.log
		        .info("Building TIC arrays with fixed number of scans per modulation");
		this.log.info("Reconstructing {} scans", size);
		final ArrayList<Array> al = new ArrayList<Array>(size);
		int offset = 0;
		final int len = scansPerModulation;
		for (int i = 0; i < size; i++) {
			this.log.info("Range for scan {}: Offset {}, Length: {}",
			        new Object[] { i, offset, len });
			try {
				if ((offset + len) < tic.getShape()[0]) {
					final Array a = tic.section(new int[] { offset },
					        new int[] { len });
					// System.out.println("Scan " + (i + 1));
					// System.out.println(a.toString());
					al.add(a);
				} else {
					this.log.warn("Omitting rest! Scan {}, offset {}, len {}",
					        new Object[] { i, offset, len });
				}
			} catch (final InvalidRangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			offset += len;
		}
		return al;
	}

	private ArrayList<Array> buildTIC2D(final int scansPerModulation,
	        final D1 msi, final Array tic) {
		final int size = msi.getShape()[0];
		final ArrayList<Array> al = new ArrayList<Array>(size);
		int offset = 0;
		final int len = scansPerModulation;
		for (int i = 0; i < size; i++) {
			offset = msi.get(i);
			this.log.info("Range for scan {}: Offset {}, Length: {}",
			        new Object[] { i, offset, len });
			try {
				if ((offset + len) < tic.getShape()[0]) {
					final Array a = tic.section(new int[] { offset },
					        new int[] { len });
					// System.out.println("Scan " + (i + 1));
					// System.out.println(a.toString());
					al.add(a);
				} else {
					this.log.warn("Omitting rest! Scan {}, offset {}, len {}",
					        new Object[] { i, offset, len });
				}
			} catch (final InvalidRangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return al;
	}

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
		this.log.debug("R'({})= {}", lag, v);
		// return v;
	}

	protected void checkDeltas(final ArrayList<Tuple2D<Integer, Double>> al) {
		Tuple2D<Integer, Double> tple = al.remove(0);
		final ArrayInt.D1 deltas = new ArrayInt.D1(al.size() - 1);
		int i = 0;
		for (final Tuple2D<Integer, Double> t : al) {
			final int d = t.getFirst() - tple.getFirst();
			this.log.info("d = {}", d);
			deltas.set(i++, d);
			tple = t;
		}
	}

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
	 * @param a
	 * @param maximaIndices
	 * @return
	 */
	protected Tuple2D<ArrayDouble.D1, ArrayInt.D1> findMaxima(
	        final ArrayDouble.D1 a, final ArrayList<Integer> maximaIndices) {
		this.log.info("Looking for maxima!");
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
				this.log.info("Current deviation {}, Maximum deviation: {}",
				        ((i - lastMax) - meanSoFar), maxDev);
				if (((i - lastMax) - meanSoFar) / meanSoFar <= maxDev) {
					this.log.info(
					        "Maximum within 5% range of mean {} at lag {}",
					        current, i);
					final int diff = i - lastExtrIdx;
					final double vdiff = current - lastExtr;
					this.log.info("Difference to last index {}, value {}",
					        diff, vdiff);
					this.log.info("Number of scans between maxima: {}",
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
					this.log.info("Mean so far: {}", meanSoFar);
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
				tic = tic.section(new int[] { 0 }, new int[] { maxIndex },
				        new int[] { 1 });
			} catch (final InvalidRangeException e) {
				this.log.warn(e.getLocalizedMessage());
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
		final StatsMap[] sma = ass.apply(new Array[] { tic, satDiff });
		final double mean = sma[0].get(cross.datastructures.Vars.Mean
		        .toString());
		final double variance = sma[0].get(cross.datastructures.Vars.Variance
		        .toString());
		final int ubound = Math.min(tic.getShape()[0] - 1,
		        tic.getShape()[0] - 1);
		final ArrayDouble.D1 acr = new ArrayDouble.D1(ubound);

		final double satMax = sma[1].get(cross.datastructures.Vars.Max
		        .toString());
		final double satMin = sma[1].get(cross.datastructures.Vars.Min
		        .toString());
		final double satMean = sma[1].get(cross.datastructures.Vars.Mean
		        .toString());
		this.log.info("scan_acquisition_time deltas: Max: {} Min: {} Mean: {}",
		        new Object[] { satMax, satMin, satMean });
		final double satMeanInv = 1.0d / satMean;
		this.log.info("Estimated number of scans per second: {}", satMeanInv);
		this.log
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
		// ArrayList<Integer> maxIndices = new ArrayList<Integer>();
		// Tuple2D<ArrayDouble.D1, ArrayInt.D1> t = findMaxima(acr,
		// maxIndices);
		// ArrayDouble.D1 maximaA = t.getFirst();
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

		// XYChart xyc = new XYChart("Autocorrelation within " +
		// ff.getName(),
		// new String[] { "Autocorrelation" }, new Array[] { acr },
		// new ArrayInt[] { domain }, "Lag", "Autocorrelation");
		// XYChart xym = new XYChart("Autocorrelation within " +
		// ff.getName(),
		// new String[] { "Maxima" }, new Array[] { maximaA },
		// new ArrayInt[] { domain }, "Lag", "Autocorrelation");
		// ArrayList<XYPlot> alp = new ArrayList<XYPlot>();
		// alp.add(xyc.create());
		// alp.add(xym.create());
		// CombinedDomainXYChart cdc = new CombinedDomainXYChart(
		// "Combined Domains", "autocorrelation", true, alp);
		//
		// PlotRunner pr = new PlotRunner(cdc.create(), "Autocorrelation
		// within "
		// + ff.getName(), ff.getName() + "_autocorrelation");
		// pr.configure(ArrayFactory.getConfiguration());
		// ArrayFactory.submitJob(pr);

		final BufferedImage bi = ImageTools.fullSpectrum(ff.getName(), tic2D,
		        (int) (this.secondColumnTime * this.scanRate), colorRamp, 1024,
		        true, this.threshold);
		ImageTools.saveImage(bi, ff.getName() + "-chromatogram", "png",
		        getIWorkflow().getOutputDirectory(this), this);
		final HeatMapChart hmc = new HeatMapChart(bi, "time 1 [s]",
		        "time 2 [s]", buildScanAcquisitionTime(
		                (int) (this.secondColumnTime * this.scanRate), tic
		                        .getShape()[0], satMean), ff.getAbsolutePath());
		final PlotRunner pl = new PlotRunner(hmc.create(), "Chromatogram of "
		        + ff.getAbsolutePath(), "chromatogram-" + ff.getName(),
		        getIWorkflow().getOutputDirectory(this));
		pl.configure(Factory.getInstance().getConfiguration());
		Factory.getInstance().submitJob(pl);
		// checkDeltas(maxima);
	}

	@Override
	public String getDescription() {
		return "Reconstructs second retention time of 2DGC-MS chromatograms from given scan rate and second column time.";
	}

	/**
	 * We need: All Tics, scan acquisition times for every scan, all maxima of
	 * the tic (modulation peaks of solvent), number of scans in between maxima.
	 * 
	 * @param fret
	 * @param satMean
	 * @param tic
	 * @param sat
	 * @param maximaA
	 * @param maximaDiff
	 * @return
	 */
	protected Tuple2D<ArrayDouble.D1, ArrayDouble.D1> getModulationTime(
	        final IFileFragment fret, final double satMean, final Array sat,
	        final ArrayDouble.D1 maximaA, final ucar.ma2.ArrayInt.D1 maximaDiff) {
		final ArrayStatsScanner ass = new ArrayStatsScanner();
		final StatsMap[] smd = ass.apply(new Array[] { maximaDiff, sat });
		final double satmax = smd[1].get(Vars.Max.toString());
		final double satmin = smd[1].get(Vars.Min.toString());
		final double diffMin = smd[0].get(Vars.Min.toString());
		final double diffMax = smd[0].get(Vars.Max.toString());
		this.log.info("Scans per modulation: Min {} Max {} Mean {}",
		        new Object[] { smd[0].get(Vars.Min.toString()),
		                smd[0].get(Vars.Max.toString()),
		                smd[0].get(Vars.Mean.toString()) });
		this.log.info("Estimated acquisition_time per modulation: {}", smd[0]
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
		return new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(modtimes, sctimes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.GENERAL_PREPROCESSING;
	}

	protected boolean isCandidate(final double prev, final double current,
	        final double next) {
		final boolean b = (prev < current) && (current > next);
		// log.info("Found candidate, checking additional constraints!");
		return b;
	}

}
