/**
 * 
 */
package maltcms.commands.fragments2d.peakfinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import ucar.ma2.ArrayDouble;
import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.io.IDataSourceFactory;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Slf4j
@Data
public class CWTPeakFinder extends AFragmentCommand {

	@Configurable
	private int minScale = 5;
	@Configurable
	private int maxScale = 20;
	@Configurable
	private double maxRidgeCost = 0.2;
	@Configurable
	private double minRelativeIntensity = 0.000002;
	@Configurable
	private String rmiServerConfigFile = "";

	// public List<Point> getMaxima(double[] d, int x) {
	// LinkedList<Point> l = new LinkedList<Point>();
	// for (int i = 0; i < d.length; i++) {
	// if (MathTools.max(d, i - 1, i + 1) == d[i]) {
	// Point p = new Point(x, i);
	// l.add(p);
	// }
	// }
	// return l;
	// }
	// private List<XYAnnotation> getPeaksFor(double[] d, int row) {
	// LinkedList<XYAnnotation> ll = new LinkedList<XYAnnotation>();
	// for (int i = 0; i < d.length; i++) {
	// if (MathTools.max(d, i - 1, i + 1) == d[i]) {
	// XYBoxAnnotation xyb = new XYBoxAnnotation(i, row - 0.5, i,
	// row + 0.5, new BasicStroke(1.0f), Color.RED, Color.RED);
	// ll.add(xyb);
	// }
	// }
	// return ll;
	// }
	// private List<XYAnnotation> getPeaksFor(double[] d) {
	// LinkedList<XYAnnotation> ll = new LinkedList<XYAnnotation>();
	// double min = MathTools.min(d);
	// double max = MathTools.max(d);
	// for (int i = 0; i < d.length; i++) {
	// if (MathTools.max(d, i - 1, i + 1) == d[i]) {
	// XYBoxAnnotation xyb = new XYBoxAnnotation(i - 0.2, d[i],
	// i + 0.2, d[i], new BasicStroke(1.0f), Color.RED,
	// Color.RED);
	// ll.add(xyb);
	// }
	// }
	// return ll;
	// }
	// /**
	// * @param dxyd
	// * @param origAnn
	// * @param origGaussAnn
	// * @param annotations
	// * @param categories
	// * @param points
	// */
	// private void showCharts(DefaultXYDataset dxyd, List<XYAnnotation>
	// origAnn,
	// List<XYAnnotation> origGaussAnn,
	// List<List<XYAnnotation>> annotations, List<String> categories,
	// List<double[]> points) {
	// ArrayDouble.D1 vals = toArrayDouble(points);
	// MinMax mm = MAMath.getMinMax(vals);
	// XYBlockRenderer xybr = new XYBlockRenderer();
	// xybr.setBaseOutlinePaint(Color.RED);
	// xybr.setPaintScale(new GrayPaintScale(mm.min, mm.max));
	// xybr.setSeriesToolTipGenerator(0, new XYToolTipGenerator() {
	//
	// @Override
	// public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
	// // System.out.println("Generating label");
	// if (arg0 instanceof XYZDataset) {
	// XYZDataset xyz = (XYZDataset) arg0;
	// StringBuilder sb = new StringBuilder();
	// sb.append("x: " + xyz.getXValue(arg1, arg2) + " y: "
	// + xyz.getYValue(arg1, arg2) + " z: "
	// + xyz.getZValue(arg1, arg2));
	// return sb.toString();
	// }
	// return null;
	// }
	// });
	//
	// // xybr.setPaintScale(new GradientPaintScale(ImageTools
	// // .createSampleTable(256), ImageTools.getBreakpoints(vals,
	// // 256, Double.NEGATIVE_INFINITY), "res/colorRamps/bcgyr.csv",
	// // mm.min, mm.max));
	// xybr.setBlockHeight(1);
	// xybr.setBlockWidth(1);
	// XYPlot xyp = new XYPlot(JFreeChartTools.getXYZDataset(points,
	// "Wavelet scales"), new NumberAxis("time"), new SymbolAxis(
	// "scale", categories.toArray(new String[] {})), xybr);
	// for (List<XYAnnotation> l : annotations) {
	// for (XYAnnotation xya : l) {
	// xyp.addAnnotation(xya, false);
	// }
	// }
	// xyp.notifyListeners(new PlotChangeEvent(xyp));
	// XYLineAndShapeRenderer xylas = new XYLineAndShapeRenderer(true, false);
	// for (XYAnnotation xya : origAnn) {
	// xylas.addAnnotation(xya);
	// }
	// for (XYAnnotation xya : origGaussAnn) {
	// xylas.addAnnotation(xya);
	// }
	// Color[] c = new Color[] { Color.RED.darker(), Color.GREEN.darker(),
	// Color.BLUE.brighter(), Color.ORANGE.darker() };
	// for (int i = 0; i < dxyd.getSeriesCount(); i++) {
	// xylas.setSeriesPaint(i, c[i]);
	// }
	// XYPlot xyo = new XYPlot(dxyd, xyp.getDomainAxis(), new NumberAxis(
	// "value"), xylas);
	// xyo.getRangeAxis().setInverted(true);
	// CombinedDomainXYPlot xdp = new CombinedDomainXYPlot(xyp.getDomainAxis());
	// xdp.add(xyp, 80);
	// xdp.add(xyo, 20);
	// JFreeChart jfc = new JFreeChart(xdp);
	// JFrame jf = new JFrame();
	// jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	// jf.add(new ChartPanel(jfc));
	// jf.pack();
	// jf.setVisible(true);
	// // try {
	// // Thread.sleep(5000);
	// // } catch (InterruptedException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	// }
	private ArrayDouble.D1 toArrayDouble(List<double[]> l) {
		ArrayDouble.D1 a = new ArrayDouble.D1(l.size());
		int i = 0;
		for (double[] d : l) {
			a.set(i++, d[2]);
		}
		return a;
	}

