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
 * $Id: SeededRegionGrowing.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import maltcms.commands.distances.IArrayDoubleComp;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.io.csv.ColorRampReader;
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
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import cross.datastructures.tools.FragmentTools;
import cross.tools.StringTools;

/**
 * Will do the snakefail.
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

	@Configurable(name = "var.total_intensity", value = "total_intensity")
	private String totalIntensityVar = "total_intensity";
	@Configurable(name = "var.scan_rate", value = "scan_rate")
	private String scanRateVar = "scan_rate";
	@Configurable(name = "var.modulation_time", value = "modulation_time")
	private String modulationTimeVar = "modulation_time";
	@Configurable(name = "var.second_column_scan_index", value = "second_column_scan_index")
	private String secondScanIndexVar = "second_column_scan_index";
	@Configurable(name = "var.peak_index_list", value = "peak_index_list")
	private String peakListVar = "peak_index_list";
	@Configurable(name = "var.scan_acquisition_time_1d", value = "scan_acquisition_time_1d")
	private String scanAcquTime1DVar = "scan_acquisition_time_1d";
	@Configurable(name = "var.second_column_time", value = "second_column_time")
	private final String secondColumnTimeVar = "second_column_time";
	@Configurable(name = "var.used_mass_values", value = "used_mass_values")
	private final String usedMassValuesVar = "used_mass_values";

	@Configurable(name = "var.region_index_list", value = "region_index_list")
	private String regionIndexListVar = "region_index_list";
	@Configurable(name = "var.region_peak_index", value = "region_peak_index")
	private String regionPeakIndexVar = "region_peak_index";
	@Configurable(name = "var.boundary_index_list", value = "boundary_index_list")
	private String boundaryIndexListVar = "boundary_index_list";
	@Configurable(name = "var.boundary_peak_index", value = "boundary_peak_index")
	private String boundaryPeakIndexVar = "boundary_peak_index";

	@Configurable(name = "maltcms.ui.charts.PlotRunner.filetype", value = "png")
	private final String format = "png";
	@Configurable(name = "images.colorramp", value = "res/colorRamps/bcgyr.csv")
	private String colorrampLocation = "res/colorRamps/bcgyr.csv";
	@Configurable(name = "ucar.nc2.NetcdfFile.fillValueDouble", value = "9.9692099683868690e+36d")
	private double doubleFillValue;
	@Configurable(name = "images.thresholdLow", value = "0")
	private double threshold = 0;
	@Configurable(value = "true")
	private boolean useMeanMS = true;
	@Configurable(value = "-1")
	private int minVerticalScanIndex = -1;
	@Configurable(value = "true")
	private boolean doBBH = true;
	@Configurable(value = "2")
	private int minPeakSize = 2;
	@Configurable(value = "1")
	private double peakFindingThreshold = 1;
	@Configurable(value = "1000")
	private int maxPeakSize = 1000;
	@Configurable(value = "false")
	private boolean filterMS = false;
	@Configurable(value = "0.99d")
	private double minDistance = 0.99d;
	@Configurable(value = "maltcms.commands.distances.ArrayCos")
	private String distClass = "maltcms.commands.distances.ArrayCos";
	@Configurable(value = "1")
	private int maxDx = 1;
	@Configurable(value = "1")
	private int maxDy = 1;

	private int scansPerModulation = 0;
	private ArrayDouble.D1 intensities;
	private List<Integer> hold;

	private long timesum;
	private int count;

	private IArrayDoubleComp distance = null;
	private IBidirectionalBestHit bbh;
	private IPeakExporter peakExporter;
	private IPeakIntegration integration;
	private IPeakIdentification identification;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		final ColorRampReader crr = new ColorRampReader();
		final int[][] colorRamp = crr.readColorRamp(this.colorrampLocation);
		final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();

		this.peakExporter.setIWorkflow(getIWorkflow());
		this.peakExporter.setCaller(this.getClass());

		for (final IFileFragment ff : t) {
			this.log.info("Setting up intensities({})", this.totalIntensityVar);
			ff.getChild(this.totalIntensityVar).setIndex(
			        ff.getChild(this.secondScanIndexVar));
			this.intensities = (ArrayDouble.D1) ff.getChild(
			        this.totalIntensityVar).getArray();

			this.log.info("Setting scan rate");
			final int scanRate = ff.getChild(this.scanRateVar).getArray()
			        .getInt(Index.scalarIndexImmutable);
			final int modulationTime = ff.getChild(this.modulationTimeVar)
			        .getArray().getInt(Index.scalarIndexImmutable);
			this.scansPerModulation = scanRate * modulationTime;
			this.log.debug("SPM: {}", this.scansPerModulation);
			final int scanLineCount = (this.intensities.getShape()[0] - 1)
			        / this.scansPerModulation;

			if (this.minVerticalScanIndex == -1) {
				this.minVerticalScanIndex = this.scansPerModulation / 4;
			}

			getFilter(ff);

			log.info("Peak finding");
			final ArrayStatsScanner ass = new ArrayStatsScanner();
			final StatsMap[] sm = ass.apply(new Array[] { this.intensities });
			final Double minCombined = sm[0].get(Vars.Mean.toString())
			        * this.peakFindingThreshold;
			final double min = sm[0].get(Vars.Min.toString());
			final double sdev = Math.sqrt(sm[0].get(Vars.Variance.toString()));
			final Double minIntensityThreshold = sm[0]
			        .get(Vars.Mean.toString())
			        + (sdev * this.peakFindingThreshold);// sm[0].get(Vars.Max.toString());
			List<Point> peaks = null;

			this.log.info("	Using {} as lower bound", minIntensityThreshold);
			peaks = findPeaks(this.maxDx, this.maxDy, minIntensityThreshold,
			        scanLineCount, this.scansPerModulation,
			        this.minVerticalScanIndex, this.intensities);
			this.log.info("	Found {} potential peaks in {}", peaks.size(), ff
			        .getName());

			this.log.info("Computing areas");
			this.log.info("	Using distance {} with minDist:{}", this.distance
			        .getClass(), this.minDistance);
			this.timesum = 0;
			this.count = 0;
			final List<PeakArea2D> peakAreaList = new ArrayList<PeakArea2D>();
			final IScanLine slc = ScanLineCacheFactory.getScanLineCache(ff);

			for (final Point seed : peaks) {
				final PeakArea2D s = regionGrowing(seed, ff, slc);
				if (s != null) {
					peakAreaList.add(s);
				}
			}

			final IFileFragment fret = Factory.getInstance()
			        .getFileFragmentFactory().create(
			                new File(getIWorkflow().getOutputDirectory(this),
			                        ff.getName()));
			fret.addSourceFile(ff);
			final List<Peak2D> peaklist = savePeaks(ff, fret, peakAreaList,
			        colorRamp);
			VariableFragment tic = new VariableFragment(fret,
			        this.totalIntensityVar);
			tic.setArray(this.intensities);

			this.bbh.addPeakLists(peaklist);

			slc.showStat();

			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
			        new File(fret.getAbsolutePath()), this, getWorkflowSlot(),
			        fret);
			ff.clearArrays();
			getIWorkflow().append(dwr);
			fret.save();
			ret.add(fret);
			slc.clear();
		}

		if ((t.size() > 1) && this.doBBH) {
			final List<List<Point>> bidiBestHitList = this.bbh
			        .getBidiBestHitList();
			final List<String> names = new ArrayList<String>();
			for (final IFileFragment ff : t) {
				names.add(StringTools.removeFileExt(ff.getName()));
			}
			this.peakExporter.exportBBHInformation(bidiBestHitList, this.bbh,
			        names);

			final List<String> chromatogramNames = new ArrayList<String>();
			for (IFileFragment ff : t) {
				chromatogramNames.add(StringTools.removeFileExt(ff.getName()));
			}

			this.log.info("Exporting whole detailed BBH information");
			this.peakExporter.exportDetailedBBHInformation(bidiBestHitList,
			        this.bbh.getPeakLists(), this.bbh, chromatogramNames,
			        "detailedBBHInformation.csv");
			this.log.info("Exporting peak occurence map");
			this.peakExporter.exportPeakOccurrenceMap(bidiBestHitList, this.bbh
			        .getPeakLists(), this.bbh, chromatogramNames,
			        "peakOccurenceMap.csv");

			// Workaround to export pairwise detailed bbh information
			List<String> filenamest = new ArrayList<String>();
			List<List<Point>> bidiBestHitListt;
			List<List<Peak2D>> peaklists;
			for (int i = 0; i < this.bbh.getPeakLists().size(); i++) {
				for (int j = i + 1; j < this.bbh.getPeakLists().size(); j++) {
					this.log
					        .info(
					                "Exporting pairwise detailed BBH information for {}, {}",
					                chromatogramNames.get(i), chromatogramNames
					                        .get(j));
					peaklists = new ArrayList<List<Peak2D>>();
					peaklists.add(this.bbh.getPeakLists().get(i));
					peaklists.add(this.bbh.getPeakLists().get(j));
					filenamest = new ArrayList<String>();
					filenamest.add(chromatogramNames.get(i));
					filenamest.add(chromatogramNames.get(j));
					bidiBestHitListt = getBidiBestList(i, j, bidiBestHitList);
					this.peakExporter.exportDetailedBBHInformation(
					        bidiBestHitListt, peaklists, this.bbh, filenamest,
					        "pwBBH_" + chromatogramNames.get(i) + "-"
					                + chromatogramNames.get(j) + ".csv");
				}
			}
		}

		return new TupleND<IFileFragment>(ret);
	}

	/**
	 * Creates a filtered BBH List.
	 * 
	 * This method will change the y component of the Point of the BBH list.
	 * This is needed to assure the correctness of the export by
	 * {@link IPeakExporter} (method exportDetailedBBH)
	 * 
	 * @param i
	 *            ith chromatogram
	 * @param j
	 *            jth chromatogram
	 * @param bidiBestHits
	 *            full BBH list
	 * @return BBH list containing only the peaks of the ith and jth
	 *         chromatogram
	 */
	private List<List<Point>> getBidiBestList(final int i, final int j,
	        final List<List<Point>> bidiBestHits) {
		final List<List<Point>> index = new ArrayList<List<Point>>();
		int c = 0;
		for (List<Point> list : bidiBestHits) {
			index.add(new ArrayList<Point>());
			for (int k = 0; k < list.size(); k++) {
				if (k == i) {
					index.get(c).add(new Point(list.get(k).x, 0));
				} else if (k == j) {
					index.get(c).add(new Point(list.get(k).x, 1));
				}
			}
			c++;
		}
		return index;
	}

	/**
	 * This method add a given point ap to the region or the boundary list.
	 * 
	 * @param snake
	 *            snake
	 * @param ap
	 *            active point
	 * @param slc
	 *            scan line cache
	 * @param meanMS
	 *            mean mass spectra
	 */
	private void check(final PeakArea2D snake, final Point ap,
	        final IScanLine slc, final Array meanMS) {
		try {// FIXME Mathias, hier geht was schief
			final Array apMS = slc.getMassSpectra(ap);
			if (isNear(meanMS, apMS)) {
				try {
					snake.addRegionPoint(ap, apMS, this.intensities.get(idx(
					        ap.x, ap.y)));
					snake.addNeighOf(ap);
				} catch (ArrayIndexOutOfBoundsException ex) {
					this.log
					        .error(
					                "Tried to use point {} and access index {}, allowed : [0,{}]",
					                new Object[] { ap, idx(ap.x, ap.y),
					                        this.intensities.getShape()[0] - 1 });
				}
			} else {
				snake.addBoundaryPoint(ap);
			}
		} catch (IndexOutOfBoundsException ex) {
			this.log.error(ex.getLocalizedMessage());
		}
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
		this.minPeakSize = cfg.getInt(this.getClass().getName()
		        + ".minPeakSize", 2);
		this.peakFindingThreshold = cfg.getDouble(this.getClass().getName()
		        + ".peakThreshold", 0.01);
		distClass = cfg.getString(this.getClass().getName() + ".distClass",
		        "maltcms.commands.distances.ArrayCos");
		this.distance = Factory.getInstance().getObjectFactory().instantiate(
		        this.distClass, IArrayDoubleComp.class);
		this.minDistance = cfg.getDouble(
		        this.getClass().getName() + ".minDist", 0.99d);
		this.useMeanMS = cfg.getBoolean(this.getClass().getName()
		        + ".useMeanMS", true);
		this.minVerticalScanIndex = cfg.getInt(this.getClass().getName()
		        + ".minVerticalScanIndex", -1);
		final String bbhClass = cfg
		        .getString(this.getClass().getName() + ".bbhClass",
		                "maltcms.commands.fragments2d.peakfinding.FastBidirectionalBestHit");
		this.bbh = Factory.getInstance().getObjectFactory().instantiate(
		        bbhClass, IBidirectionalBestHit.class);
		final String peakExporterClass = cfg.getString(this.getClass()
		        .getName()
		        + ".peakExporterClass",
		        "maltcms.commands.fragments2d.peakfinding.PeakExporter");
		this.peakExporter = Factory.getInstance().getObjectFactory()
		        .instantiate(peakExporterClass, IPeakExporter.class);
		final String peakIntegrationClass = cfg.getString(this.getClass()
		        .getName()
		        + ".peakIntegrationClass",
		        "maltcms.commands.fragments2d.peakfinding.PeakIntegration");
		this.integration = Factory.getInstance().getObjectFactory()
		        .instantiate(peakIntegrationClass, IPeakIntegration.class);
		final String identificationClass = cfg.getString(this.getClass()
		        .getName()
		        + ".peakIndentificationClass",
		        "maltcms.commands.fragments2d.peakfinding.PeakIdentification");
		this.identification = Factory.getInstance().getObjectFactory()
		        .instantiate(identificationClass, IPeakIdentification.class);
		this.doBBH = cfg.getBoolean(this.getClass().getName() + ".doBBH", true);
		this.maxPeakSize = cfg.getInt(this.getClass().getName()
		        + ".maxPeakSize", 1000);
		this.filterMS = cfg.getBoolean(this.getClass().getName() + ".filterMS",
		        false);
		this.maxDx = cfg.getInt(this.getClass().getName() + ".maxDx", 1);
		this.maxDy = cfg.getInt(this.getClass().getName() + ".maxDy", 1);
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
		ImageTools.saveImage(image, name, this.format, getIWorkflow()
		        .getOutputDirectory(this), this);
		ImageTools.saveImage(image, name + "_emtpy", this.format,
		        getIWorkflow().getOutputDirectory(this), this);

		final File d = getIWorkflow().getOutputDirectory(this);
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
		final PlotRunner pl = new PlotRunner(plot, title, name, d);
		pl.configure(Factory.getInstance().getConfiguration());
		Factory.getInstance().submitJob(pl);
	}

	/**
	 * Creates and writes a csv containing all needed information about the
	 * peak.
	 * 
	 * @param name
	 *            file name without file extension
	 * @param pas
	 *            list of all snakes
	 * @param times
	 *            first and second retention time
	 * @param ff
	 *            file fragment
	 * @return ff
	 */
	private List<Peak2D> createPeaklist(final String name,
	        final List<PeakArea2D> pas,
	        final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times,
	        final IFileFragment ff) {
		final List<Peak2D> peaklist = new ArrayList<Peak2D>();
		final Vector<Integer> region = new Vector<Integer>();
		final Vector<Integer> boundary = new Vector<Integer>();
		final Vector<Integer> regionIndex = new Vector<Integer>();
		final Vector<Integer> boundaryIndex = new Vector<Integer>();
		final Collection<String> peakNames = new ArrayList<String>();
		int maxLength = 10;
		PeakArea2D s;
		for (int i = 0; i < pas.size(); i++) {
			if (i % 10 == 0) {
				this.log.info("	Did " + i);
			}
			s = pas.get(i);
			final Peak2D peak = new Peak2D();
			final double x = times.getFirst().get(s.getSeedPoint().x);
			final double y = times.getSecond().get(s.getSeedPoint().y);
			peak.setPeakArea(s);
			peak.setFirstRetTime(x);
			peak.setSecondRetTime(y);
			peak.setIndex(pas.indexOf(s));
			peak.setScanIndex(s.getIndex());

			if (this.identification != null) {
				this.identification.setName(peak);
				if (peak.getName().length() > maxLength) {
					maxLength = peak.getName().length();
				}
			}
			this.integration.integrate(peak, ff, getIWorkflow());

			peakNames.add(peak.getName());
			peaklist.add(peak);

			regionIndex.add(region.size());
			for (final Point p : s.getRegionPoints()) {
				region.add(idx(p.x, p.y));
			}
			boundaryIndex.add(boundary.size());
			for (final Point p : s.getBoundaryPointsCopy()) {
				boundary.add(idx(p.x, p.y));
			}
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

		return peaklist;
	}

	/**
	 * Find peaks.
	 * 
	 * @param xsize
	 *            x window size
	 * @param ysize
	 *            y window size
	 * @param min
	 *            minimum
	 * @param scanLineCount
	 *            number of scan lines
	 * @param isl
	 *            IScanLine implementation to use for MS retrieval
	 * @return peaklist
	 */
	private List<Point> findPeaks2(final int xsize, final int ysize,
	        final double min, final int scanLineCount, IScanLine isl) {
		final List<Point> peaks = new ArrayList<Point>();

		for (int x = xsize; x < scanLineCount - xsize; x++) {
			for (int y = ysize; y < this.scansPerModulation - ysize; y++) {
				final double currentHeight = this.intensities.get(idx(x, y));
				boolean max = true;
				for (int i = -xsize; i <= xsize; i++) {
					for (int j = -ysize; j <= ysize; j++) {
						if ((i != 0) || (j != 0)) {
							final double nHeight = this.intensities.get(idx(
							        (x + i), (y + j)));
							if (currentHeight < nHeight) {
								max = false;
								break;
							}
						}
					}
					if (!max) {
						break;
					}
				}
				if (max && (currentHeight > min)
				        && (y > this.minVerticalScanIndex)) {
					peaks.add(new Point(x, y));
					Tuple2D<Array, Array> t = isl.getSparseMassSpectra(x, y);
					// this.log.info("Max mass at peak candidate: {}",MaltcmsTools.getMaxMass(t.getFirst(),t.getSecond()));
				}
			}
		}
		return peaks;
	}

	/**
	 * Find peaks.
	 * 
	 * @param xsize
	 *            x window size
	 * @param ysize
	 *            y window size
	 * @param min
	 *            minimum
	 * @param scanLineCount
	 *            number of scan lines
	 * @return peaklist
	 */
	private List<Point> findPeaks(final int xsize, final int ysize,
	        final double min, final int scanLineCount,
	        final int scansPerModulation, final int minVerticalScanIndex,
	        final ArrayDouble.D1 intensities) {

		int minX = 10, maxX = scanLineCount - 1;
		final List<Point> peaks = new ArrayList<Point>();

		for (int x = xsize; x < scanLineCount - xsize; x++) {
			for (int y = ysize; y < scansPerModulation - ysize; y++) {
				final double currentHeight = intensities.get(idx(x, y));
				boolean max = true;
				for (int i = -xsize; i <= xsize; i++) {
					for (int j = -ysize; j <= ysize; j++) {
						if ((i != 0) || (j != 0)) {
							final double nHeight = intensities.get(idx((x + i),
							        (y + j)));
							if (currentHeight < nHeight) {
								max = false;
								break;
							}
						}
					}
					if (!max) {
						break;
					}
				}
				if (max && (currentHeight > min) && (y > minVerticalScanIndex)
				        && (x >= minX) && (x <= maxX)) {
					peaks.add(new Point(x, y));
				}
			}
		}
		return peaks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return "Will do an initial peak finding and computes the 'snakes'";
	}

	/**
	 * Tries to find the used_mass_values array and converts it to an List of
	 * Integer.
	 * 
	 * TODO: Duplicated method. See MeanVarVis - Should be one in ArrayTools?
	 * 
	 * @param ff
	 *            file fragment
	 */
	private void getFilter(final IFileFragment ff) {
		if (this.filterMS) {
			this.log.info("	Filtering mass spectra");
			try {
				final Array holdA = ff.getChild(this.usedMassValuesVar)
				        .getArray();
				final IndexIterator iter = holdA.getIndexIterator();
				this.hold = new ArrayList<Integer>();
				while (iter.hasNext()) {
					this.hold.add(iter.getIntNext());
				}
				// FIXME: wenn cahcedlist benutzt werden, klappt das über slc
				// nicht
				this.log.info("		Using {} of {} masses", holdA.getShape()[0],
				        750);
			} catch (final ResourceNotAvailableException e) {
				this.log.error("Resource {} not available",
				        this.usedMassValuesVar);
				this.log.error("Turning off filtering.");
				this.filterMS = false;
			}
		}
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
		this.log.info("Reading retention time arrays");
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
	 * Getter.
	 * 
	 * @param seedMS
	 *            seed ms
	 * @param neighMS
	 *            neighbour ms
	 * @return true if dist is low enough, otherwise false
	 */
	private boolean isNear(final Array seedMS, final Array neighMS) {
		if ((seedMS != null) && (neighMS != null)) {
			Array sms = seedMS.copy();
			Array nms = neighMS.copy();
			if (this.filterMS) {
				sms = ArrayTools2.filter(sms, this.hold, false);
				nms = ArrayTools2.filter(nms, this.hold, false);
			}
			final double d = this.distance.apply(0, 0, 0, 0, sms, nms);
			// this.log.info("{}", d);
			if (this.distance.minimize()) {
				return (d <= this.minDistance);
			} else {
				return (d >= this.minDistance);
			}
		}
		return false;
	}

	/**
	 * Computes the snake.
	 * 
	 * @param seed
	 *            seed point
	 * @param ff
	 *            file fragment
	 * @param slc
	 *            scan line cache
	 * @return snake area
	 */
	private PeakArea2D regionGrowing(final Point seed, final IFileFragment ff,
	        final IScanLine slc) {
		final long start = System.currentTimeMillis();
		final PeakArea2D pa = new PeakArea2D(seed, slc.getMassSpectra(seed),
		        this.intensities.get(idx(seed.x, seed.y)), idx(seed.x, seed.y),
		        this.scansPerModulation);
		pa.addNeighOf(seed);
		Array meanMS = pa.getMeanMS();

		while (pa.hasActivePoints()) {
			while (pa.hasActivePoints()) {
				check(pa, pa.popActivePoint(), slc, meanMS);
				if (this.useMeanMS) {
					meanMS = pa.getMeanMS();
				}
				if (pa.size() > this.maxPeakSize) {
					break;
				}
			}
			if (pa.size() > this.maxPeakSize) {
				this.log
				        .error(
				                "			Stopping region growing: Limit of {} points/peakarea exceeded (maxPeakSize)",
				                this.maxPeakSize);
				break;
			}
			if (this.useMeanMS) {
				for (final Point bp : pa.getBoundaryPoints()) {
					check(pa, bp, slc, meanMS);
					if (this.useMeanMS) {
						meanMS = pa.getMeanMS();
					}
				}
			}
		}

		this.timesum += System.currentTimeMillis() - start;
		this.count++;
		if (this.count % 10 == 0) {
			this.log.info("		Avg time {} in {} runs",
			        this.timesum / this.count, this.count);
		}
		if ((pa.size() > this.maxPeakSize)
		        || (pa.getRegionPoints().size() < this.minPeakSize)) {
			return null;
		}

		return pa;
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
	        final IFileFragment fret, final List<PeakArea2D> peakAreaList,
	        final int[][] colorRamp) {
		this.log.info("Saving areas");
		final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times = getRetentiontime(ff);
		final ArrayInt.D1 peakindex = new ArrayInt.D1(peakAreaList.size());
		final IndexIterator iter = peakindex.getIndexIterator();
		for (final PeakArea2D pa : peakAreaList) {
			iter.setIntNext(idx(pa.getSeedPoint().x, pa.getSeedPoint().y));
		}

		final IVariableFragment var = new VariableFragment(fret,
		        this.peakListVar);
		var.setArray(peakindex);

		this.log.info("Creating peaklist");
		final List<Peak2D> peaklist = createPeaklist(StringTools
		        .removeFileExt(ff.getName()), peakAreaList, times, fret);

		this.log.info("Saving peaks");
		this.peakExporter.exportPeakInformation(StringTools.removeFileExt(ff
		        .getName()), peaklist);
		this.peakExporter.exportDetailedPeakInformation(StringTools
		        .removeFileExt(ff.getName()), peaklist);
		TupleND<IFileFragment> iff = getIWorkflow().getCommandSequence()
		        .getInput();
		IFileFragment originalFile = null;
		for (IFileFragment f : iff) {
			if (StringTools.removeFileExt(f.getName()).equals(
			        StringTools.removeFileExt(ff.getName()))) {
				originalFile = f;
			}
		}
		IFileFragment pfrag = Factory.getInstance().getFileFragmentFactory()
		        .create(getIWorkflow().getOutputDirectory(this),
		                "osc-" + originalFile.getName(), originalFile);
		IVariableFragment scanRateV = new VariableFragment(pfrag, "scan_rate");
		IVariableFragment modTV = new VariableFragment(pfrag, "modulation_time");
		scanRateV.setArray(ff.getChild("scan_rate").getArray());
		modTV.setArray(ff.getChild("modulation_time").getArray());
		pfrag.save();
		IScanLine isl = ScanLineCacheFactory.getScanLineCache(pfrag);
		this.peakExporter.exportPeaksToMSP(StringTools.removeFileExt(ff
		        .getName())
		        + "-peaks.msp", peaklist, isl);
		// final BufferedImage biSnakes = cross.tools.muChrom.ImageTools
		// .create2DImage(ff.getName(), intensity,
		// this.scansPerModulation, this.doubleFillValue,
		// this.threshold, colorRamp, this.getClass());
		// createAndSaveImage(cross.tools.muChrom.ImageTools.addPeakAreaToImage(
		// biSnakes, peakAreaList, new int[] { 0, 0, 0, 255, },
		// new int[] { 255, 0, 0, 255, }, null,
		// this.scansPerModulation), StringTools.removeFileExt(ff
		// .getName())
		// + "_snakes", "Chromatogram with snakes", peaklist, times);
		createImage(ff, peakAreaList, colorRamp, getRetentiontime(ff), peaklist);
		return peaklist;
	}

	private void createImage(final IFileFragment ff,
	        final List<PeakArea2D> peakAreaList, final int[][] colorRamp,
	        final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> times,
	        final List<Peak2D> peaklist) {
		List<Array> intensities = ff.getChild(this.totalIntensityVar)
		        .getIndexedArray();
		// FIXME: should not be static!
		intensities = intensities.subList(0, intensities.size() - 2);
		final BufferedImage biBoundary = ImageTools.create2DImage(ff.getName(),
		        intensities, this.scansPerModulation, this.doubleFillValue,
		        this.threshold, colorRamp, this.getClass());
		createAndSaveImage(ImageTools.addPeakAreaToImage(biBoundary,
		        peakAreaList, new int[] { 0, 0, 0, 255 }, null, new int[] { 0,
		                0, 0, 255, }, this.scansPerModulation), StringTools
		        .removeFileExt(ff.getName())
		        + "_boundary", "chromatogram with peak boundaries", peaklist,
		        times);
	}

}
