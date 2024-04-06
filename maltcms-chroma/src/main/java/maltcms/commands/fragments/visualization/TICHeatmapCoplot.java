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
package maltcms.commands.fragments.visualization;

import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import lombok.Data;

import maltcms.datastructures.ms.IAnchor;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ArrayTools;
import maltcms.tools.ImageTools;
import maltcms.tools.MaltcmsTools;
import maltcms.ui.charts.GradientPaintScale;
import maltcms.ui.charts.PlotRunner;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

/**
 * <p>TICHeatmapCoplot class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@RequiresVariables(names = {"var.total_intensity", "var.scan_acquisition_time"})
@RequiresOptionalVariables(names = {"var.anchors.retention_index_names",
    "var.anchors.retention_times", "var.anchors.retention_indices",
    "var.anchors.retention_scans"})
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class TICHeatmapCoplot extends AFragmentCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TICHeatmapCoplot.class);

    private final String description = "Generates a stacked heatmap plot of TICs (bird's eye view) with shared time axis and a coplot with a shared intensity axis (overlay)";
    private final WorkflowSlot workflowSlot = WorkflowSlot.VISUALIZATION;
    @Configurable(name = "images.colorramp", description="The location of the "
            + "color ramp to use for plotting.")
    private String colorRampLocation = "res/colorRamps/bcgyr.csv";
    @Configurable(name = "var.total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time")
    private String scanAcquisitionTimeVar = "scan_acquisition_time";

    /** {@inheritDoc} */
    @Override
    public void configure(Configuration cfg) {
        this.totalIntensityVar = cfg.getString("var.total_intensity");
        this.scanAcquisitionTimeVar = cfg.getString("var.scan_acquisition_time");
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        final File jfplot = drawTICSJFreeChart(getWorkflow().getOutputDirectory(
                this), t, totalIntensityVar,
                scanAcquisitionTimeVar, null, "tics-jfreechart.png");
        final DefaultWorkflowResult dwrut1 = new DefaultWorkflowResult(jfplot,
                this, getWorkflowSlot(), t.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwrut1);
        final File utics = drawTICS(getWorkflow().getOutputDirectory(this), t,
                totalIntensityVar, scanAcquisitionTimeVar, null, "tics.png");
        final DefaultWorkflowResult dwrut = new DefaultWorkflowResult(utics,
                this, getWorkflowSlot(), t.toArray(new IFileFragment[]{}));
        getWorkflow().append(dwrut);
        return t;
    }

    /**
     * <p>drawTICSJFreeChart.</p>
     *
     * @param outputDir a {@link java.io.File} object.
     * @param t
     * @param ticvar
     * @param ticvar a {@link java.lang.String} object.
     * @param satVar a {@link java.lang.String} object.
     * @param ref a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param filename a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    protected File drawTICSJFreeChart(final File outputDir,
            final TupleND<IFileFragment> t, final String ticvar,
            final String satVar, final IFileFragment ref, final String filename) {

        DefaultXYZDataset cd = new DefaultXYZDataset();
        int rowIdx = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double minRT = Double.POSITIVE_INFINITY;
        double maxRT = Double.NEGATIVE_INFINITY;
        // FIXME this can be very memory consuming
        int length = 0;
        TreeMap<IFileFragment, TreeMap<Double, Double>> fragToVals = new TreeMap<>(
                new Comparator<IFileFragment>() {
                    @Override
                    public int compare(IFileFragment o1, IFileFragment o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
        for (IFileFragment f : t) {
            double[] sat = (double[]) f.getChild(satVar).getArray().
                    get1DJavaArray(double.class);
            double[] tic = (double[]) f.getChild(ticvar).getArray().
                    get1DJavaArray(double.class);
            TreeMap<Double, Double> satToIntensity = new TreeMap<>();
            for (int i = 0; i < Math.min(sat.length, tic.length); i++) {
                if (satToIntensity.containsKey(sat[i])) {
                    satToIntensity.put(sat[i], satToIntensity.get(sat[i])
                            + tic[i]);
                } else {
                    satToIntensity.put(sat[i], tic[i]);
                }
            }
            fragToVals.put(f, satToIntensity);
            length += satToIntensity.size();
        }
        double[][] data = new double[3][length];
        int k = 0;
        for (IFileFragment f : fragToVals.keySet()) {
            TreeMap<Double, Double> satToVals = fragToVals.get(f);
            for (Double sat : satToVals.keySet()) {
                data[0][k] = sat;
                minRT = Math.min(minRT, sat);
                maxRT = Math.max(maxRT, sat);
                data[1][k] = (double) rowIdx;
                data[2][k] = satToVals.get(sat);
                min = Math.min(min, data[2][k]);
                max = Math.max(max, data[2][k]);
                k++;
                // log.info("Adding "+i+" "+d+" "+dtm.getColumnName(j));
                // cd.addSeries(xys);
            }
            rowIdx++;
        }
        cd.addSeries("heatmap", data);
        // ArrayDouble.D1 a = new ArrayDouble.D1(npoints);
        // int offset = 0;
        // for (IFileFragment f : t) {
        // Array tic = f.getChild(ticvar).getArray();
        // int len = tic.getShape()[0];
        // Array.arraycopy(tic, 0, a, offset, len);
        // offset += len;
        // }
        // histogram with fixed binsize
        // fill intensities into adequate bin, raise count in bin by one
        // afterwards, relative frequency within a bin gives a normalization
        // coefficient
        XYBlockRenderer xyb = new XYBlockRenderer();
        GradientPaintScale ps = new GradientPaintScale(
                ImageTools.createSampleTable(256), min, max,
                ImageTools.rampToColorArray(new ColorRampReader().readColorRamp(
                                this.colorRampLocation)));

        xyb.setPaintScale(ps);
        final String[] colnames = new String[t.getSize()];
        for (int i = 0; i < colnames.length; i++) {
            colnames[i] = StringTools.removeFileExt(t.get(i).getName());
        }
        NumberAxis na = new NumberAxis("retention time");
        na.setAutoRange(false);
        na.setLowerBound(minRT);
        na.setUpperBound(maxRT);
        na.setLowerMargin(0);
        na.setUpperMargin(10);
        // na.setVerticalTickLabels(true);
        XYPlot xyp = new XYPlot(cd, na, new SymbolAxis("file", colnames), xyb);
        xyb.setBlockWidth(1);
        xyb.setSeriesToolTipGenerator(0, new XYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset xyd, int i, int i1) {
                return "[" + colnames[xyd.getX(i, i1).intValue()] + ":"
                        + colnames[xyd.getY(i, i1).intValue()] + "] = "
                        + ((XYZDataset) xyd).getZValue(i, i1) + "";
            }
        });
        PlotRunner pr = new PlotRunner(xyp, "TIC Plot", filename, outputDir);
        pr.configure(Factory.getInstance().getConfiguration());
        pr.setImgheight((t.size() * 10) + 200);
        // pr.setImgwidth(npoints / t.size() + 100);
        File f = pr.getFile();
        try {
            pr.call();
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
        }
//        Factory.getInstance().submitJob(pr);
        return f;
    }

    private File drawTIC(final File outputDir, final int heightPerTIC,
            final int maxLength, final ArrayList<Tuple2D<String, Array>> a,
            final String filename, final Array scan_acquisition_time,
            final HashMap<String, Array> anchors,
            final HashMap<String, List<String>> anchorNames) {
        // string length per row
        final int fontsize = heightPerTIC * 2 / 3;
        Font f = new Font("Lucida Sans", Font.PLAIN, fontsize);
        final int maxStringLength = ImageTools.getMaxStringLengthForTuple(a, f);
        final Font f2 = new Font("Lucida Sans", Font.PLAIN, heightPerTIC / 3);
        // create BufferedImage for plot, add 2*heightPerTIC for labels
        final BufferedImage bi = new BufferedImage(maxLength + maxStringLength,
                (heightPerTIC * a.size()) + (2 * heightPerTIC),
                BufferedImage.TYPE_INT_RGB);
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(this.colorRampLocation);
        // Sort Arrays according to lexicographical order of names
        Collections.sort(a, new Comparator<Tuple2D<String, Array>>() {
            @Override
            public int compare(final Tuple2D<String, Array> o1,
                    final Tuple2D<String, Array> o2) {
                return o1.getFirst().compareTo(o2.getFirst());
            }
        });

        int nelements = 0;
        for (final Tuple2D<String, Array> t : a) {
            nelements += t.getSecond().getShape()[0];
        }
        final ArrayDouble.D1 arr = new ArrayDouble.D1(nelements);
        int start = 0;
        for (final Tuple2D<String, Array> t : a) {
            final IndexIterator ii = t.getSecond().getIndexIterator();
            while (ii.hasNext()) {
                arr.set(start++, ii.getDoubleNext());
            }
        }
        EvalTools.eqI(start, nelements, ImageTools.class);

        final double[] breakpoints = ImageTools.getBreakpoints(arr, 1024,
                Double.POSITIVE_INFINITY);

        // for each array, draw a line of height heightPerTIC
        for (int i = 0; i < a.size(); i++) {
            final String name = a.get(i).getFirst();
            final BufferedImage tic = bi.getSubimage(maxStringLength, i
                    * heightPerTIC, maxLength, heightPerTIC);
            final ArrayList<Array> al = new ArrayList<>(a.get(i).getSecond().
                    getShape()[0]);
            final ucar.ma2.Index idx = a.get(i).getSecond().getIndex();
            for (int j = 0; j < a.get(i).getSecond().getShape()[0]; j++) {
                final ArrayInt.D1 b = new ArrayInt.D1(heightPerTIC, false);
                ArrayTools.fill(b, a.get(i).getSecond().getDouble(idx.set(j)));
                al.add(b);
            }
            ImageTools.makeImage(tic.getRaster(), al, 1024, colorRamp, 0.0d,
                    breakpoints);
            final Color c = tic.getGraphics().getColor();
            String aname = name;
            if (name.contains(" *")) {
                aname = name.substring(0, name.indexOf(" *"));
            }
            // add anchor cross.annotations
            if (anchors.containsKey(aname)) {
                int j = 0;
                final List<String> anchorNamesList = anchorNames.get(aname);
                final IndexIterator iter = anchors.get(aname).getIndexIterator();
                tic.getGraphics().setColor(Color.WHITE);
                while (iter.hasNext()) {
                    final int scan = iter.getIntNext();
                    final int width = heightPerTIC / 4;
                    final int height = width;
                    tic.getGraphics().drawOval(scan - (width / 2),
                            (heightPerTIC / 2) - (width / 2), width, height);
                    final TextLayout tl = new TextLayout(
                            anchorNamesList.get(j++), f2,
                            ((Graphics2D) tic.getGraphics()).
                            getFontRenderContext());
                    tl.draw((Graphics2D) tic.getGraphics(), scan
                            - (float) (tl.getBounds().getWidth() / 2),
                            tl.getAscent());
                }
            } else {
                log.warn("No anchor annotation present for {}", aname);
            }
            tic.getGraphics().setColor(c);
            final BufferedImage label = bi.getSubimage(0, i * heightPerTIC,
                    maxStringLength, heightPerTIC);
            final Graphics2D g = label.createGraphics();
            g.setColor(Color.WHITE);
            g.setFont(f);
            final TextLayout tl = new TextLayout(name, f,
                    g.getFontRenderContext());
            tl.draw(g, 0, tl.getAscent());
        }

        // draw ticks
        BufferedImage tic = bi.getSubimage(0, a.size() * heightPerTIC,
                maxLength + maxStringLength, heightPerTIC);
        Graphics2D g = tic.createGraphics();
        g.setColor(Color.white);
        f = new Font("Lucida Sans", Font.PLAIN, fontsize / 2);
        final int tickInterval = Factory.getInstance().getConfiguration().getInt(ImageTools.class.
                getName() + ".tickInterval", 250);
        TextLayout tl = new TextLayout("index", f, g.getFontRenderContext());
        tl.draw(g,
                (maxStringLength / 2)
                - (int) (Math.ceil(tl.getBounds().getWidth() / 2.0)),
                (heightPerTIC / 2) + tl.getAscent());
        for (int i = 0; i < maxLength; i++) {
            if (i % tickInterval == 0) {
                g.drawLine(maxStringLength + i, 0, maxStringLength + i,
                        heightPerTIC / 2);
                tl = new TextLayout((i) + "", f, g.getFontRenderContext());
                tl.draw(g,
                        maxStringLength
                        + i
                        - (int) (Math.rint(tl.getBounds().getWidth() / 2.0d)),
                        (heightPerTIC / 2) + tl.getAscent());
            }
        }
        tic = bi.getSubimage(0, (a.size() + 1) * heightPerTIC, maxLength
                + maxStringLength, heightPerTIC);
        g = tic.createGraphics();
        tl = new TextLayout("time [min]", f, g.getFontRenderContext());
        tl.draw(g,
                (maxStringLength / 2)
                - (int) (Math.ceil(tl.getBounds().getWidth() / 2.0)),
                (heightPerTIC / 2) + tl.getAscent());
        final Array sat = ArrayTools.divBy60(scan_acquisition_time);
        final Index satI = sat.getIndex();
        final int timeInterval = Factory.getInstance().getConfiguration().getInt(ImageTools.class.
                getName() + ".timeInterval", 100);
        for (int i = 0; i < maxLength; i++) {
            final double value = (sat.getDouble(satI.set(i)));
            if (i % timeInterval == 0) {
                g.drawLine(maxStringLength + i, 0, maxStringLength + i,
                        heightPerTIC / 2);
                tl = new TextLayout(String.format("%.2f", value), f,
                        g.getFontRenderContext());
                tl.draw(g,
                        maxStringLength
                        + i
                        - (int) (Math.rint(tl.getBounds().getWidth() / 2.0d)),
                        (heightPerTIC / 2) + tl.getAscent());
            }
        }
        File out = null;
        // final File d = FileTools.prependDefaultDirs(creator, date);
        out = new File(outputDir, filename);
        try {
            log.info("Saving image to " + out.getAbsolutePath());

            ImageIO.write(bi, "png", out);
            return out;
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * <p>drawTICS.</p>
     *
     * @param outputDir a {@link java.io.File} object.
     * @param t
     * @param ticvar
     * @param ticvar a {@link java.lang.String} object.
     * @param satVar a {@link java.lang.String} object.
     * @param ref a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param filename a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    protected File drawTICS(final File outputDir,
            final TupleND<IFileFragment> t, final String ticvar,
            final String satVar, final IFileFragment ref, final String filename) {
        final int heightPerTIC = 50;
        int maxLength = 0;
        // Map of Names to projected anchors
        final HashMap<String, Array> anchors = new HashMap<>();
        final HashMap<String, List<String>> anchorNames = new HashMap<>();
        final ArrayList<Tuple2D<String, Array>> a = new ArrayList<>(
                t.size());
        int k = 0;
        for (int i = 0; i < t.size(); i++) {
            if ((ref != null) && ref.getName().equals(t.get(i).getName())) {
                a.add(new Tuple2D<>(StringTools.removeFileExt(t.get(
                        i).getName()) + " *",
                        t.get(i).getChild(ticvar).getArray()));
            } else {
                a.add(new Tuple2D<>(StringTools.removeFileExt(t.get(
                        i).getName()), t.get(i).getChild(ticvar).getArray()));
            }
            if (a.get(i).getSecond().getShape()[0] > maxLength) {
                maxLength = a.get(i).getSecond().getShape()[0];
                k = i;
            }
            final List<IAnchor> l1 = MaltcmsTools.prepareAnchors(t.get(i));
            final ArrayInt.D1 anchPos1 = new ArrayInt.D1(l1.size(), false);
            final List<String> anchNames = new ArrayList<>(l1.size());
            for (int j = 0; j < l1.size(); j++) {
                final int anchor = l1.get(j).getScanIndex();
                anchPos1.set(j, anchor);
                anchNames.add(l1.get(j).getName());
            }
            anchors.put(StringTools.removeFileExt(t.get(i).getName()), anchPos1);
            anchorNames.put(StringTools.removeFileExt(t.get(i).getName()),
                    anchNames);
        }
        final Array sat = ref == null ? t.get(k).getChild(satVar).getArray()
                : ref.getChild(satVar).getArray();
        return drawTIC(outputDir, heightPerTIC, maxLength, a, filename, sat,
                anchors, anchorNames);
    }
}