	// private List<Tuple2D<Point2D, Double>> findDiffs(double[] seedlings,
	// double[] newSeedlings, int scaleIndx) {
	// List<Tuple2D<Point2D, Double>> l = new LinkedList<Tuple2D<Point2D,
	// Double>>();
	//
	// return l;
	// }
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("No arguments given!");
			System.exit(1);
		}
		Factory fac = Factory.getInstance();
		fac.getConfiguration().setProperty("var.modulation_time.default", 5.0d);
		fac.getConfiguration().setProperty("var.scan_rate.default", 50.0d);
		IDataSourceFactory dsf = fac.getDataSourceFactory();
		dsf.setDataSources(Arrays
				.asList(new String[] { "maltcms.io.andims.NetcdfDataSource" }));
		List<File> inputfiles = new LinkedList<File>();
		for (String s : args) {
			File f = new File(s);
			if (f.getName().startsWith("*")) {
				final String ext = StringTools.getFileExtension(f.getName());
				String[] files = f.getParentFile().list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(ext);
					}
				});
				for (String str : files) {
					inputfiles.add(new File(f.getParentFile(), str));
				}

				System.out.println(Arrays.toString(files));
				// final List<Scan2D> scans = new ArrayList<Scan2D>();

			} else {
				inputfiles.add(f);
			}
			// CWTPeakFinder cwt = new CWTPeakFinder();
			// cwt.apply(new TupleND<IFileFragment>(new FileFragment(new
			// File(
			// s))));
		}
		CWTPeakFinder cwt = new CWTPeakFinder();
		long start = System.currentTimeMillis();
		TupleND<IFileFragment> t = new TupleND<IFileFragment>();
		for (File f : inputfiles) {
			t.add(new FileFragment(f));
			System.out.println("Adding file: " + f);
		}
		cwt.apply(t);
		start = System.currentTimeMillis() - start;
		System.out.println("Runtime total: " + (start / 1000)
				+ " s, avg. per file: " + (start / t.size()) / 1000.0d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.fragments.AFragmentCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Finds peak locations and extents in intensity profiles using the continuous wavelet transform.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
		// MasterServerFactory msf = new MasterServerFactory();
		// Impaxs ms = msf.getMasterServerImplementations().get(0);
		// ms.startMasterServer(this.rmiServerConfigFile);
		// JobMonitor jm = new JobMonitor(ms,t.size());
		// ms.addJobEventListener(jm);
		// ExecutorService es = Executors.newSingleThreadExecutor();
		int cnt = 0;
		for (IFileFragment f : t) {
			CWTRunnable cwt = new CWTRunnable();
			System.out.println("Opening file: " + f);
			// create ConfigurableRunnable config
			File runtimeConfig = createRuntimeConfiguration(cnt, f, cwt);

			// create job config
			File jobConfig = createJobConfiguration(runtimeConfig, cnt);
			// try {
			// Job j = new Job(jobConfig.getAbsolutePath());
			cwt.configure(runtimeConfig);
			cwt.run();
			// jm.addJob(j.getId());
			// ms.submitJob(j);
			// } catch (MalformedURLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (ClassNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (InstantiationException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IllegalAccessException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			cnt++;
		}
		// es.submit(jm);
		// es.shutdown();
		// try {
		// es.awaitTermination(20, TimeUnit.MINUTES);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		return t;
	}

	/**
	 * @param cnt
	 * @param f
	 * @param cwt
	 * @return
	 */
	private File createRuntimeConfiguration(int cnt, IFileFragment f,
			CWTRunnable cwt) {
		PropertiesConfiguration pc = new PropertiesConfiguration();
		pc.setProperty(cwt.getClass().getName() + ".minScale", this.minScale);
		pc.setProperty(cwt.getClass().getName() + ".maxScale", this.maxScale);
		pc.setProperty(cwt.getClass().getName() + ".maxRidgeCost",
				this.maxRidgeCost);
		pc.setProperty(cwt.getClass().getName() + ".minRelativeIntensity",
				this.minRelativeIntensity);
		pc.setProperty(cwt.getClass().getName() + ".inputFile",
				f.getAbsolutePath());
		if (getWorkflow() == null) {
			pc.setProperty(cwt.getClass().getName() + ".outputDir", getClass()
					.getName());
		} else {
			pc.setProperty(cwt.getClass().getName() + ".outputDir",
					getWorkflow().getOutputDirectory(this));
		}
		File configOutput = new File(pc.getString(cwt.getClass().getName()
				+ ".outputDir"), cwt.getClass().getName() + "_" + cnt
				+ ".properties");
		if (!configOutput.getParentFile().exists()) {
			configOutput.getParentFile().mkdirs();
		}
		pc.setProperty("var.modulation_time.default", 5.0d);
		pc.setProperty("var.scan_rate.default", 100.0d);
		try {
			ConfigurationUtils.dump(pc, new PrintStream(configOutput));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return configOutput;
	}

	private File createJobConfiguration(File configFile, int cnt) {
		PropertiesConfiguration pc = new PropertiesConfiguration();
		pc.setProperty("JAR_PATH",
				"/vol/maltcms/codebase/chroma4DCWTPeakfinder.jar");
		pc.setProperty("STARTUP_CLASS", CWTRunnable.class.getName());
		pc.setProperty("CONFIGURATION_FILE", configFile.getAbsolutePath());
		File outf = new File(configFile.getParentFile(), "job-" + cnt
				+ "-cfg.txt");
		try {
			ConfigurationUtils.dump(pc, new PrintStream(outf));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outf;
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

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.minScale = cfg.getInt(getClass().getName() + ".minScale", 2);
		this.maxScale = cfg.getInt(getClass().getName() + ".maxScale", 20);
		this.maxRidgeCost = cfg.getDouble(getClass().getName()
				+ ".maxRidgeCost", 0.2);
		this.minRelativeIntensity = cfg.getDouble(getClass().getName()
				+ ".minRelativeIntensity", 0.000003);
		this.rmiServerConfigFile = cfg.getString(getClass().getName()
				+ ".rmiServerConfigFile");
	}
}
