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
package maltcms.commands.fragments2d.warp.visualization;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.MinimumFilter;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.AChart;
import maltcms.ui.charts.XYBPlot;

import org.apache.commons.configuration.Configuration;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import cross.Logging;
import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Default visualizer for a 2d time warp. You can use it to create an image or
 * an {@link AChart}. You have the option to conserve the time axis of the
 * first, second, both or no chromatogram. Both, the image and the plot,
 * contains a differential color plot of the total intensity from both
 * chromatograms.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
public class Default2DTWVisualizer implements IVisualization {

    @Configurable(name = "var.peak_index_list", value = "peak_index_list")
    private String peakListVar = "peak_index_list";
    @Configurable(value = "true")
    private boolean holdi = true;
    @Configurable(value = "true")
    private boolean holdj = true;
    @Configurable(value = "false")
    private boolean globalmax = false;
    @Configurable(value = "true")
    private boolean black = true;
    @Configurable(value = "filer")
    private boolean filter = true;
    @Configurable(value = "true")
    private boolean normalize = true;
    @Configurable(value = "6.0d")
    private double threshold = 6.0d;
    @Configurable(value = "true")
    private boolean horizontal = true;
    private int currentrasterline = -1;
    private int binSize = 256;

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage createImage(List<Array> scanlinesi,
            List<Array> scanlinesj, final Array warpPathi, final Array warpPathj) {
        if (!this.horizontal) {
            scanlinesi = ArrayTools2.transpose(scanlinesi);
            scanlinesj = ArrayTools2.transpose(scanlinesj);
        }
        this.currentrasterline = -1;

        final Tuple2D<double[], Tuple2D<double[], double[]>> sb = getSampleAndBreakpointTable(
                scanlinesi, scanlinesj);

        final List<Tuple2D<Array, Array>> outputintensities = new ArrayList<Tuple2D<Array, Array>>();
        final List<Tuple2D<Integer, Integer>> outputintensitiescounter = new ArrayList<Tuple2D<Integer, Integer>>();

        final IndexIterator iteri = warpPathi.getIndexIterator();
        final IndexIterator iterj = warpPathj.getIndexIterator();

        log.info("Conserve left chromatogram axis: {}", this.holdi);
        log.info("Conserve top chromatogram axis: {}", this.holdj);

        int oldi = -1, oldj = -1;
        while (iteri.hasNext() && iterj.hasNext()) {
            final int scanindexi = (Integer) iteri.next();
            final int scanindexj = (Integer) iterj.next();

            final Array scanlinei = scanlinesi.get(scanindexi);
            final Array scanlinej = scanlinesj.get(scanindexj);

            if (oldi != scanindexi && oldj != scanindexj) {
                // new pixel line
                outputintensities.add(new Tuple2D<Array, Array>(scanlinei,
                        scanlinej));
                outputintensitiescounter.add(new Tuple2D<Integer, Integer>(1, 1));
                this.currentrasterline++;
            } else if (oldi != scanindexi && oldj == scanindexj) {
                if (this.holdj) {
                    // adding pixelline to current sum
                    int c = outputintensitiescounter.get(this.currentrasterline).
                            getFirst();
                    outputintensitiescounter.get(this.currentrasterline).
                            setFirst(++c);
                    final Tuple2D<Array, Array> tmp = outputintensities.get(
                            this.currentrasterline);
                    tmp.setFirst(maltcms.tools.ArrayTools.sum(tmp.getFirst(),
                            scanlinei));
                } else {
                    // adding pixelline
                    outputintensities.add(new Tuple2D<Array, Array>(scanlinei,
                            scanlinej));
                    outputintensitiescounter.add(new Tuple2D<Integer, Integer>(
                            1, 1));
                    this.currentrasterline++;
                }
            } else if (oldi == scanindexi && oldj != scanindexj) {
                if (this.holdi) {
                    // add pixelline to current sum
                    int c = outputintensitiescounter.get(this.currentrasterline).
                            getSecond();
                    outputintensitiescounter.get(this.currentrasterline).
                            setSecond(++c);
                    final Tuple2D<Array, Array> tmp = outputintensities.get(
                            this.currentrasterline);
                    tmp.setSecond(maltcms.tools.ArrayTools.sum(tmp.getSecond(),
                            scanlinej));
                } else {
                    // adding pixelline
                    outputintensities.add(new Tuple2D<Array, Array>(scanlinei,
                            scanlinej));
                    outputintensitiescounter.add(new Tuple2D<Integer, Integer>(
                            1, 1));
                    this.currentrasterline++;
                }
            } else {
                // should never be reached
                log.error("Warpath has at least one invalid node.");
                throw new RuntimeException("Warppath exception. Invalid path.");
            }
            oldi = scanindexi;
            oldj = scanindexj;
        }

        return ci(outputintensities, outputintensitiescounter,
                (int) (scanlinesi.get(0).getSize()), sb.getFirst(),
                sb.getSecond().getFirst(), sb.getSecond().getSecond());
    }

