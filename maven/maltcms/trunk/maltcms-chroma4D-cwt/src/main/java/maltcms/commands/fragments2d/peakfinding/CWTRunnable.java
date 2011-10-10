/**
 * 
 */
package maltcms.commands.fragments2d.peakfinding;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.wavelet.MexicanHatWaveletFilter;
import maltcms.commands.fragments2d.peakfinding.output.PeakExporter;
import maltcms.commands.fragments2d.peakfinding.output.PeakIdentification;
import maltcms.commands.fragments2d.peakfinding.picking.IPeakPicking;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.peak.MaltcmsAnnotationFactory;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.datastructures.rank.Rank;
import maltcms.datastructures.rank.RankSorter;
import maltcms.datastructures.ridge.Ridge;
import maltcms.io.csv.ColorRampReader;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.tools.ImageTools;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.math.stat.Frequency;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.data.xy.XYSeries;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import maltcms.ui.viewer.datastructures.tree.ElementNotFoundException;
import maltcms.ui.viewer.datastructures.tree.QuadTree;
import net.sf.maltcms.execution.api.concurrent.ConfigurableRunnable;
import net.sf.maltcms.execution.api.job.Progress;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class CWTRunnable implements ConfigurableRunnable<String>, IPeakPicking {

	/**
     * 
     */
	private static final long serialVersionUID = -517839315109598824L;
	@Configurable(value = "5", type = int.class)
	private int minScale = 5;
	@Configurable(value = "20", type = int.class)
	private int maxScale = 20;
	@Configurable(value = "0.2", type = double.class)
	private double maxRidgeCost = 0.2;
	@Configurable(value = "0.000003", type = double.class)
	private double minRelativeIntensity = 0.000003;
	@Configurable(value = "", type = String.class)
	private String inputFile = "";
	@Configurable(value = "", type = String.class)
	private String outputDir = "";
	@Configurable(name = "var.modulation_time.default", type = double.class, value = "5.0d")
	private double modulationTime = 5.0d;

	@Configurable(name = "var.scan_rate.default", type = double.class, value = "100.0d")
	private double scanRate = 100.0d;

	private QuadTree<Ridge> ridgeTree = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see shared.ConfigurableRunnable#configure(java.io.File)
	 */
	@Override
	public void configure(File arg0) {
		try {
			PropertiesConfiguration pc = new PropertiesConfiguration(arg0);
			configure(pc);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see shared.ConfigurableRunnable#get()
	 */
	@Override
	public String get() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see shared.ConfigurableRunnable#getProgress()
	 */
	@Override
	public Progress getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		IFileFragment f = new FileFragment(new File(this.inputFile));
		IVariableFragment mt = null;
		try {
			mt = f.getChild("modulation_time");
		} catch (ResourceNotAvailableException rnae) {
			mt = new VariableFragment(f, "modulation_time");
			mt.setArray(maltcms.tools.ArrayTools
			        .factoryScalar(this.modulationTime));
		}

		IVariableFragment sr = null;
		try {
			sr = f.getChild("scan_rate");
		} catch (ResourceNotAvailableException rnae) {
			sr = new VariableFragment(f, "scan_rate");
			sr.setArray(maltcms.tools.ArrayTools.factoryScalar(this.scanRate));
		}

		Array tic = f.getChild("total_intensity").getArray();
		// LogFilter lf = new LogFilter();
		// tic = lf.apply(tic);
		ArrayStatsScanner ass = new ArrayStatsScanner();
		StatsMap sm = ass.apply(new Array[] { tic })[0];
		MultiplicationFilter mf = new MultiplicationFilter(
		        1.0 / (sm.get(Vars.Max.name()) - sm.get(Vars.Min.name())));
		tic = mf.apply(tic);
		Percentile p = new Percentile(99);
		double[] tica = (double[]) tic.get1DJavaArray(double.class);
		double ninetyFivePercent = p.evaluate(tica);
		p = new Percentile(85);
		double fivePercent = p.evaluate(tica);
		System.out.println("95% quantile value: " + fivePercent
		        + " 99% quantile value: " + ninetyFivePercent);

		Array sat = f.getChild("scan_acquisition_time").getArray();
		// TopHatFilter thf = new TopHatFilter();
		// thf.setWindow(150);
		// tic = thf.apply(tic);
		// ChromatogramFactory cf = new ChromatogramFactory();
		// IChromatogram2D ic2d = cf.createChromatogram2D(f);
		// MexicanHatWaveletFilter mhwf = new MexicanHatWaveletFilter();
		// mhwf.setScale(10);
		// int n = 256;
		// ArrayDouble.D1 arr = new ArrayDouble.D1(n);
		// for (int i = 0; i < n; i++) {
		// arr.set(i, ((Math.random()) * (Math.pow(2, 32)))
		// + ((i > 0) ? arr.get(i - 1) * 0.5 : 0));
		// }
		System.out.println(tic.getShape()[0] + " values");
		int spm = (int) (mt.getArray().getDouble(0) * sr.getArray()
		        .getDouble(0));
		int modulations = tic.getShape()[0] / spm;
		System.out.println("Using " + spm
		        + " scans per modulation, total modulations: " + modulations);
		// ArrayDouble.D2 filtered = new ArrayDouble.D2(modulations, spm);
		// ArrayDouble.D2 original = new ArrayDouble.D2(modulations, spm);
		// LinkedList<Point> maxima = new LinkedList<Point>();
		// for (int i = 5; i < modulations - 1; i++) {
		List<Ridge> r = apply(f.getName(), tic, sat, f, 0, modulations, spm,
		        fivePercent);
		// r = findRidgeMaxima(r,tic);
		List<Peak2D> l = createPeaksForRidges(f, tic, sat, r, spm);
		// identifyPeaks(l);
		saveToAnnotationsFile(f, l);
	}

	private void identifyPeaks(List<Peak2D> l) {
		// String idclass =
		// "maltcms.commands.fragments2d.peakfinding.output.PeakIdentification";
		// String dbFile = idclass+".dbFile";
		// String threshold = idclass+".dbThreshold";
		// String doSearch = idclass+".doSearch";
		// String k = idclass+".kBest";
		// String masq = idclass+".masq";
		// PropertiesConfiguration pc = new PropertiesConfiguration();
		// pc.setProperty(dbFile,
		// "/Users/nilshoffmann/Documents/maltcms/data/mbeckmann/NIST-EIMS-TMS.db4o");
		// pc.setProperty(threshold,0.7);
		// pc.setProperty(doSearch,true);
		// pc.setProperty(k,5);
		// pc.setProperty(masq,Collections.emptyList());
		// PeakIdentification pi = new PeakIdentification();
		// pi.configure(pc);
		// for(Peak2D r:l) {
		// pi.setName(r);
		// }
	}

	private PeakIdentification createPeakIdentification() {
		String idclass = "maltcms.commands.fragments2d.peakfinding.output.PeakIdentification";
		String dbFile = idclass + ".dbFile";
		String threshold = idclass + ".dbThreshold";
		String doSearch = idclass + ".doSearch";
		String k = idclass + ".kBest";
		String masq = idclass + ".masq";
		PropertiesConfiguration pc = new PropertiesConfiguration();
		pc.setProperty(dbFile,
		        "/Users/nilshoffmann/Documents/maltcms/gmd/GMD_20100614_VAR5_FAME.db4o");
		pc.setProperty(threshold, 0.7);
		pc.setProperty(doSearch, true);
		pc.setProperty(k, 5);
		pc.setProperty(masq, Collections.emptyList());
		PeakIdentification pi = new PeakIdentification();
		pi.configure(pc);
		return pi;
	}

	private List<Peak2D> createPeaksForRidges(IFileFragment f, Array tic,
	        Array sat, List<Ridge> r, int spm) {
		int index = 0;
		Index tidx = tic.getIndex();
		Index sidx = sat.getIndex();
		// List<Scan2D> scans = new ArrayList<Scan2D>();
		System.out.println("Building scans");
		List<Peak2D> p2 = new LinkedList<Peak2D>();
		IScanLine isl = ScanLineCacheFactory.getScanLineCache(f);
		PeakIdentification pi = createPeakIdentification();
		for (Ridge ridge : r) {
			System.out.println("Processing Ridge " + (index + 1) + " "
			        + r.size());
			Peak2D p = new Peak2D();
			// Point pt = ic2d.getPointFor(ridge.getGlobalScanIndex());
			p.setIndex(index++);
			// Scan2D s2d = ic2d.getScan(ridge.getGlobalScanIndex());
			// scans.add(s2d);
			p.setApexIndex(ridge.getGlobalScanIndex());
			p.setFile(f.getName());
			p.setIntensity(tic.getDouble(tidx.set(p.getApexIndex())));
			p.setApexTime(sat.getDouble(sidx.set(p.getApexIndex())));
			p2.add(p);
			Point2D.Double ps = getPointForRidge(ridge, spm);
			Point seed = new Point((int) ps.getX(), (int) ps.getY());
			PeakArea2D pa2 = new PeakArea2D(seed, isl.getMassSpectra(seed),
			        p.getIntensity(), p.getApexIndex(), spm);
			p.setPeakArea(pa2);
			pi.setName(p);
			List<Tuple2D<Double, IMetabolite>> t = p.getNames();
			for (Tuple2D<Double, IMetabolite> tple : t) {
				System.out.println("Score: " + tple.getFirst() + " Name: "
				        + tple.getSecond().getName());
			}
		}
		return p2;
	}
	
//	private Array getFilteredMS(IScanLineCache isl, Point2D.Double p) {
//		
//	}

	/**
	 * @param f
	 * @param tic
	 * @param sat
	 * @param r
	 */
	private void saveToAnnotationsFile(IFileFragment f, List<Peak2D> l) {
		MaltcmsAnnotationFactory maf = new MaltcmsAnnotationFactory();
		MaltcmsAnnotation ma = maf.createNewMaltcmsAnnotationType(new File(f
		        .getAbsolutePath()).toURI());
		// List<Scan2D> scans = new ArrayList<Scan2D>();
		System.out.println("Building scans");
		// List<Peak2D> p2 = new LinkedList<Peak2D>();
		for (Peak2D p : l) {
			maf.addPeakAnnotation(ma, CWTPeakFinder.class.getName(), p);
		}
		File outf = new File(outputDir, StringTools.removeFileExt(f.getName())
		        + ".mann.xml");
		maf.save(ma, outf);
	}

	private QuadTree<Ridge> getRidgeTree() {
		return this.ridgeTree;
	}

	private List<Ridge> apply(String filename, final Array arr,
	        final Array sat, final IFileFragment f, final int x,
	        final int modulations, final int spm,
	        final double fivePercentPercentile) {
		MexicanHatWaveletFilter cwt = new MexicanHatWaveletFilter();

		List<Double> scales = new LinkedList<Double>();

		final ArrayDouble.D2 scaleogram = new ArrayDouble.D2(arr.getShape()[0],
		        maxScale);
		for (int i = 1; i <= maxScale; i++) {
			double scale = ((double) i);
			// System.out.println("Scale: " + scale);
			cwt.setScale(scale);
			Array res = cwt.apply(arr);
			Index resI = res.getIndex();
			for (int j = 0; j < res.getShape()[0]; j++) {
				scaleogram.set(j, i - 1, res.getDouble(resI.set(j)));
			}
			scales.add(scale);
		}
		// System.out.println("Scaleogram: " + scaleogram);
		int rad = 3;
		List<Ridge> ridges = followRidgesBottomUp(fivePercentPercentile,
		        scaleogram, scales, minScale, maxScale);
		ridges = filterVerticallyAdjacentRidges(ridges, rad);
		BufferedImage bi1 = createPeakOverlayImage(
		        StringTools.removeFileExt(filename), "allRidges", arr,
		        modulations, spm, ridges);
		createRidgeImages(StringTools.removeFileExt(filename),
		        "ridge-before-filter", spm, scaleogram, ridges, bi1, 164, 165,
		        166, 167);

		// exportPeaks("cwtAllPeaks", ridges, arr, sat, f);

		Rectangle2D.Double boundingBox = getBoundingBox(ridges, spm);
		QuadTree<Ridge> qr = getQuadTree(ridges, boundingBox, spm);
		// List<JFreeChart> rccharts = CWTChartFactory.create2DRidgeCostDS(spm,
		// ridges.toArray(new Ridge[ridges.size()]));
		// File odir = getOutputDir(StringTools.removeFileExt(filename));
		// // CWTChartFactory.saveImages(odir, "ridgeCost2D", rccharts);
		// List<JFreeChart> nhc2d = CWTChartFactory.create2DRidgeNeighborhoodDS(
		// spm, qr, 20, ridges.toArray(new Ridge[ridges.size()]));
		// CWTChartFactory.saveImages(odir, "ridgeNeighborhood2D", nhc2d);
		// List<JFreeChart> costHist = CWTChartFactory.createCostHistogramDS(20,
		// ridges.toArray(new Ridge[ridges.size()]));
		// CWTChartFactory.saveImages(odir, "ridgeCostHistogram", costHist);
		// List<JFreeChart> neighHist = CWTChartFactory
		// .createNeighborhoodHistogramDS(spm, 20, qr, 20,
		// ridges.toArray(new Ridge[ridges.size()]));
		// CWTChartFactory.saveImages(odir, "ridgeNeighborhoodHistogram",
		// neighHist);

		// System.out.println("Shape of scaleogram: "
		// + Arrays.toString(scaleogram.getShape()));
		// ridges = filter2DRidges(ridges, arr, modulations, spm,
		// minRelativeIntensity, maxRidgeCost);
		// BufferedImage bi2 = createPeakOverlayImage(
		// StringTools.removeFileExt(filename), "afterRidgeCostFilter",
		// arr, modulations, spm, ridges);

		// qr = getQuadTree(ridges, boundingBox, spm);
		// simple radius search is too easy,
		// something like compactness is required?
		// maybe peaks per area?
		List<Rank<Ridge>> ranks = new LinkedList<Rank<Ridge>>();
		for (Ridge r : ridges) {
			ranks.add(new Rank<Ridge>(r));
		}
		filterByRidgeCost(ranks);
		// filterByVerticalRidgeNeighborhood(ranks, 1, spm, qr, 1);
		filterByHorizontalRidgeNeighborhood(ranks,1, spm, qr, 1);
		filterRidgesByResponse(ranks, arr);
		double radius = 5;
		int maxn = 2;
		filterByRidgeNeighborhood(ranks, radius, spm, qr, maxn);
		// filterRidgesByPercentile(ranks, arr, fivePercentPercentile);
		// ridges = filterRidgeNeighborhood(ridges, qr, radius, spm, maxn);

		// BufferedImage bi3 = createPeakOverlayImage(
		// StringTools.removeFileExt(filename),
		// "afterRidgeNeighborhoodFilter", arr, modulations, spm, ridges);

		Collections.sort(ranks);

		ridges = filterRidgesByCWTResponse(ranks, 100);

		BufferedImage bi4 = createPeakOverlayImage(
		        StringTools.removeFileExt(filename),
		        "afterRidgeResponseMaxKFilter", arr, modulations, spm, ridges);

		createRidgeImages(StringTools.removeFileExt(filename),
		        "ridge-after-filter", spm, scaleogram, ridges, bi1, 164, 165,
		        166, 167);

		System.out.println("Found " + ridges.size() + " ridges at maxScale="
		        + maxScale);

		// System.out.println("Applying wavelet synthesis");
		// final double[] restored = cwt
		// .applyInverseTransform(scaleImages, scales);
		// maxima.addAll(getMaxima(restored, x));
		// double min = MathTools.min(restored);
		// for (int i = 0; i < restored.length; i++) {
		// restored[i] = restored[i] - min;
		// }

		// dxyd.addSeries("Restored",
		// JFreeChartTools.getXYDataSeries(restored));
		//
		// showCharts(dxyd, origAnn, origGaussAnn, annotations, categories,
		// points);
		// final double[] restored = new double[]{};
		// return restored;

		this.ridgeTree = getQuadTree(ridges, boundingBox, spm);
		return ridges;
	}

	private List<Ridge> filterVerticallyAdjacentRidges(List<Ridge> ridges, int radius) {
		List<Ridge> survivors = new LinkedList<Ridge>();
		int i = 0;
		for (Ridge r : ridges) {
			if (isMaxInRadius(ridges, i, radius)) {
				survivors.add(r);
			}
			i++;
		}
		return survivors;
	}

	private boolean isMaxInRadius(List<Ridge> ridges, int i, int radius) {
		List<Ridge> sublist = ridges.subList(Math.max(0, i - radius),
		        Math.min(ridges.size() - 1, i + radius));
		Ridge max = null;
		double maxResponse = Double.NEGATIVE_INFINITY;
		Ridge r = ridges.get(i);
		for (Ridge ridge : sublist) {
			double response = ridge.getRidgePoints().get(0).getSecond();
			if (response > maxResponse) {
				max = ridge;
				maxResponse = response;
			}
		}
		return (r == max) ? true : false;
	}

	private List<Ridge> filterRidgesByCWTResponse(List<Rank<Ridge>> l, int topk) {
		RankSorter rs = new RankSorter(l);
		// String[] fields = new
		// String[]{"percentile","ridgeNeighborhood","horizontalRidgeNeighborhood"};
		String[] fields = new String[] { "response","horizontalRidgeNeighborhood","ridgeNeighborhood" };
		rs.sortToOrder(Arrays.asList(fields), l);
		// System.out.println(l);
		// Collections.sort(l,new Rank.ResponseComparator<Ridge>());
		LinkedList<Ridge> ridges = new LinkedList<Ridge>();
		for (int i = 0; i < topk; i++) {
			Rank<Ridge> rank = l.get(i);
			System.out.print("Rank " + (i + 1) + "/" + topk + ": ");
			for (int j = fields.length - 1; j >= 0; j--) {
				System.out.print("[" + fields[j] + ": "
				        + rank.getRank(fields[j]) + "]");
			}
			System.out.println();
			ridges.add(rank.getRidge());
		}
		return ridges;
	}

	private void filterByRidgeCost(List<Rank<Ridge>> ranks) {
		for (Rank<Ridge> rank : ranks) {
			Ridge r = rank.getRidge();
			double penalty = r.getRidgeCost();
			rank.addRank("ridgeCost", penalty);
		}
	}

	private void filterRidgesByResponse(List<Rank<Ridge>> ranks, Array tic) {
		// List<Ridge> rr = new ArrayList<Ridge>();
		for (Rank<Ridge> rank : ranks) {
			Ridge r = rank.getRidge();
			int x = (int) r.getRidgePoints().get(0).getFirst().getX();
			double val = tic.getDouble(x);
			// if (val >= percentile) {
			rank.addRank("response", -val);

		}
	}

	private double[] truncateZeros(double[] d) {
		// int leftBound = -1;
		// for (int i = 0; i < d.length; i++) {
		// if (d[i] != 0) {
		// leftBound = i;
		// break;
		// }
		// }
		int rightBound = -1;
		for (int i = d.length - 1; i > 0; i--) {
			if (d[i] != 0) {
				rightBound = i;
				break;
			}
		}
		int lb = 0;// Math.max(leftBound, 0);
		int rb = Math.min(rightBound, d.length - 1);
		if (lb <= rb) {
			return Arrays.copyOfRange(d, lb, rb);
		}
		return d;
	}

	private void filterByRidgeNeighborhood(List<Rank<Ridge>> r, double i,
	        int spm, QuadTree<Ridge> qt, int threshold) {
		double[] vals = new double[r.size()];
		int n = (int) i;
		// double[] cntDistr = new double[(((2*n) + 1)*((2*n) +1)-1)];
		int binsize = 1;// /bins;
		System.out.println("Using threshold: " + threshold);
		int cnt = 0;
		for (Rank<Ridge> rank : r) {
			Ridge ridge = rank.getRidge();
			Point2D root = ridge.getRidgePoints().get(0).getFirst();
			double x = root.getX() / spm;
			double y = root.getX() % spm;
			// qt.getNeighborsInRadius(new Point2D.Double(x, y), i);
			double v = qt.getNeighborsInRadius(new Point2D.Double(x, y), i)
			        .size();
			vals[cnt] = v;
			cnt++;
		}
		int classID = 0;
		HashSet<Ridge> filtered = new HashSet<Ridge>();
		for (int k = 0; k < vals.length; k++) {
			if (vals[k] <= threshold) {// keep
				r.get(k).addRank("ridgeNeighborhood", -vals[k]);
				// r.get(k).setClassLabel("v" + 0);// filtered.add(r.get(k));
				// filtered.add(r.get(k));
			} else {
				r.get(k).addRank("ridgeNeighborhood", vals[k]);
				// r.get(k).setClassLabel("v" + 1);
			}

		}
		// }

		// return new ArrayList<Ridge>(filtered);
	}

	private List<Rank<Ridge>> filterByHorizontalRidgeNeighborhood(List<Rank<Ridge>> r,
	        double i, int spm, QuadTree<Ridge> qt, int threshold) {
		double[] vals = new double[r.size()];
		int n = (int) i;
		// double[] cntDistr = new double[(((2*n) + 1)*((2*n) +1)-1)];
		int binsize = 1;// /bins;
		System.out.println("Using threshold: " + threshold);
		int cnt = 0;
		for (Rank<Ridge> rank : r) {
			Ridge ridge = rank.getRidge();
			Point2D root = ridge.getRidgePoints().get(0).getFirst();
			double x = root.getX() / spm;
			double y = root.getX() % spm;
			// qt.getNeighborsInRadius(new Point2D.Double(x, y), i);
			double v = qt.getHorizontalNeighborsInRadius(
			        new Point2D.Double(x, y), i).size();
			vals[cnt] = v;
			cnt++;
		}
		int classID = 0;
		LinkedHashSet<Rank<Ridge>> filtered = new LinkedHashSet<Rank<Ridge>>();
		for (int k = 0; k < vals.length; k++) {
			if (vals[k] <= threshold) {// keep
				r.get(k).addRank("horizontalRidgeNeighborhood", vals[k]);
				// r.get(k).setClassLabel("v" + 0);// filtered.add(r.get(k));
				filtered.add(r.get(k));
			} else {
				r.get(k).addRank("horizontalRidgeNeighborhood", vals[k]);
				// r.get(k).setClassLabel("v" + 1);
			}

		}
		// }
		return new ArrayList<Rank<Ridge>>(filtered);
		// return new ArrayList<Ridge>(filtered);
	}

	private List<Rank<Ridge>> filterByVerticalRidgeNeighborhood(List<Rank<Ridge>> r,
	        double i, int spm, QuadTree<Ridge> qt, int threshold) {
		double[] vals = new double[r.size()];
		int n = (int) i;
		// double[] cntDistr = new double[(((2*n) + 1)*((2*n) +1)-1)];
		int binsize = 1;// /bins;
		System.out.println("Using threshold: " + threshold);
		int cnt = 0;
		for (Rank<Ridge> rank : r) {
			Ridge ridge = rank.getRidge();
			Point2D root = ridge.getRidgePoints().get(0).getFirst();
			double x = root.getX() / spm;
			double y = root.getX() % spm;
			// qt.getNeighborsInRadius(new Point2D.Double(x, y), i);
			double v = qt.getVerticalNeighborsInRadius(
			        new Point2D.Double(x, y), i).size();
			vals[cnt] = v;
			cnt++;
		}
		int classID = 0;
		LinkedHashSet<Rank<Ridge>> filtered = new LinkedHashSet<Rank<Ridge>>();
		for (int k = 0; k < vals.length; k++) {
			if (vals[k] <= threshold) {// keep
				r.get(k).addRank("verticalRidgeNeighborhood", vals[k]);
				// r.get(k).setClassLabel("v" + 0);// filtered.add(r.get(k));
				filtered.add(r.get(k));
			} else {
				r.get(k).addRank("verticalRidgeNeighborhood", vals[k]);
				// r.get(k).setClassLabel("v" + 1);
			}

		}
		// }

		return new ArrayList<Rank<Ridge>>(filtered);
	}

	private void exportPeaks(String name, List<Ridge> r, Array tic, Array sat,
	        IFileFragment f) {
		int index = 0;
		Index tidx = tic.getIndex();
		Index sidx = sat.getIndex();
		List<Peak2D> peaks = new ArrayList<Peak2D>(r.size());
		for (Ridge ridge : r) {
			Peak2D p = new Peak2D();
			// Point pt = ic2d.getPointFor(ridge.getGlobalScanIndex());
			p.setIndex(index++);
			// Scan2D s2d = ic2d.getScan(ridge.getGlobalScanIndex());
			// scans.add(s2d);
			p.setApexIndex(ridge.getGlobalScanIndex());
			p.setFile(f.getName());
			p.setIntensity(tic.getDouble(tidx.set(p.getApexIndex())));
			p.setApexTime(sat.getDouble(sidx.set(p.getApexIndex())));
		}
		PeakExporter pe = new PeakExporter();
		DefaultWorkflow dw = new DefaultWorkflow();
		pe.setIWorkflow(dw);
		pe.exportPeakInformation(name, peaks);
	}

	// //TODO
	// private List<Ridge> findRidgeMaxima(List<Ridge> r,Array tic) {
	// for(Ridge ridge:r) {
	// Ridge s = new Ridge();
	// }
	// }
	/**
	 * @param array
	 * @return
	 */
	private static BufferedImage createColorHeatmap(ArrayDouble.D2 array) {
		// BufferedImage bi = new
		// BufferedImage(array.getShape()[0],array.getShape()[1],BufferedImage.TYPE_INT_RGB);
		BufferedImage bi = ImageTools.makeImage2D(array, 256);
		final ColorRampReader crr = new ColorRampReader();
		final int[][] colorRamp = crr.getDefaultRamp();
		Color[] cRamp = ImageTools.rampToColorArray(colorRamp);
		int nsamples = 256;
		double[] sampleTable = ImageTools.createSampleTable(nsamples);
		// double[] bp = ImageTools.getBreakpoints(array, nsamples,
		// Double.NEGATIVE_INFINITY);
		// ImageTools.makeImage2D(bi.getRaster(), array, nsamples, colorRamp,
		// 0.0d, bp);
		sampleTable = ImageTools.mapSampleTable(sampleTable, -1, 1);
		// System.out.println("Sampletable: " +
		// Arrays.toString(sampleTable));
		BufferedImage crampImg = ImageTools.createColorRampImage(sampleTable,
		        Transparency.TRANSLUCENT, cRamp);
		BufferedImage destImg = ImageTools.applyLut(bi,
		        ImageTools.createLookupTable(crampImg, 1.0f, nsamples));
		return destImg;
		// return bi;
	}

	/**
	 * @param array
	 * @return
	 */
	private static BufferedImage createAdaptiveColorHeatmap(ArrayDouble.D2 array) {
		// BufferedImage bi = new
		// BufferedImage(array.getShape()[0],array.getShape()[1],BufferedImage.TYPE_INT_RGB);
		BufferedImage bi = new BufferedImage(array.getShape()[0],
		        array.getShape()[1], BufferedImage.TYPE_INT_RGB);;// ImageTools.makeImage2D(array,
		                                                          // 256);
		final ColorRampReader crr = new ColorRampReader();
		final int[][] colorRamp = crr.getDefaultRamp();
		// Color[] cRamp = ImageTools.rampToColorArray(colorRamp);
		int nsamples = 256;
		double[] sampleTable = ImageTools.createSampleTable(nsamples);
		double[] bp = ImageTools.getBreakpoints(array, nsamples,
		        Double.NEGATIVE_INFINITY);
		ImageTools.makeImage2D(bi.getRaster(), array, nsamples, colorRamp,
		        0.0d, bp);
		// sampleTable = ImageTools.mapSampleTable(sampleTable, -1, 1);
		// System.out.println("Sampletable: " +
		// Arrays.toString(sampleTable));
		// BufferedImage crampImg = ImageTools.createColorRampImage(sampleTable,
		// Transparency.TRANSLUCENT, cRamp);
		// BufferedImage destImg = ImageTools.applyLut(bi, ImageTools
		// .createLookupTable(crampImg, 1.0f, nsamples));
		return bi;
		// return bi;
	}

	private List<Integer> getPeakMaxima(ArrayDouble.D2 scaleogram, int row) {
		double[] scaleResponse = (double[]) scaleogram.slice(1, row)
		        .get1DJavaArray(double.class);
		FirstDerivativeFilter fdf = new FirstDerivativeFilter();
		double[] res = (double[]) fdf.apply(Array.factory(scaleResponse))
		        .get1DJavaArray(double.class);
		List<Integer> peakMaxima = new LinkedList<Integer>();
		for (int i = 1; i < scaleResponse.length - 1; i++) {
			if (res[i - 1] >= 0 && res[i + 1] <= 0) {
				// remove peaks, which are not true maxima
				peakMaxima.add(i);
			}
		}
		return peakMaxima;
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

	private List<Ridge> filter2DRidges(List<Ridge> ridges, final Array arr,
	        final int modulations, final int spm, final double minIntenRatio,
	        final double maxRidgePenalty) {
		// Index aidx = arr.getIndex();
		double totalIntensity = maltcms.tools.ArrayTools.integrate(arr);
		// double[] modulationsSum = new double[modulations];
		// for (int i = 0; i < modulations; i++) {
		// for (int j = 0; j < spm; j++) {
		// modulationsSum[i] += arr.getDouble(aidx.set((i * spm) + j));
		// }
		// }

		int removed = 0;
		int ridgesSize = ridges.size();
		List<Ridge> toKeep = new ArrayList<Ridge>();
		for (int i = 0; i < ridges.size(); i++) {
			Ridge r = ridges.get(i);
			double rp = Math.abs(r.getRidgeCost());
			double maxVal = r.getRidgePoints().get(0).getSecond();
			double intenRatio = (maxVal / totalIntensity);
			// double intenRatio = (maxVal / modulationsSum[seedIndex]);
			if (rp <= maxRidgePenalty) {
				// if (intenRatio >= minIntenRatio) {
				toKeep.add(r);
				// } else {
				// removed++;
				// }
				// System.out.println("Removing degenerate ridge " + r);
			} else {
				// ridges.remove(i);
				removed++;
			}
		}
		System.out.println("Removed " + removed + " out of " + ridgesSize
		        + " ridges!");
		return toKeep;
	}

	private void filter1DRidges(List<Ridge> ridges, final Array arr,
	        final double minIntenRatio, final double maxRidgePenalty) {
		double sum = maltcms.tools.ArrayTools.integrate(arr);
		for (int i = 0; i < ridges.size(); i++) {
			Ridge r = ridges.get(i);
			double rp = Math.abs(r.getRidgeCost());
			double maxVal = r.getRidgePoints().get(0).getSecond();
			double intenRatio = (maxVal / sum);
			if (intenRatio <= minIntenRatio || rp > maxRidgePenalty) {
				// System.out.println("Removing degenerate ridge " + r);
				ridges.remove(i);
			}
		}
	}

	/**
	 * @param arr
	 * @param modulations
	 * @param spm
	 * @param ridges
	 * @return
	 */
	private BufferedImage createPeakOverlayImage(final String filename,
	        final String prefix, final Array arr, final int modulations,
	        final int spm, List<Ridge> ridges) {
		Index aidx = arr.getIndex();
		ArrayDouble.D2 heatmap = new ArrayDouble.D2(modulations, spm);
		for (int i = 0; i < modulations; i++) {
			for (int j = 0; j < spm; j++) {
				heatmap.set(i, j, arr.getDouble(aidx.set((i * spm) + j)));
			}
		}
		int samples = 256;
		BufferedImage cramp = ImageTools.createColorRampImage(
		        ImageTools.createSampleTable(samples),
		        BufferedImage.TRANSLUCENT, Color.WHITE, Color.BLACK);
		BufferedImage hmImg = createAdaptiveColorHeatmap(heatmap);
		Graphics2D hmg2 = hmImg.createGraphics();
		// int cnt = 0;
		// System.out.println("Painting "+ridges.size()+" peak markers");
		for (int i = 0; i < ridges.size(); i++) {
			Ridge r = ridges.get(i);
			hmg2.setComposite(AlphaComposite.getInstance(
			        AlphaComposite.SRC_OVER, 0.9f));
			// System.out.println("Intensity ratio for ridge: " + intenRatio);
			// System.out.println("Ridge penalty: "+r.getRidgePenalty());
			int sample = Math.max(
			        0,
			        Math.min(samples - 1,
			                (int) (cramp.getWidth() * r.getRidgeCost())));
			Color c = new Color(cramp.getRGB(sample, 0));
			String clabel = r.getClassLabel();
			hmg2.setColor(c);
			Point2D p = r.getRidgePoints().get(0).getFirst();
			double xl = Math.floor(p.getX() / spm);
			double yl = p.getX() - (xl * spm);
			// System.out.println("Adding peak marker at "+xl+" "+yl);
			Rectangle2D.Double r2d = new Rectangle2D.Double(xl, yl, 1, 1);
			hmg2.fill(r2d);
			// hmg2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f));
			// hmg2.setColor(Color.BLACK);
			// hmg2.setFont(Font.decode("Sans-5pt"));
			// hmg2.drawString(clabel, (float)xl, (float)yl);

			// TODO find modulation / solvent peak and follow 2D ridge

			// if(cnt==1000) {
			// break;
			// }
			// cnt++;
		}
		String fname = StringTools.removeFileExt(filename);
		File dir = new File(outputDir, fname);
		dir.mkdirs();
		ImageTools.saveImage(hmImg, prefix + "-simp-with-ridgeseeds", "png",
		        dir, null);
		return hmImg;
	}

	private File getOutputDir(String filename) {
		String fname = StringTools.removeFileExt(filename);
		File dir = new File(outputDir, fname);
		dir.mkdirs();
		return dir;
	}

	/**
	 * @param spm
	 * @param scaleogram
	 * @param ridges
	 */
	private void createRidgeImages(final String filename, final String prefix,
	        final int spm, final ArrayDouble.D2 scaleogram, List<Ridge> ridges,
	        BufferedImage hmImg, int... selection) {

		BufferedImage bi = ImageTools.makeImage2D(scaleogram, 256);
		Graphics2D g2 = bi.createGraphics();
		for (Ridge r : ridges) {
			// if (Math.abs(r.getRidgePenalty()) <= 0.1) {
			r.draw(g2);
			// }
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		if (width > spm) {
			int parts = (width / spm);
			if (width % spm != 0) {
				parts++;
			}
			if (selection != null && selection.length > 0) {
				System.out.println("Writing scaleogram to " + selection.length
				        + " parts");
				for (int j = 0; j < selection.length; j++) {
					int partWidth = spm;
					int i = selection[j];
					if ((i * partWidth) + partWidth > width) {
						partWidth = width - (i * partWidth);
					}
					// System.out.println("Part " + (i + 1) + "/" + parts +
					// " from "
					// + (i * partWidth) + " to "
					// + (i * partWidth + partWidth));
					BufferedImage combinedImage = new BufferedImage(partWidth,
					        height + 1, BufferedImage.TYPE_INT_ARGB);
					BufferedImage subScaleogram = bi.getSubimage(i * partWidth,
					        0, partWidth, height);
					BufferedImage subHeatmap = hmImg.getSubimage(i, 0, 1,
					        hmImg.getHeight());
					Graphics2D g2comb = combinedImage.createGraphics();
					AffineTransform at = AffineTransform.getTranslateInstance(
					        subHeatmap.getHeight() / 2, 0);
					at.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
					at.concatenate(AffineTransform.getQuadrantRotateInstance(1,
					        0, 0));
					at.concatenate(AffineTransform.getTranslateInstance(0,
					        -subHeatmap.getHeight() / 2));
					g2comb.drawImage(subHeatmap, at, null);
					g2comb.drawImage(subScaleogram, 0, 1, null);
					String fname = StringTools.removeFileExt(filename);
					File dir = new File(outputDir, fname);
					dir.mkdirs();
					try {
						ImageIO.write(combinedImage, "PNG", new File(dir,
						        prefix + "-" + (i + 1) + "_of_" + parts
						                + ".png"));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} else {
				System.out.println("Writing scaleogram to " + parts + " parts");
				for (int i = 0; i < parts; i++) {
					int partWidth = spm;
					if ((i * partWidth) + partWidth > width) {
						partWidth = width - (i * partWidth);
					}
					// System.out.println("Part " + (i + 1) + "/" + parts +
					// " from "
					// + (i * partWidth) + " to "
					// + (i * partWidth + partWidth));
					BufferedImage combinedImage = new BufferedImage(partWidth,
					        height + 1, BufferedImage.TYPE_INT_ARGB);
					BufferedImage subScaleogram = bi.getSubimage(i * partWidth,
					        0, partWidth, height);
					BufferedImage subHeatmap = hmImg.getSubimage(i, 0, 1,
					        hmImg.getHeight());
					Graphics2D g2comb = combinedImage.createGraphics();
					AffineTransform at = AffineTransform.getTranslateInstance(
					        subHeatmap.getHeight() / 2, 0);
					at.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
					at.concatenate(AffineTransform.getQuadrantRotateInstance(1,
					        0, 0));
					at.concatenate(AffineTransform.getTranslateInstance(0,
					        -subHeatmap.getHeight() / 2));
					g2comb.drawImage(subHeatmap, at, null);
					g2comb.drawImage(subScaleogram, 0, 1, null);
					String fname = StringTools.removeFileExt(filename);
					File dir = new File(outputDir, fname);
					dir.mkdirs();
					try {
						ImageIO.write(combinedImage, "PNG", new File(dir,
						        prefix + "-" + (i + 1) + "_of_" + parts
						                + ".png"));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		} else {
			try {
				String fname = StringTools.removeFileExt(filename);
				File dir = new File(outputDir, fname);
				dir.mkdirs();
				ImageIO.write(bi, "PNG", new File(dir, "ridges.png"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * @param maxima
	 * @param x
	 * @param row
	 * @param annotations
	 * @param categories
	 * @param points
	 * @param factor
	 * @param scales
	 * @param scaleImages
	 * @param i
	 * @param scale
	 * @param res
	 */
	private void createScaleogram(final List<Point> maxima, final int x,
	        int row, List<List<XYAnnotation>> annotations,
	        List<String> categories, List<double[]> points, double factor,
	        List<Double> scales, List<double[]> scaleImages, int i,
	        double scale, double[] res) {
		scaleImages.add(res);
		scales.add(scale);
		// annotations.add(getPeaksFor(res, row));
		// if (i == 16) {
		// maxima.addAll(getMaxima(res, x));
		// }
		//
		// categories.add("cwt/mh " + ((double) i) / factor);
		// JFreeChartTools.addXYZDataset(points, res, row);
	}

	private Rectangle2D.Double getBoundingBox(List<Ridge> ridges, int spm) {
		Rectangle2D.Double bbox = null;
		for (Ridge r : ridges) {
			Point2D p = getPointForRidge(r, spm);
			if (bbox == null) {
				bbox = new Rectangle2D.Double(p.getX(), p.getY(), 1, 1);
			} else {
				bbox.add(p.getX(), p.getY());
			}
		}
		return bbox;
	}

	private Point2D.Double getPointForRidge(Ridge r, int spm) {
		Point2D p = r.getRidgePoints().get(0).getFirst();
		double xl = p.getX() / spm;
		double yl = p.getX() % spm;
		return new Point2D.Double(xl, yl);
	}

	private QuadTree<Ridge> getQuadTree(List<Ridge> ridges,
	        Rectangle2D.Double boundingBox, int spm) {
		QuadTree<Ridge> qt = new QuadTree<Ridge>(boundingBox.x, boundingBox.y,
		        boundingBox.width + 1, boundingBox.height + 1, 5);
		for (Ridge r : ridges) {
			qt.put(getPointForRidge(r, spm), r);
		}
		return qt;
	}

	private List<Ridge> filterRidgeNeighborhood(List<Ridge> ridges,
	        QuadTree<Ridge> qr, double radius, int spm, int maxhits) {
		HashSet<Ridge> filtered = new HashSet<Ridge>();
		int[] neighborCount = new int[ridges.size()];
		Frequency f = new Frequency();
		XYSeries xys = new XYSeries("Neighborhood Distribution", true, true);

		int rn = 0;
		// for (int i = 0; i <= radius; i++) {
		for (Ridge r : ridges) {
			Point2D p = getPointForRidge(r, spm);
			try {
				List<Tuple2D<Point2D, Ridge>> l = qr.getNeighborsInRadius(p,
				        radius);
				System.out.println("Point: " + p + " has " + l.size()
				        + " neighbors within radius " + radius);
				int cnt = 0;
				for (Tuple2D<Point2D, Ridge> tple : l) {
					if (Math.abs(p.getX() - tple.getFirst().getX()) > 0) {
						System.out
						        .println("Points are horizontal or diagonal neighbors: "
						                + p + " " + tple.getFirst());
						cnt++;
					} else {
						double av = r.getRidgePoints().get(0).getSecond();
						double bv = tple.getSecond().getRidgePoints().get(0)
						        .getSecond();
						double apen = r.getRidgeCost();
						double bpen = tple.getSecond().getRidgeCost();
						System.out.println("Points are vertical neighbors: "
						        + p + " " + tple.getFirst());
						System.out.println("Ridge a value: " + av
						        + " penalty: " + apen);
						System.out.println("Ridge b value: " + bv
						        + " penalty: " + bpen);
						if (av > bv) {
							filtered.add(r);
						} else if (av < bv) {
							filtered.remove(r);
							filtered.add(tple.getSecond());
						} else {
							if (apen < bpen) {
								filtered.add(r);
							} else if (apen > bpen) {
								filtered.remove(r);
								filtered.add(tple.getSecond());
							} else {
								System.out.println("Peaks are equal, adding!");
								filtered.add(r);
							}
						}
					}
				}
				neighborCount[rn++] += l.size();
				// neighborCount[rn++]+=cnt;

				// System.out.println("Point: "+p+" has "+cnt+" horizontal neighbors within radius "+radius);
				// Tuple2D<Point2D,Ridge> s = qr.getClosestInRadius(p, radius);
				// double d = Math.abs(p.getX()-s.getFirst().getX());
				// int idx = (int)Math.floor(d);
				// if (d <= radius) {
				// if () {
				// System.out.println("Found neighbor to " + p + " in radius " +
				// radius + " : " + s.getFirst());
				// }
				// f.addValue(idx);
				// }
				// if (l.size() <= maxhits) {
				// filtered.add(r);
				// }
			} catch (ElementNotFoundException enfe) {
			}

			// }
		}
		// for(int i = 0;i<neighborCount.length;i++) {
		// xys.add(i, neighborCount);
		// }
		// }
		// for (int i = 0; i < (int)Math.ceil(radius); i++) {
		// System.out.println("Frequency for d<=" + (i+1) + "=" +
		// f.getCount(i));
		// }

		return new ArrayList<Ridge>(filtered);
	}

	@Override
	public List<Point> findPeaks(IFileFragment ffO) {
		this.inputFile = ffO.getAbsolutePath();
		run();
		List<Point> l = new ArrayList<Point>(ridgeTree.size());
		Iterator<Tuple2D<Point2D,Ridge>> iter = ridgeTree.iterator();
		while (iter.hasNext()) {
			Ridge r = iter.next().getSecond();
			Point2D p2 = r.getRidgePoints().get(0).getFirst();
			l.add(new Point((int) p2.getX(), (int) p2.getY()));
		}
		return l;
	}

	@Override
	public List<Point> findPeaksNear(IFileFragment ff, Point p, int dx, int dy) {
		if (this.ridgeTree == null) {
			findPeaks(ff);
		}
		List<Tuple2D<Point2D, Ridge>> l = ridgeTree.getNeighborsInRadius(p,
		        Math.max(dx, dy));
		List<Point> list = new ArrayList<Point>(l.size());
		for (Tuple2D<Point2D, Ridge> tpl : l) {
			Point2D p2 = tpl.getFirst();
			list.add(new Point((int) p2.getX(), (int) p2.getY()));
		}
		return list;
	}

	@Override
	public void configure(Configuration pc) {
		this.minScale = pc.getInt(getClass().getName() + ".minScale", 2);
		this.maxScale = pc.getInt(getClass().getName() + ".maxScale", 20);
		this.maxRidgeCost = pc.getDouble(
		        getClass().getName() + ".maxRidgeCost", 0.2);
		this.minRelativeIntensity = pc.getDouble(getClass().getName()
		        + ".minRelativeIntensity", 0.000003);
		this.inputFile = pc.getString(getClass().getName() + ".inputFile");
		this.outputDir = pc.getString(getClass().getName() + ".outputDir");
		this.modulationTime = pc.getDouble("var.modulation_time.default", 5.0d);
		this.scanRate = pc.getDouble("var.scan_rate.default", 100.0d);
	}
}
