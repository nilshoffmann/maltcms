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
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.quadTree.QuadTree;
import maltcms.datastructures.ridge.IRidgeCost;
import maltcms.datastructures.ridge.Ridge;
import maltcms.io.csv.ColorRampReader;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.GradientPaintScale;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import ucar.ma2.ArrayDouble;

/**
 * <p>CwtChartFactory class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
public class CwtChartFactory {

    /**
     * <p>createColorHeatmap.</p>
     *
     * @param array a {@link ucar.ma2.ArrayDouble.D2} object.
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    public static BufferedImage createColorHeatmap(ArrayDouble.D2 array) {
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
        // log.info("Sampletable: " +
        // Arrays.toString(sampleTable));
        BufferedImage crampImg = ImageTools.createColorRampImage(sampleTable,
                Transparency.TRANSLUCENT, cRamp);
        BufferedImage destImg = ImageTools.applyLut(bi,
                ImageTools.createLookupTable(crampImg, 1.0f, nsamples));
        return destImg;
        // return bi;
    }

    /**
     * <p>createAdaptiveColorHeatmap.</p>
     *
     * @param array a {@link ucar.ma2.ArrayDouble.D2} object.
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    public static BufferedImage createAdaptiveColorHeatmap(ArrayDouble.D2 array) {
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
        // log.info("Sampletable: " +
        // Arrays.toString(sampleTable));
        // BufferedImage crampImg = ImageTools.createColorRampImage(sampleTable,
        // Transparency.TRANSLUCENT, cRamp);
        // BufferedImage destImg = ImageTools.applyLut(bi, ImageTools
        // .createLookupTable(crampImg, 1.0f, nsamples));
        return bi;
        // return bi;
    }

    /**
     * <p>createCostHistogramDS.</p>
     *
     * @param nbins a int.
     * @param r a {@link maltcms.datastructures.ridge.Ridge} object.
     * @return a {@link java.util.List} object.
     */
    public static List<JFreeChart> createCostHistogramDS(int nbins, Ridge... r) {
        List<IRidgeCost> l = Ridge.getAvailableRidgeCostClasses();
        List<JFreeChart> dsl = new ArrayList<>(l.size());
        int k = 0;
        for (IRidgeCost irc : l) {
            HistogramDataset dxyzd = new HistogramDataset();
            double[] vals = new double[r.length];
            int v = 0;
            for (Ridge ridge : r) {
                double penalty = ridge.getRidgeCosts().get(k).getSecond();
                vals[v++] = penalty;
            }
            dxyzd.addSeries(irc.getClass().getName(), vals, nbins);
            JFreeChart jfc = ChartFactory.createHistogram("Cost histogram for "
                    + irc.getClass().getName(), "cost", "count", dxyzd,
                    PlotOrientation.VERTICAL, true, true, true);
            XYPlot xyp = jfc.getXYPlot();
            XYBarRenderer xybr = (XYBarRenderer) xyp.getRenderer();
            xybr.setShadowVisible(false);
            xybr.setBarAlignmentFactor(0);
            dsl.add(jfc);
            k++;
        }
        return dsl;
    }

    /**
     * <p>createNeighborhoodHistogramDS.</p>
     *
     * @param spm a int.
     * @param nbins a int.
     * @param qt a {@link maltcms.datastructures.quadTree.QuadTree} object.
     * @param radius a int.
     * @param r a {@link maltcms.datastructures.ridge.Ridge} object.
     * @return a {@link java.util.List} object.
     */
    public static List<JFreeChart> createNeighborhoodHistogramDS(int spm, int nbins,
            QuadTree<Ridge> qt, int radius, Ridge... r) {
        List<IRidgeCost> l = Ridge.getAvailableRidgeCostClasses();
        List<JFreeChart> dsl = new ArrayList<>(l.size());
        for (int k = 1; k <= radius; k++) {
            HistogramDataset dxyzd = new HistogramDataset();
            List<Double> vals = new ArrayList<>();
            int v = 0;
            for (Ridge ridge : r) {
                Point2D root = ridge.getRidgePoints().get(0).getFirst();
                double x = root.getX() / spm;
                double y = root.getX() % spm;
                double n = qt.getNeighborsInRadius(new Point2D.Double(x, y), k).size();
                if (n > 0) {
                    vals.add(n);
                }
                // vals[v++] = n;
            }
            double[] vs = new double[vals.size()];
            for (int i = 0; i < vals.size(); i++) {
                vs[i] = vals.get(i);
            }
            if (vs != null && vs.length > 0) {
                dxyzd.addSeries("Neighborhood histogram for r=" + k, vs, nbins);
                JFreeChart jfc = ChartFactory.createHistogram(
                        "Neighborhood histogram for radius " + k,
                        "neighborhood size", "count", dxyzd,
                        PlotOrientation.VERTICAL, true, true, true);
                XYPlot xyp = jfc.getXYPlot();
                XYBarRenderer xybr = (XYBarRenderer) xyp.getRenderer();
                xybr.setShadowVisible(false);
                xybr.setBarAlignmentFactor(0);
                dsl.add(jfc);
            }
        }
        return dsl;
    }

    /**
     * <p>create2DRidgeCostDS.</p>
     *
     * @param spm a int.
     * @param r a {@link maltcms.datastructures.ridge.Ridge} object.
     * @return a {@link java.util.List} object.
     */
    public static List<JFreeChart> create2DRidgeCostDS(int spm, Ridge... r) {
        List<IRidgeCost> l = Ridge.getAvailableRidgeCostClasses();
        List<JFreeChart> dsl = new ArrayList<>(l.size());
        int k = 0;
        for (IRidgeCost irc : l) {
            DefaultXYZDataset dxyzd = new DefaultXYZDataset();
            double[][] tic2ddata = new double[3][r.length];
            int cnt = 0;
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
            for (Ridge ridge : r) {
                Point2D root = ridge.getRidgePoints().get(0).getFirst();
                tic2ddata[0][cnt] = root.getX() / spm;
                tic2ddata[1][cnt] = root.getX() % spm;
                double v = ridge.getRidgeCosts().get(k).getSecond();
                min = Math.min(min, v);
                max = Math.max(max, v);
                tic2ddata[2][cnt] = v;
                cnt++;
            }
            dxyzd.addSeries(irc.getClass().getName(), tic2ddata);
            XYBlockRenderer xybr = new XYBlockRenderer();
            xybr.setBlockHeight(1.0d);
            xybr.setBlockWidth(1.0d);
            if (min < max) {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            } else {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            }
            XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
                    "y"), xybr);
            JFreeChart jfc = new JFreeChart(xyp);
            // jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
            // new NumberAxis("ridge cost of " + irc.getClass().getName())));
            dsl.add(jfc);
            k++;
        }
        return dsl;
    }

    /**
     * <p>create2DRidgeNeighborhoodDS.</p>
     *
     * @param spm a int.
     * @param qt a {@link maltcms.datastructures.quadTree.QuadTree} object.
     * @param radius a int.
     * @param r a {@link maltcms.datastructures.ridge.Ridge} object.
     * @return a {@link java.util.List} object.
     */
    public static List<JFreeChart> create2DRidgeNeighborhoodDS(int spm,
            QuadTree<Ridge> qt, int radius, Ridge... r) {
        List<JFreeChart> l = new ArrayList<>();
//		HashMap<Ridge, Integer> ridgeToPreviousNeighbors = new HashMap<Ridge, Integer>();
        for (int i = 1; i <= radius; i++) {
            DefaultXYZDataset dxyzd = new DefaultXYZDataset();
            double[][] tic2ddata = new double[3][r.length];
            int cnt = 0;
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
            for (Ridge ridge : r) {
                Point2D root = ridge.getRidgePoints().get(0).getFirst();
                tic2ddata[0][cnt] = root.getX() / spm;
                tic2ddata[1][cnt] = root.getX() % spm;
                double v = qt.getVerticalNeighborsInRadius(new Point2D.Double(tic2ddata[0][cnt], tic2ddata[1][cnt]), i).size();
//				if (ridgeToPreviousNeighbors.containsKey(ridge)) {
//					v = v - ridgeToPreviousNeighbors.get(ridge);
//				}
//				ridgeToPreviousNeighbors.put(ridge, (int) v);
                min = Math.min(min, v);
                max = Math.max(max, v);
                tic2ddata[2][cnt] = v;
                cnt++;
            }
            dxyzd.addSeries("vertical ridge neighborhood, radius=" + i, tic2ddata);
            XYBlockRenderer xybr = new XYBlockRenderer();
            xybr.setBlockHeight(1.0d);
            xybr.setBlockWidth(1.0d);
            if (min < max) {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            } else {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            }
            XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
                    "y"), xybr);
            JFreeChart jfc = new JFreeChart(
                    "2D Vertical Ridge Neighborhood difference for radius: " + i, xyp);
            // jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
            // new NumberAxis("neighbors in radius " + i)));
            l.add(jfc);
        }
        for (int i = 1; i <= radius; i++) {
            DefaultXYZDataset dxyzd = new DefaultXYZDataset();
            double[][] tic2ddata = new double[3][r.length];
            int cnt = 0;
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
            for (Ridge ridge : r) {
                Point2D root = ridge.getRidgePoints().get(0).getFirst();
                tic2ddata[0][cnt] = root.getX() / spm;
                tic2ddata[1][cnt] = root.getX() % spm;
                double v = qt.getHorizontalNeighborsInRadius(new Point2D.Double(tic2ddata[0][cnt], tic2ddata[1][cnt]), i).size();
//				if (ridgeToPreviousNeighbors.containsKey(ridge)) {
//					v = v - ridgeToPreviousNeighbors.get(ridge);
//				}
//				ridgeToPreviousNeighbors.put(ridge, (int) v);
                min = Math.min(min, v);
                max = Math.max(max, v);
                tic2ddata[2][cnt] = v;
                cnt++;
            }
            dxyzd.addSeries("horizontal ridge neighborhood, radius=" + i, tic2ddata);
            XYBlockRenderer xybr = new XYBlockRenderer();
            xybr.setBlockHeight(1.0d);
            xybr.setBlockWidth(1.0d);
            if (min < max) {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            } else {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            }
            XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
                    "y"), xybr);
            JFreeChart jfc = new JFreeChart(
                    "2D Horizontal Ridge Neighborhood difference for radius: " + i, xyp);
            // jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
            // new NumberAxis("neighbors in radius " + i)));
            l.add(jfc);
        }
        for (int i = 1; i <= radius; i++) {
            DefaultXYZDataset dxyzd = new DefaultXYZDataset();
            double[][] tic2ddata = new double[3][r.length];
            int cnt = 0;
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
            for (Ridge ridge : r) {
                Point2D root = ridge.getRidgePoints().get(0).getFirst();
                tic2ddata[0][cnt] = root.getX() / spm;
                tic2ddata[1][cnt] = root.getX() % spm;
                double v = qt.getNeighborsInRadius(new Point2D.Double(tic2ddata[0][cnt], tic2ddata[1][cnt]), i).size();
//				if (ridgeToPreviousNeighbors.containsKey(ridge)) {
//					v = v - ridgeToPreviousNeighbors.get(ridge);
//				}
//				ridgeToPreviousNeighbors.put(ridge, (int) v);
                min = Math.min(min, v);
                max = Math.max(max, v);
                tic2ddata[2][cnt] = v;
                cnt++;
            }
            dxyzd.addSeries("ridge neighborhood, radius=" + i, tic2ddata);
            XYBlockRenderer xybr = new XYBlockRenderer();
            xybr.setBlockHeight(1.0d);
            xybr.setBlockWidth(1.0d);
            if (min < max) {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            } else {
                xybr.setPaintScale(new GradientPaintScale(ImageTools.createSampleTable(256), min, max, new Color[]{Color.WHITE, Color.BLUE, Color.GREEN, Color.YELLOW, Color.orange, Color.red, Color.MAGENTA}));
            }
            XYPlot xyp = new XYPlot(dxyzd, new NumberAxis("x"), new NumberAxis(
                    "y"), xybr);
            JFreeChart jfc = new JFreeChart(
                    "2D Ridge Neighborhood difference for radius: " + i, xyp);
            // jfc.addSubtitle(new PaintScaleLegend(xybr.getPaintScale(),
            // new NumberAxis("neighbors in radius " + i)));
            l.add(jfc);
        }
        return l;
    }

    /**
     * <p>saveImages.</p>
     *
     * @param outputDirectory a {@link java.io.File} object.
     * @param prefix a {@link java.lang.String} object.
     * @param l a {@link java.util.List} object.
     */
    public static void saveImages(File outputDirectory, String prefix,
            List<JFreeChart> l) {
        int i = 0;
        for (JFreeChart jfc : l) {
            File image = new File(outputDirectory, prefix + "_" + i + ".png");
            try {
                ChartUtils.saveChartAsPNG(image, jfc, 1024, 768, null,
                        true, 0);
            } catch (IOException e) {
   
                log.warn(e.getLocalizedMessage());
            }
            i++;
        }
    }

    /**
     * <p>saveImages.</p>
     *
     * @param outputDirectory a {@link java.lang.String} object.
     * @param prefix a {@link java.lang.String} object.
     * @param l a {@link java.util.List} object.
     */
    public static void saveImages(String outputDirectory, String prefix,
            List<JFreeChart> l) {
        saveImages(new File(outputDirectory), prefix, l);
    }
}
