/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: SeededRegionGrowing.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.commands.fragments2d.peakfinding.bbh.BBHTools;
import maltcms.commands.fragments2d.peakfinding.bbh.IBidirectionalBestHit;
import maltcms.commands.fragments2d.peakfinding.bbh.MissingPeak2D;
import maltcms.commands.fragments2d.peakfinding.output.IPeakExporter;
import maltcms.commands.fragments2d.peakfinding.output.IPeakIdentification;
import maltcms.commands.fragments2d.peakfinding.output.IPeakIntegration;
import maltcms.commands.fragments2d.peakfinding.output.PeakNormalization;
import maltcms.commands.fragments2d.peakfinding.picking.IPeakPicking;
import maltcms.commands.fragments2d.peakfinding.srg.IRegionGrowing;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.io.csv.ColorRampReader;
import maltcms.statistics.LogDeltaEvaluation;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.BHeatMapChart;
import maltcms.ui.charts.PlotRunner;
import maltcms.ui.charts.XYBPlot;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.FragmentTools;
import cross.tools.StringTools;

/**
 * Peakpicking + integration + identification + normalization + evaluation...
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@RequiresVariables(names = { "var.total_intensity", "var.scan_rate",
		"var.modulation_time", "var.second_column_scan_index",
		"var.scan_acquisition_time_1d" })
@RequiresOptionalVariables(names = { "var.v_total_intensity" })
@ProvidesVariables(names = { "var.peak_index_list", "var.region_index_list",
		"var.region_peak_index", "var.boundary_index_list",
		"var.boundary_peak_index", "var.peak_mass_intensity" })
public class SeededRegionGrowing extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this);

	@Configurable(name = "var.total_intensity", value = "total_intensity", type=String.class)
	private String totalIntensityVar = "total_intensity";
	@Configurable(name = "var.scan_rate", value = "scan_rate", type=String.class)
	private String scanRateVar = "scan_rate";
	@Configurable(name = "var.modulation_time", value = "modulation_time", type=String.class)
	private String modulationTimeVar = "modulation_time";
	@Configurable(name = "var.second_column_scan_index", value = "second_column_scan_index", type=String.class)
	private String secondScanIndexVar = "second_column_scan_index";
	@Configurable(name = "var.peak_index_list", value = "peak_index_list", type=String.class)
	private String peakListVar = "peak_index_list";
	@Configurable(name = "var.scan_acquisition_time_1d", value = "scan_acquisition_time_1d", type=String.class)
	private String scanAcquTime1DVar = "scan_acquisition_time_1d";
	@Configurable(name = "var.second_column_time", value = "second_column_time", type=String.class)
	private final String secondColumnTimeVar = "second_column_time";

	@Configurable(name = "var.region_index_list", value = "region_index_list", type=String.class)
	private String regionIndexListVar = "region_index_list";
	@Configurable(name = "var.region_peak_index", value = "region_peak_index", type=String.class)
	private String regionPeakIndexVar = "region_peak_index";
	@Configurable(name = "var.boundary_index_list", value = "boundary_index_list", type=String.class)
	private String boundaryIndexListVar = "boundary_index_list";
	@Configurable(name = "var.boundary_peak_index", value = "boundary_peak_index", type=String.class)
	private String boundaryPeakIndexVar = "boundary_peak_index";

	@Configurable(name = "maltcms.ui.charts.PlotRunner.filetype", value = "png", type=String.class)
	private final String format = "png";
	@Configurable(name = "images.colorramp", value = "res/colorRamps/bcgyr.csv", type=String.class)
	private String colorrampLocation = "res/colorRamps/bcgyr.csv";
	@Configurable(name = "ucar.nc2.NetcdfFile.fillValueDouble", value = "9.9692099683868690e+36d", type=double.class)
	private double doubleFillValue;
	@Configurable(name = "images.thresholdLow", value = "0", type=double.class)
	private double threshold = 0;
	@Configurable(value = "true", type=String.class)
	private boolean doBBH = true;
	@Configurable(value = "maltcms.commands.fragments2d.peakfinding.picking.MaxSortPeakPicking", type=IPeakPicking.class)
	private String pickingClass = "maltcms.commands.fragments2d.peakfinding.picking.MaxSortPeakPicking";
	@Configurable(value = "maltcms.commands.fragments2d.peakfinding.srg.OneByOneRegionGrowing", type=IRegionGrowing.class)
	private String srgClass = "maltcms.commands.fragments2d.peakfinding.srg.OneByOneRegionGrowing";
	@Configurable(value = "maltcms.commands.fragments2d.peakfinding.bbh.FastBidirectionalBestHit", type=IBidirectionalBestHit.class)
	private String bbhClass = "maltcms.commands.fragments2d.peakfinding.bbh.FastBidirectionalBestHit";
	@Configurable(value = "maltcms.commands.fragments2d.peakfinding.output.PeakIdentification", type=IPeakIdentification.class)
	private String identificationClass = "maltcms.commands.fragments2d.peakfinding.output.PeakIdentification";
	@Configurable(value = "maltcms.commands.fragments2d.peakfinding.output.PeakIntegration", type=IPeakIntegration.class)
	private String integrationClass = "maltcms.commands.fragments2d.peakfinding.output.PeakIntegration";
	@Configurable(value = "maltcms.commands.fragments2d.peakfinding.output.PeakExporter", type=IPeakExporter.class)
	private String exportClass = "maltcms.commands.fragments2d.peakfinding.output.PeakExporter";
	@Configurable(value = "maltcms.commands.distances.ArrayCos", type=IArrayDoubleComp.class)
	private String distClass = "maltcms.commands.distances.ArrayCos";
	@Configurable(value = "true", type=boolean.class)
	private boolean separate = true;
	@Configurable(value = "true", type=boolean.class)
	private boolean secondRun = true;
	@Configurable(value = "false", type=boolean.class)
	private boolean doNormalization = false;
	@Configurable(value = "false", type=boolean.class)
	private boolean doIntegration = false;
	@Configurable(value = "0.999d", type=double.class)
	private double separationDistance = 0.999d;
	@Configurable(value = "0.99d", type=double.class)
	private double secondSeedMinDistance = 0.99d;

	private boolean seedNotInRegion = false;

	private int scansPerModulation = 0;
//	private int scanLineCount = 0;

	private IArrayDoubleComp distance = null;
	private IPeakPicking peakPicking;
	private IRegionGrowing regionGrowing;
	private IBidirectionalBestHit bbh;
	private IPeakIdentification identification;
	private IPeakIntegration integration;
	private IPeakExporter peakExporter;
	private PeakSeparator ps = new PeakSeparator();

	private List<List<Peak2D>> peakLists = new ArrayList<List<Peak2D>>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		final ColorRampReader crr = new ColorRampReader();
		final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);
		final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();

		this.peakExporter.setWorkflow(getWorkflow());
		this.peakExporter.setCaller(this.getClass());

		// running SRG the first time
		List<Peak2D> peaklist = null;
		for (final IFileFragment ff : t) {
			peaklist = runSRG(ff, null);
			Collections.sort(peaklist, new PeakComparator());
			for (int j = 0; j < peaklist.size(); j++) {
				peaklist.get(j).setIndex(j);
			}
			this.peakLists.add(peaklist);
		}

		// finding BBHs
		List<List<Point>> bidiBestHitList = null;
		if (t.size() > 1 && this.doBBH) {
			bidiBestHitList = bbh.getBidiBestHitList(this.peakLists);
			this.bbh.clear();
		}

		// second run
		if (bidiBestHitList != null && this.secondRun) {
			bidiBestHitList = startSecondRun(bidiBestHitList, t);
		}

                for (int i = 0; i < t.size(); i++) {
                    addAdditionalInformation(this.peakLists.get(i), t.get(i));
                }
		this.log.info("Saving all Peaks");
		// exporting bbh information + doing normalization + statistical
		// evaluation
		if (t.size() > 1 && this.doBBH) {
			BBHTools.exportBBHInformation(bidiBestHitList, this.peakLists,
					this.bbh, this.peakExporter, getNamesFor(t));

			if (this.doNormalization) {
				PeakNormalization pn = new PeakNormalization();
				pn.normalize(this.peakLists, bidiBestHitList, t);
			}

			final LogDeltaEvaluation lde = new LogDeltaEvaluation();
			lde.setWorkflow(getWorkflow());
			lde.calcRatios(BBHTools.getPeak2DCliqueList(t, bidiBestHitList,
					this.peakLists), t);
		}
		// exporting peak lists
		for (int i = 0; i < t.size(); i++) {
			final IFileFragment fret = Factory.getInstance()
					.getFileFragmentFactory().create(
							new File(getWorkflow().getOutputDirectory(this), t
									.get(i).getName()));
			fret.addSourceFile(t.get(i));
			savePeaks(t.get(i), fret, this.peakLists.get(i), colorRamp);

			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
					new File(fret.getAbsolutePath()), this, getWorkflowSlot(),
					t.get(i));
			getWorkflow().append(dwr);
			fret.save();
			ret.add(fret);
		}

		return new TupleND<IFileFragment>(ret);
	}

	/**
	 * Returns the truncates names of the input files
	 * 
	 * @param t
	 *            list of input file fragments
	 * @return list of strings
	 */
	private List<String> getNamesFor(final TupleND<IFileFragment> t) {
		final List<String> chromatogramNames = new ArrayList<String>();
		for (final IFileFragment ff : t) {
			chromatogramNames.add(StringTools.removeFileExt(ff.getName()));
		}
		return chromatogramNames;
	}

	/**
	 * Will run the seeded region growing either on the given input seeds or
	 * uses the {@link IPeakPicking} class to find seeds. After extending the
	 * region towards its maximum, the {@link PeakSeparator} will try to merge
	 * or separate the resulting {@link PeakArea2D}s.
	 * 
	 * @param ff
	 *            file fragement to generate the {@link IScanLine}
	 * @param seeds
	 *            initial seeds. If this parameter is <code>null</code> then the
	 *            {@link IPeakPicking} class will be used to determine seeds.
	 * @return List of resulting peaks
	 */
	private List<Peak2D> runSRG(IFileFragment ff, List<Point> seeds) {
		final int scanRate = ff.getChild(this.scanRateVar).getArray().getInt(
				Index.scalarIndexImmutable);
		final int modulationTime = ff.getChild(this.modulationTimeVar)
				.getArray().getInt(Index.scalarIndexImmutable);
		this.scansPerModulation = scanRate * modulationTime;

		if (seeds == null) {
			this.log.info("== starting peak finding for " + ff.getName());
			seeds = this.peakPicking.findPeaks(ff);
		} else {
			this.log.info("== restarting peak finding for " + ff.getName());
		}
		this.log.info("	Found {} potential peaks in {}", seeds.size(), ff
				.getName());

		this.log.info("Computing areas");
		final IScanLine slc = ScanLineCacheFactory.getDefaultScanLineCache(ff);
//		this.scanLineCount = slc.getScanLineCount();
		long start = System.currentTimeMillis();
		final List<PeakArea2D> peakAreaList = this.regionGrowing.getAreasFor(
				seeds, ff, slc);

		this.log.info("Integration take {} ms", System.currentTimeMillis()
				- start);

		if (this.separate) {
                    //Also, maybe better to use time penalized dist class! with a smaller window?
			this.ps.startSeparationFor(peakAreaList, this.distance, slc,
					this.regionGrowing.getIntensities());
		}

		slc.clear();

		final List<Peak2D> peaklist = createPeaklist(peakAreaList,
				getRetentiontime(ff), ff);
		return peaklist;
	}

	/**
	 * This methode tries to find missing peaks in all chromatograms. It uses
	 * internally the private variable peakLists. TODO: More
	 * 
	 * @param bidiBestHitList
	 *            bidirection best hit list
	 * @param t
	 *            file fragment
	 * @return altered bidirection best hit list if some new peaks were found,
	 *         or the same list als bidiBestHitList
	 */
	private List<List<Point>> startSecondRun(List<List<Point>> bidiBestHitList,
			final TupleND<IFileFragment> t) {
		boolean change = false;
		this.log.info("Checking for missing peaks");
		final List<MissingPeak2D> missingPeaks = BBHTools.getMissingPeaks(
				bidiBestHitList, this.peakLists, this.scansPerModulation);
		List<Point> seeds;
		List<Point> tmpSeeds;
		IScanLine slc;
		double score;
		for (int i = 0; i < t.size(); i++) {
			slc = ScanLineCacheFactory.getDefaultScanLineCache(t.get(i));
			seeds = new ArrayList<Point>();
			int maxArg;
			double max = 0;
			for (MissingPeak2D mp : missingPeaks) {
				if (mp.getMissingChromatogramList().contains(i)) {
					tmpSeeds = this.peakPicking.findPeaksNear(t.get(i), mp
							.getMeanPoint(), mp.getMaxFirstDelta(), mp
							.getMaxSecondDelta());
					// check weather point is in a region of an
					// existing peak or an seed point of an region
					List<Point> remove = new ArrayList<Point>();
					for (Point p : tmpSeeds) {
						// for (PeakArea2D pa : this.peakAreaLists.get(i)) {
						for (Peak2D peak : this.peakLists.get(i)) {
							if (this.seedNotInRegion
									&& peak.getPeakArea().regionContains(p)) {
								remove.add(p);
							}
							if (tmpSeeds.contains(peak.getPeakArea()
									.getSeedPoint())
									&& !remove.contains(p)) {
								remove.add(p);
							}
						}
					}
					for (Point p : remove) {
						tmpSeeds.remove(p);
					}

					int c = 0;
					max = 0;
					maxArg = -1;
					for (Point p : tmpSeeds) {
						score = this.distance.apply(0, 0, mp
								.getMeanFirstScanIndex()
								- p.x, mp.getMeanSecondScanIndex() - p.y, mp
								.getMeanMS(), slc.getMassSpectra(p));
						// this.log.info("	s: {}", score);
						if (score > max) {
							max = score;
							maxArg = c;
						}
						c++;
					}
					if (maxArg != -1 && max > this.secondSeedMinDistance) {
						// if (maxArg != -1) {
						seeds.add(tmpSeeds.get(maxArg));
						// this.log.info("Adding one with ms sim {}", max);
					}
				}
			}

			List<Peak2D> peaklist = null;
			if (seeds.size() > 0) {
				this.log
						.info("Found {} potential peaks in total", seeds.size());
				peaklist = runSRG(t.get(i), seeds);
				this.peakLists.get(i).addAll(peaklist);
				Collections.sort(this.peakLists.get(i), new PeakComparator());
				for (int j = 0; j < this.peakLists.get(i).size(); j++) {
					this.peakLists.get(i).get(j).setIndex(j);
				}

				change = true;
			} else {
				this.log.info("Nohing found in evaluation of BBHs");
			}
			slc.clear();
		}
		if (change) {
			this.log.info("Setting up BBH");
			this.bbh.clear();
			// for (List<Peak2D> pl : this.peakLists) {
			// this.bbh.addPeakLists(pl);
			// }
			this.log.info("Recomputing BBHs");
			return this.bbh.getBidiBestHitList(this.peakLists);
		} else {
			this.log.info("All in all nothing found. Will not do BBHs again.");
		}

		return bidiBestHitList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.totalIntensityVar = cfg.getString(this.getClass().getName()
				+ ".totalIntensityVar", "total_intensity");
		this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
		this.modulationTimeVar = cfg.getString("var.modulation_time",
				"modulation_time");
		this.secondScanIndexVar = cfg.getString("var.second_column_scan_index",
				"second_column_scan_index");
		this.scanAcquTime1DVar = cfg.getString("var.scan_acquisition_time_1d",
				"scan_acquisition_time_1d");
		this.peakListVar = cfg.getString("var.peak_index_list",
				"peak_index_list");
		this.boundaryIndexListVar = cfg.getString("var.boundary_index_list",
				"boundary_index_list");
		this.boundaryPeakIndexVar = cfg.getString("var.boundary_peak_list",
				"boundary_peak_list");
		this.regionIndexListVar = cfg.getString("var.region_index_list",
				"region_index_list");
		this.regionPeakIndexVar = cfg.getString("var.region_peak_list",
				"region_peak_list");
		this.colorrampLocation = cfg.getString("images.colorramp",
				"res/colorRamps/bcgyr.csv");
		this.doubleFillValue = cfg.getDouble(
				"ucar.nc2.NetcdfFile.fillValueDouble", 9.9692099683868690e+36);
		this.threshold = cfg.getDouble("images.thresholdLow", 0.0d);

                // TODO: This dist class is used to determine second seeds. Maybe it is better to use a time penalized dist class here?
                // But if so, you have to change scan index difference to time difference in line 372
		this.distClass = cfg.getString(
				this.getClass().getName() + ".distClass",
				"maltcms.commands.distances.ArrayCos");
		this.distance = Factory.getInstance().getObjectFactory().instantiate(
				this.distClass, IArrayDoubleComp.class);
		this.distance.configure(cfg);

		final String bbhClass = cfg
				.getString(this.getClass().getName() + ".bbhClass",
						"maltcms.commands.fragments2d.peakfinding.bbh.FastBidirectionalBestHit");
		this.bbh = Factory.getInstance().getObjectFactory().instantiate(
				bbhClass, IBidirectionalBestHit.class);
		this.bbh.configure(cfg);
		this.doBBH = cfg.getBoolean(this.getClass().getName() + ".doBBH", true);

		final String peakExporterClass = cfg.getString(this.getClass()
				.getName()
				+ ".exportClass",
				"maltcms.commands.fragments2d.peakfinding.output.PeakExporter");
		this.peakExporter = Factory.getInstance().getObjectFactory()
				.instantiate(peakExporterClass, IPeakExporter.class, cfg);

                this.doIntegration = cfg.getBoolean(this.getClass().getName() + ".doIntegration", false);
		final String peakIntegrationClass = cfg
				.getString(this.getClass().getName() + ".integrationClass",
						"maltcms.commands.fragments2d.peakfinding.output.PeakIntegration");
		this.integration = Factory.getInstance().getObjectFactory()
				.instantiate(peakIntegrationClass, IPeakIntegration.class, cfg);

		final String identificationClass = cfg
				.getString(this.getClass().getName()
						+ ".indentificationClass",
						"maltcms.commands.fragments2d.peakfinding.output.PeakIdentification");
		this.identification = Factory.getInstance().getObjectFactory()
				.instantiate(identificationClass, IPeakIdentification.class,
						cfg);

		String ppClass = cfg
				.getString(this.getClass().getName() + ".pickingClass",
						"maltcms.commands.fragments2d.peakfinding.picking.SimplePeakPicking");
		this.peakPicking = Factory.getInstance().getObjectFactory()
				.instantiate(ppClass, IPeakPicking.class, cfg);

		String regionGrowingClass = cfg
				.getString(this.getClass().getName() + ".srgClass",
						"maltcms.commands.fragments2d.peakfinding.srg.OneByOneRegionGrowing");
		this.regionGrowing = Factory.getInstance().getObjectFactory()
				.instantiate(regionGrowingClass, IRegionGrowing.class, cfg);
		// this.srgMinDist = this.regionGrowing.getMinDist();
		this.separationDistance = cfg.getDouble(this.getClass().getName()
				+ ".separationDistance", 0.999d);
		this.ps.setMinDist(this.separationDistance);
		this.separate = cfg.getBoolean(this.getClass().getName() + ".separate",
				true);
		this.secondSeedMinDistance = cfg.getDouble(this.getClass().getName()
				+ ".secondSeedMinDistance", 0.99d);
		this.secondRun = cfg.getBoolean(this.getClass().getName()
				+ ".secondRun", true);

		this.doNormalization = cfg.getBoolean(this.getClass().getName()
				+ ".doNormalization", false);
	}

	/**
	 * Save the given {@link BufferedImage} and serialize it.
	 * 
	 * @param image
	 *            image
	 * @param name
	 *            name
	 * @param title
	 *            title of the plot
	 * @param peakList
	 *            peaks
	 * @param times
	 *            first and second retention time
	 */
	private void createAndSaveImage(final BufferedImage image,
			final String name, final String title, final List<Peak2D> peakList,
			final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times) {
		ImageTools.saveImage(image, name, this.format, getWorkflow()
				.getOutputDirectory(this), this);
		// ImageTools.saveImage(image, name + "_emtpy", this.format,
		// getWorkflow().getOutputDirectory(this), this);

		final File d = getWorkflow().getOutputDirectory(this);
		final File out = new File(d, name + "." + this.format);
		this.log.info("Using file {} for AChart", out.getAbsolutePath());
		final AChart<XYBPlot> chart = new BHeatMapChart(out.getAbsolutePath(),
				"first retention time[min]", "second retention time[s]", times,
				name);
		final XYPlot plot = chart.create();
		for (final Peak2D p : peakList) {
			final XYPointerAnnotation pointer = new XYPointerAnnotation(
					// p.getName() + "(" +
					p.getIndex() + ""
					// + ")"
					, p.getFirstRetTime(), p.getSecondRetTime(),
					7 * Math.PI / 4.0d);
			pointer.setTipRadius(0.0d);
			pointer.setArrowLength(0.0d);
			pointer.setBaseRadius(0.0d);
			pointer.setPaint(Color.WHITE);
			plot.addAnnotation(pointer);
		}
		final PlotRunner pl = new PlotRunner(plot, title, name + "_plot", d);
		pl.configure(Factory.getInstance().getConfiguration());
		Factory.getInstance().submitJob(pl);
	}

	/**
	 * Creates and writes a csv containing all needed information about the
	 * peak.
	 * 
	 * @param pas
	 *            list of all snakes
	 * @param times
	 *            first and second retention time
	 * @param ff
	 *            file fragment
	 * @return ff
	 */
	private List<Peak2D> createPeaklist(final List<PeakArea2D> pas,
			final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times,
			final IFileFragment ff) {
		final List<Peak2D> peaklist = new ArrayList<Peak2D>();
		PeakArea2D s;
		Peak2D peak;
		double x, y;
		for (int i = 0; i < pas.size(); i++) {
			if (i % 10 == 0) {
				this.log.info("	Did " + i);
			}
			s = pas.get(i);
			peak = new Peak2D();
			x = times.getFirst().get(s.getSeedPoint().x);
			y = times.getSecond().get(s.getSeedPoint().y);
			peak.setPeakArea(s);
			peak.setFirstRetTime(x);
			peak.setSecondRetTime(y);
			peak.setScanIndex(s.getIndex());

			peaklist.add(peak);
		}

		return peaklist;
	}

	/**
	 * Adds additional information to the peaklist ps such as integration on
	 * special mzs and identification by db search.
	 * 
	 * @param ps
	 * @param ff
	 * @return
	 */
	private List<Peak2D> addAdditionalInformation(final List<Peak2D> ps,
			final IFileFragment ff) {

		final List<Array> tic = getIntensities(ff);
		Peak2D peak;
		this.log.info("Adding additional Information");
		for (int i = 0; i < ps.size(); i++) {
			if (i % 10 == 0) {
				this.log.info("	Did " + i);
			}
			peak = ps.get(i);
			if (this.identification != null) {
				this.identification.setName(peak);
			}
			if (this.doIntegration) {
				this.integration.integrate(peak, ff, tic, getWorkflow());
			}
		}

		return ps;
	}

	/**
	 * Index map.
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return index
	 */
	private int idx(final int x, final int y) {
		return x * this.scansPerModulation + y;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Will do an initial peak finding and computes the 'snakes'";
	}

	/**
	 * Getter.
	 * 
	 * @param ff
	 *            file fragment
	 * @return first and second retentiontime
	 */
	private Tuple2D<ArrayDouble.D1, ArrayDouble.D1> getRetentiontime(
			final IFileFragment ff) {
		// this.log.info("Reading retention time arrays");
		// final ArrayDouble.D1 firstRetTime = (ArrayDouble.D1) ArrayTools
		// .divBy60(ff.getChild(this.scanAcquTime1DVar).getArray());
		final ArrayDouble.D1 firstRetTime = (ArrayDouble.D1) ff.getChild(
				this.scanAcquTime1DVar).getArray();
		ff.getChild(this.secondColumnTimeVar).setIndex(
				ff.getChild(this.secondScanIndexVar));
		final ArrayDouble.D1 secondRetTime = (ArrayDouble.D1) ff.getChild(
				this.secondColumnTimeVar).getIndexedArray().get(0);
		final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times = new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(
				firstRetTime, secondRetTime);
		return times;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.PEAKFINDING;
	}

	/**
	 * ATTENTION: NOT FIXED YET. METHOD WILL NOT DO WHAT IT SHOULD
	 * 
	 * @param ps
	 * @param ff
	 */
	@SuppressWarnings("unused")
	private void saveFragment(final List<Peak2D> ps, final IFileFragment ff) {
		final Vector<Integer> region = new Vector<Integer>();
		final Vector<Integer> boundary = new Vector<Integer>();
		final Vector<Integer> regionIndex = new Vector<Integer>();
		final Vector<Integer> boundaryIndex = new Vector<Integer>();
		final Collection<String> peakNames = new ArrayList<String>();

		Peak2D peak;

		for (int i = 0; i < ps.size(); i++) {
			peak = ps.get(i);

			regionIndex.add(region.size());
			for (final Point p : peak.getPeakArea().getRegionPoints()) {
				region.add(idx(p.x, p.y));
			}
			boundaryIndex.add(boundary.size());
			for (final Point p : peak.getPeakArea().getBoundaryPointsCopy()) {
				boundary.add(idx(p.x, p.y));
			}
			peakNames.add(peak.getName());
		}

		final IVariableFragment regionVar = new VariableFragment(ff,
				this.regionIndexListVar);
		regionVar.setArray(ArrayTools2.createIntegerArray(region));
		final IVariableFragment regionIndexVar = new VariableFragment(ff,
				this.regionPeakIndexVar);
		regionIndexVar.setArray(ArrayTools2.createIntegerArray(regionIndex));
		final IVariableFragment boundaryVar = new VariableFragment(ff,
				this.boundaryIndexListVar);
		boundaryVar.setArray(ArrayTools2.createIntegerArray(boundary));
		final IVariableFragment boundaryIndexVar = new VariableFragment(ff,
				this.boundaryPeakIndexVar);
		boundaryIndexVar
				.setArray(ArrayTools2.createIntegerArray(boundaryIndex));
		FragmentTools.createStringArray(ff, "peak_names", peakNames);
	}

	/**
	 * Saves all information about peaks and the peakarea.
	 * 
	 * @param ff
	 *            file fragment
	 * @param fret
	 *            returning file fragment
	 * @param peakAreaList
	 *            peak area list
	 * @param colorRamp
	 *            color ramp
	 * @return peak list
	 */
	private List<Peak2D> savePeaks(final IFileFragment ff,
			final IFileFragment fret, final List<Peak2D> peaklist,
			final int[][] colorRamp) {
		this.log.info("Saving areas");
		final ArrayInt.D1 peakindex = new ArrayInt.D1(peaklist.size());
		final IndexIterator iter = peakindex.getIndexIterator();
		for (final Peak2D pa : peaklist) {
			iter.setIntNext(idx(pa.getPeakArea().getSeedPoint().x, pa
					.getPeakArea().getSeedPoint().y));
		}

		final IVariableFragment var = new VariableFragment(fret,
				this.peakListVar);
		var.setArray(peakindex);

		this.log.info("Saving peaks");
		this.peakExporter.exportPeakInformation(StringTools.removeFileExt(ff
				.getName()), peaklist);
                this.peakExporter.exportPeakNames(peaklist, StringTools.removeFileExt(ff
				.getName()));
                if (this.doIntegration) {
                    this.peakExporter.exportDetailedPeakInformation(StringTools
				.removeFileExt(ff.getName()), peaklist);
                }
		// TupleND<IFileFragment> iff = getWorkflow().getCommandSequence()
		// .getInput();
		// IFileFragment originalFile = null;
		// for (IFileFragment f : iff) {
		// if (StringTools.removeFileExt(f.getName()).equals(
		// StringTools.removeFileExt(ff.getName()))) {
		// originalFile = f;
		// }
		// }
		// IFileFragment pfrag = Factory.getInstance().getFileFragmentFactory()
		// .create(getWorkflow().getOutputDirectory(this),
		// "osc-" + originalFile.getName(), originalFile);
		// IVariableFragment scanRateV = new VariableFragment(pfrag,
		// "scan_rate");
		// IVariableFragment modTV = new VariableFragment(pfrag,
		// "modulation_time");
		// scanRateV.setArray(ff.getChild("scan_rate").getArray());
		// modTV.setArray(ff.getChild("modulation_time").getArray());
		// pfrag.save();
		IScanLine isl = ScanLineCacheFactory.getScanLineCache(ff);
		this.peakExporter.exportPeaksToMSP(StringTools.removeFileExt(ff
				.getName())
				+ "-peaks.msp", peaklist, isl);
		isl.clear();
		createImage(ff, peaklist, colorRamp, getRetentiontime(ff), peaklist);
		return peaklist;
	}

	private List<Array> getIntensities(IFileFragment ff) {
		ff.getChild(this.totalIntensityVar).setIndex(
				ff.getChild(this.secondScanIndexVar));
		return ff.getChild(this.totalIntensityVar).getIndexedArray();
	}

	private void createImage(final IFileFragment ff,
			final List<Peak2D> peakList, final int[][] colorRamp,
			final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times,
			final List<Peak2D> peaklist) {

		List<Array> intensities = getIntensities(ff);
		// FIXME: should not be static!
		// intensities = intensities.subList(0, intensities.size() - 2);
		final BufferedImage biBoundary = ImageTools.create2DImage(ff.getName(),
				intensities, this.scansPerModulation, this.doubleFillValue,
				this.threshold, colorRamp, this.getClass());
		// this.log.info("PEAK AREA SIZE: {}", peakAreaList.size());
		// createAndSaveImage(ImageTools.addPeakAreaToImage(biBoundary,
		// peakAreaList, new int[] { 0, 0, 0, 255 }, null, null,
		// this.scansPerModulation), StringTools.removeFileExt(ff
		// .getName())
		// + "_seeds", "chromatogram with peak seeds", peaklist, times);
		createAndSaveImage(ImageTools.addPeakToImage(biBoundary, peakList,
				new int[] { 255, 255, 255, 255 }, null, new int[] { 0, 0, 0,
						255, }, this.scansPerModulation), StringTools
				.removeFileExt(ff.getName())
				+ "_boundary", "chromatogram with peak boundaries", peaklist,
				times);

		// Array[] values = null;
		// values = new Array[intensities.size()];
		// int cnt = 0;
		// for (Array a : intensities) {
		// values[cnt] = a;
		// cnt++;
		// }
		//
		// // Array tic = cross.tools.ArrayTools.glue(intensities);
		//
		// ArrayDouble.D2 tic2d = create2DArray(scanLineCount,
		// scansPerModulation,
		// values);
		// RenderedImage bi = ImageTools.makeImage2D(tic2d, 256,
		// Double.NEGATIVE_INFINITY);
		// bi = ImageTools.addPeakToImage(bi, peakList, new int[] { 255, 255,
		// 255,
		// 255 }, null, new int[] { 0, 0, 0, 255, },
		// this.scansPerModulation);
		// ImageTools.saveImage(ImageTools.flipVertical(bi), ff.getName()
		// + "-TIC2D", "png", getWorkflow().getOutputDirectory(this),
		// this);

	}

	// private ArrayDouble.D2 create2DArray(final int scanLineCount,
	// final int scansPerModulation, Array[] totalIntensity) {
	// this.log.debug("Creating 2d array with {}x{} elements", scanLineCount,
	// scansPerModulation);
	// ArrayDouble.D2 tic2d = new ArrayDouble.D2(scanLineCount,
	// scansPerModulation);
	// for (int x = 0; x < scanLineCount; x++) {
	// Array arr = totalIntensity[x];
	// Index arrIdx = arr.getIndex();
	// for (int y = 0; y < scansPerModulation; y++) {
	// final double currentHeight = arr.getDouble(arrIdx.set(y));
	// tic2d.set(x, y, currentHeight);
	// }
	// }
	// return tic2d;
	// }

	public class PeakComparator implements Comparator<Peak2D> {
		@Override
		public int compare(Peak2D o1, Peak2D o2) {
			Point p1 = o1.getPeakArea().getSeedPoint();
			Point p2 = o2.getPeakArea().getSeedPoint();
			if (Double.compare(p1.x, p2.x) == 0) {
				return Double.compare(p1.y, p2.y);
			} else {
				return Double.compare(p1.x, p2.x);
			}
		}
	}

	public class PeakAreaComparator implements Comparator<PeakArea2D> {
		@Override
		public int compare(PeakArea2D o1, PeakArea2D o2) {
			Point p1 = o1.getSeedPoint();
			Point p2 = o2.getSeedPoint();
			if (Double.compare(p1.x, p2.x) == 0) {
				return Double.compare(p1.y, p2.y);
			} else {
				return Double.compare(p1.x, p2.x);
			}
		}
	}

}