    /**
     * Creates the samples table and the breakpoint table for reference and
     * query arrays.
     * 
     * @param scanlinesi
     *            scanlines of the reference
     * @param scanlinesj
     *            scanlines of the query
     * @return {smaple table,{breakpoints i, breakpoints j}}
     */
    protected Tuple2D<double[], Tuple2D<double[], double[]>> getSampleAndBreakpointTable(
            final List<Array> scanlinesi, final List<Array> scanlinesj) {
        final double[] samples = ImageTools.createSampleTable(this.binSize);
        final Array scanlinesiC = cross.datastructures.tools.ArrayTools.glue(
                scanlinesi);
        final Array scanlinesjC = cross.datastructures.tools.ArrayTools.glue(
                scanlinesj);

        final ArrayStatsScanner ass = new ArrayStatsScanner();
        final StatsMap[] sm = ass.apply(new Array[]{scanlinesiC, scanlinesjC});
        final Double meani = sm[0].get(Vars.Mean.toString());
        final Double meanj = sm[1].get(Vars.Mean.toString());

        double thresholdi, thresholdj;
        if (this.globalmax) {
            thresholdi = ((meani + meanj) / 2.0d) / this.threshold;
            thresholdj = thresholdi;
        } else {
            thresholdi = meani / this.threshold;
            thresholdj = meanj / this.threshold;
        }

        log.info("Using thresholdi: {}", thresholdi);
        log.info("Using thresholdj: {}", thresholdj);
        if (thresholdi != 0) {
            if (this.filter) {
                final AArrayFilter minFilteri = new MinimumFilter(thresholdi);
                minFilteri.apply(new Array[]{scanlinesiC});
                final AArrayFilter minFilterj = new MinimumFilter(thresholdj);
                minFilterj.apply(new Array[]{scanlinesjC});
            } else {
                log.info("Filtering was turned off");
            }
        } else {
            log.info("Skipping threshold minimization.");
        }

        final double[] breakpointsi = ImageTools.getBreakpoints(scanlinesiC,
                this.binSize, Double.NEGATIVE_INFINITY);
        final double[] breakpointsj = ImageTools.getBreakpoints(scanlinesjC,
                this.binSize, Double.NEGATIVE_INFINITY);

        return new Tuple2D<double[], Tuple2D<double[], double[]>>(samples,
                new Tuple2D<double[], double[]>(breakpointsi, breakpointsj));
    }

