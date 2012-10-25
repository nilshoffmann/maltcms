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
package maltcms.commands.fragments2d.peakfinding.cwt;

import java.io.File;
import java.util.List;

import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class CwtPeakFinder extends AFragmentCommand {

    @Configurable
    private int minScale = 5;
    @Configurable
    private int maxScale = 20;
    @Configurable
    private double maxRidgeCost = 0.2;
    @Configurable
    private double minRelativeIntensity = 0.000002;
    @Configurable
    private double modulationTime = 5.0;
    @Configurable
    private double scanRate = 100.0d;

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
//    private ArrayDouble.D1 toArrayDouble(List<double[]> l) {
//        ArrayDouble.D1 a = new ArrayDouble.D1(l.size());
//        int i = 0;
//        for (double[] d : l) {
//            a.set(i++, d[2]);
//        }
//        return a;
//    }
    // private List<Tuple2D<Point2D, Double>> findDiffs(double[] seedlings,
    // double[] newSeedlings, int scaleIndx) {
    // List<Tuple2D<Point2D, Double>> l = new LinkedList<Tuple2D<Point2D,
    // Double>>();
    //
    // return l;
    // }
//    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.err.println("No arguments given!");
//            System.exit(1);
//        }
//        Factory fac = Factory.getInstance();
//        fac.getConfiguration().setProperty("var.modulation_time.default", 5.0d);
//        fac.getConfiguration().setProperty("var.scan_rate.default", 50.0d);
//        IDataSourceFactory dsf = fac.getDataSourceFactory();
//        dsf.setDataSources(Arrays.asList(new String[]{
//                    "maltcms.io.andims.NetcdfDataSource"}));
//        List<File> inputfiles = new LinkedList<File>();
//        for (String s : args) {
//            File f = new File(s);
//            if (f.getName().startsWith("*")) {
//                final String ext = StringTools.getFileExtension(f.getName());
//                String[] files = f.getParentFile().list(new FilenameFilter() {
//
//                    @Override
//                    public boolean accept(File dir, String name) {
//                        return name.endsWith(ext);
//                    }
//                });
//                for (String str : files) {
//                    inputfiles.add(new File(f.getParentFile(), str));
//                }
//
//                System.out.println(Arrays.toString(files));
//                // final List<Scan2D> scans = new ArrayList<Scan2D>();
//
//            } else {
//                inputfiles.add(f);
//            }
//            // CwtPeakFinder cwt = new CwtPeakFinder();
//            // cwt.apply(new TupleND<IFileFragment>(new FileFragment(new
//            // File(
//            // s))));
//        }
//        CwtPeakFinder cwt = new CwtPeakFinder();
//        long start = System.currentTimeMillis();
//        TupleND<IFileFragment> t = new TupleND<IFileFragment>();
//        for (File f : inputfiles) {
//            t.add(new FileFragment(f));
//            System.out.println("Adding file: " + f);
//        }
//        cwt.apply(t);
//        start = System.currentTimeMillis() - start;
//        System.out.println("Runtime total: " + (start / 1000)
//                + " s, avg. per file: " + (start / t.size()) / 1000.0d);
//    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    /**
     *
     * @return
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
        ICompletionService<File> ics = createCompletionService(File.class);
        int cnt = 0;
        for (IFileFragment f : t) {
            CwtRunnable cwt = new CwtRunnable();
            cwt.setMaxRidgeCost(maxRidgeCost);
            cwt.setMaxScale(maxScale);
            cwt.setMinRelativeIntensity(minRelativeIntensity);
            cwt.setMinScale(minScale);
            cwt.setModulationTime(modulationTime);
            cwt.setScanRate(scanRate);
            cwt.setInputFile(new File(f.getAbsolutePath()));
            log.info("Running cwt peak finder");
            ics.submit(cwt);
            cnt++;
        }
        try {
            List<File> results = ics.call();
        } catch (Exception ex) {
            log.warn("Exception while waiting for results!", ex);
        }


        return t;
    }

//    /**
//     * @param cnt
//     * @param f
//     * @param cwt
//     * @return
//     */
//    private File createRuntimeConfiguration(int cnt, IFileFragment f,
//            CwtRunnable cwt) {
//        PropertiesConfiguration pc = new PropertiesConfiguration();
//        pc.setProperty(cwt.getClass().getName() + ".minScale", this.minScale);
//        pc.setProperty(cwt.getClass().getName() + ".maxScale", this.maxScale);
//        pc.setProperty(cwt.getClass().getName() + ".maxRidgeCost",
//                this.maxRidgeCost);
//        pc.setProperty(cwt.getClass().getName() + ".minRelativeIntensity",
//                this.minRelativeIntensity);
//        pc.setProperty(cwt.getClass().getName() + ".inputFile",
//                f.getAbsolutePath());
//        if (getWorkflow() == null) {
//            pc.setProperty(cwt.getClass().getName() + ".outputDir", getClass().
//                    getName());
//        } else {
//            pc.setProperty(cwt.getClass().getName() + ".outputDir",
//                    getWorkflow().getOutputDirectory(this));
//        }
//        File configOutput = new File(pc.getString(cwt.getClass().getName()
//                + ".outputDir"), cwt.getClass().getName() + "_" + cnt
//                + ".properties");
//        if (!configOutput.getParentFile().exists()) {
//            configOutput.getParentFile().mkdirs();
//        }
//        pc.setProperty("var.modulation_time.default", 5.0d);
//        pc.setProperty("var.scan_rate.default", 100.0d);
//        try {
//            ConfigurationUtils.dump(pc, new PrintStream(configOutput));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return configOutput;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /**
     *
     * @return
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.PEAKFINDING;
    }
}