    /**
     * Creates the image.
     * 
     * @param outputintensities
     *            output intensities
     * @param outputintensitiescounter
     *            counter for the output intensities
     * @param imageheight
     *            image height
     * @param samples
     *            samples
     * @param breakpointsi
     *            break points for reference
     * @param breakpointsj
     *            break points for query
     * @return image
     */
    protected BufferedImage ci(
            final List<Tuple2D<Array, Array>> outputintensities,
            final List<Tuple2D<Integer, Integer>> outputintensitiescounter,
            int imageheight, final double[] samples,
            final double[] breakpointsi, final double[] breakpointsj) {

        int imagewidth = outputintensities.size();
        if (!this.horizontal) {
            imagewidth = imageheight;
            imageheight = outputintensities.size();
        }

        log.info("Creating BufferedImage(" + imagewidth + "x" + imageheight
                + ")");
        log.info("{},{}", outputintensities.size(), outputintensities.get(0).
                getFirst().getShape()[0]);
        final BufferedImage img = new BufferedImage(imagewidth, imageheight,
                BufferedImage.TYPE_INT_RGB);
        final WritableRaster raster = img.getRaster();

        int rasterline = 0;
        int[] rgbvalueEqual = null;
        for (Tuple2D<Array, Array> t : outputintensities) {
            // if (t.getFirst().getSize() == imageheight
            // && t.getSecond().getSize() == imageheight) {
            final IndexIterator oiteri = t.getFirst().getIndexIterator();
            final IndexIterator oiterj = t.getSecond().getIndexIterator();
            final int counteri = outputintensitiescounter.get(rasterline).
                    getFirst();
            final int counterj = outputintensitiescounter.get(rasterline).
                    getSecond();
            int c = 0;
            while (oiteri.hasNext() && oiterj.hasNext()) {
                if (c >= imageheight && this.horizontal) {
                    log.error("Skipping {} value(s) in scanline {}.", c
                            - imageheight, rasterline);
                    break;
                }
                double intensityi = (Double) oiteri.next();
                double intensityj = (Double) oiterj.next();
                if (this.normalize) {
                    rgbvalueEqual = getRasterColor(ImageTools.getSample(
                            samples, breakpointsi, intensityi / counteri),
                            1.0d, ImageTools.getSample(samples, breakpointsj,
                            intensityj / counterj), 1.0d);
                } else if (!this.normalize && this.binSize == 2) {
                    if (intensityi > 0.0d) {
                        intensityi = 1.0d;
                    }
                    if (intensityj > 0.0d) {
                        intensityj = 1.0d;
                    }
                    rgbvalueEqual = getRasterColor(intensityi, 1.0d,
                            intensityj, 1.0d);
                } else {
                    rgbvalueEqual = getRasterColor(ImageTools.getSample(
                            samples, breakpointsi, intensityi), 1.0d,
                            ImageTools.getSample(samples, breakpointsj,
                            intensityj), 1.0d);
                }

                if (!this.horizontal) {
                    // System.out
                    // .println(c + "," + (imageheight - rasterline - 1));
                    raster.setPixel(c, imageheight - rasterline - 1,
                            rgbvalueEqual);
                } else {
                    raster.setPixel(rasterline, imageheight - c - 1,
                            rgbvalueEqual);
                }

                c++;
            }
            // } else {
            // log
            // .error(
            // "Skipping rasterline {}. The arraysize of at least one of the two chromatograms is not equal the imageheight.",
            // rasterline);
            // }
            rasterline++;
        }

        img.setData(raster);
        return img;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.peakListVar = cfg.getString("var.peak_index_list",
                "peak_index_list");
        this.holdi = cfg.getBoolean(this.getClass().getName() + ".holdi", true);
        this.holdj = cfg.getBoolean(this.getClass().getName() + ".holdj", false);
        this.globalmax = cfg.getBoolean(this.getClass().getName()
                + ".globalmax", false);
        this.black = cfg.getBoolean(this.getClass().getName() + ".black", true);
        this.filter = cfg.getBoolean(this.getClass().getName() + ".filter",
                true);
        this.threshold = cfg.getDouble(
                this.getClass().getName() + ".threshold", 6.0d);
        this.horizontal = cfg.getBoolean(this.getClass().getName()
                + ".horizontal", true);
    }

    /**
     * Will create an array int[3] containing the rgb values for the raster.
     * 
     * @param ci
     *            current intensity of the first series
     * @param maxci
     *            maximum intensity of the first series
     * @param cj
     *            current intensity of the second series
     * @param maxcj
     *            maximum intensity of the second series
     * @return rgb color array
     */
    protected int[] getRasterColor(final double ci, final double maxci,
            final double cj, final double maxcj) {
        final int vi = (int) (ci * 255.0d / maxci);
        final int vj = (int) (cj * 255.0d / maxcj);

        if (this.black) {
            return new int[]{vi, vj, 0};
        } else {
            return new int[]{255 - vj, 255 - vi, 255 - Math.max(vi, vj)};
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JFreeChart createChart(final String filename,
            final String samplenamei, final String samplenamej,
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimei,
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimej) {
        final NumberAxis domain = new NumberAxis();
        domain.setLowerMargin(0);
        domain.setUpperMargin(0);
        domain.setAutoRange(false);
        if (this.holdi && !this.holdj) {
            domain.setLabel("1. retention time(first sample) [s]");
            final ArrayDouble.D1 irettime = rettimei.getFirst();
            domain.setRange(irettime.get(0),
                    irettime.get(irettime.getShape()[0] - 1));
        } else if (this.holdj && !this.holdi) {
            domain.setLabel("1. retention time(second sample) [s]");
            final ArrayDouble.D1 jrettime = rettimej.getFirst();
            domain.setRange(jrettime.get(0),
                    jrettime.get(jrettime.getShape()[0] - 1));
        } else {
            log.info(
                    "Using neither scan acquisition time from the first nor from the second chromatogram. Using the warp path index for the domain axis.");
            if (this.currentrasterline != -1) {
                domain.setRange(0, this.currentrasterline);
                domain.setLabel("warp path index");
            } else {
                log.info("Can not create serialized image");
                return null;
            }
        }

        final NumberAxis values = new NumberAxis();
        values.setLowerMargin(0);
        values.setUpperMargin(0);
        values.setAutoRange(false);
        final ArrayDouble.D1 secondrettime = rettimei.getSecond();
        values.setRange(secondrettime.get(0), secondrettime.get(secondrettime.
                getShape()[0] - 1));
        values.setLabel("2. ret time [sec]");

        try {
            final XYBPlot plot = new XYBPlot(filename, domain, values);
            final JFreeChart chart = new JFreeChart(
                    "Differential time warp visualization", plot);
            chart.setAntiAlias(true);
            chart.setBackgroundPaint(Color.WHITE);

            final int scaleresolution = 100;
            final LookupPaintScale paintscalei = new LookupPaintScale(0,
                    scaleresolution, Color.white);
            final LookupPaintScale paintscalej = new LookupPaintScale(0,
                    scaleresolution, Color.white);
            final LookupPaintScale paintscaleij = new LookupPaintScale(0,
                    scaleresolution, Color.white);
            int[] rgb = new int[3];
            for (int i = 0; i < scaleresolution; i++) {
                rgb = getRasterColor(i, scaleresolution, 0, scaleresolution);
                paintscalei.add(i, new Color(rgb[0], rgb[1], rgb[2]));
                rgb = getRasterColor(0, scaleresolution, i, scaleresolution);
                paintscalej.add(i, new Color(rgb[0], rgb[1], rgb[2]));
                rgb = getRasterColor(i, scaleresolution, i, scaleresolution);
                paintscaleij.add(i, new Color(rgb[0], rgb[1], rgb[2]));
            }

            final NumberAxis scaleaxisi = new NumberAxis(samplenamei);
            final NumberAxis scaleaxisj = new NumberAxis(samplenamej);
            final NumberAxis scaleaxisij = new NumberAxis(samplenamei + " and "
                    + samplenamej);
            scaleaxisi.setRange(0, scaleresolution);
            scaleaxisj.setRange(0, scaleresolution);
            scaleaxisij.setRange(0, scaleresolution);
            chart.addSubtitle(0, new PaintScaleLegend(paintscalei, scaleaxisi));
            chart.addSubtitle(1, new PaintScaleLegend(paintscalej, scaleaxisj));
            chart.addSubtitle(2,
                    new PaintScaleLegend(paintscaleij, scaleaxisij));

            return chart;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JFreeChart addPeakMarker(final JFreeChart chart,
            final Tuple2D<Array, Array> warpPathij, final IFileFragment ref,
            final IFileFragment query,
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimei,
            final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> rettimej,
            final int spm) {
        ArrayDouble.D1 secondColumnTime = null;
        ArrayDouble.D1 firstColumnTime = null;
        Array peakident = null;
        Array peakwarp = null;
        Map<Integer, Integer[]> path = null;
        try {
            if (this.holdi && !this.holdj) {
                secondColumnTime = rettimei.getSecond();
                firstColumnTime = rettimei.getFirst();
                path = ArrayTools2.createPath(warpPathij.getSecond(),
                        warpPathij.getFirst());
                peakident = ref.getChild(this.peakListVar).getArray();
                peakwarp = query.getChild(this.peakListVar).getArray();
            } else if (this.holdj && !this.holdi) {
                secondColumnTime = rettimej.getSecond();
                firstColumnTime = rettimej.getFirst();
                path = ArrayTools2.createPath(warpPathij.getFirst(), warpPathij.
                        getSecond());
                peakident = query.getChild(this.peakListVar).getArray();
                peakwarp = ref.getChild(this.peakListVar).getArray();
            } else {
                return chart;
            }
        } catch (final ResourceNotAvailableException e) {
            // TODO
            return chart;
        }

        if (peakident == null || peakwarp == null) {
            log.error("Can not add peak marker. Peaklist is empty.");
            return chart;
        }

        final XYPlot plot = chart.getXYPlot();
        final int scanspermodulation = spm;

        final IndexIterator iterident = peakident.getIndexIterator();
        int refid = 0, queryid = 0;
        while (iterident.hasNext()) {
            final int index = iterident.getIntNext();
            final int x = index / scanspermodulation;
            final int y = index % scanspermodulation;
            addMarker(plot, firstColumnTime.get(x + 1), secondColumnTime.get(
                    y + 1), true, refid++);
        }

        final IndexIterator iterwarp = peakwarp.getIndexIterator();
        while (iterwarp.hasNext()) {
            final int index = iterwarp.getIntNext();
            final int ox = index / scanspermodulation;
            final Integer[] xs = path.get(ox);
            final int y = index % scanspermodulation;
            for (Integer x : xs) {
                addMarker(plot, firstColumnTime.get(x + 1),
                        secondColumnTime.get(y + 1), false, queryid);
            }
            queryid++;
        }

        return chart;
    }

    /**
     * Adding a marker to the given plot.
     * 
     * @param plot
     *            plot
     * @param x
     *            x
     * @param y
     *            y
     * @param isI
     *            <code>true</code> then the marker will be painted in red,
     *            otherwise green
     * @param id
     *            peak id
     */
    private void addMarker(final XYPlot plot, final double x, final double y,
            final boolean isI, final int id) {
        XYPointerAnnotation pm;
        if (isI) {
            pm = new XYPointerAnnotation("" + id, x, y, 7 * Math.PI / 4.0d);
            pm.setPaint(Color.RED);
        } else {
            pm = new XYPointerAnnotation("" + id, x, y, 3 * Math.PI / 4.0d);
            pm.setPaint(Color.GREEN);
        }
        pm.setArrowLength(0.0d);
        pm.setBaseRadius(0.0d);
        pm.setTipRadius(0.0d);
        plot.addAnnotation(pm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBinSize(final int nBinSize) {
        this.binSize = nBinSize;
    }

    /**
     * {@inheritDoc}
     * 
     * @return
     */
    @Override
    public int getBinSize() {
        return this.binSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNormalize(final boolean nNormalize) {
        this.normalize = nNormalize;
    }
}
