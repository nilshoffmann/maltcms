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
package maltcms.commands.fragments2d.testing;

import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.tuple.Tuple2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.MinimumFilter;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools;
import maltcms.tools.ArrayTools2;
import maltcms.tools.ImageTools;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * <p>Visualization2D class.</p>
 *
 * @author Mathias Wilhelm
 * 
 */
@Slf4j
@Data
public class Visualization2D {

    private int currentrasterline = -1;
    private boolean holdHorizontalI = false;
    private boolean holdVerticalI = false;
    private boolean holdHorizontalJ = false;
    private boolean holdVerticalJ = false;
    private boolean black = true;
    private double threshold = 6.0d;
    private boolean globalmax = false;
    private boolean filter = false;
    private boolean normalize = true;
    private int binSize = 256;

    /**
     * <p>createImage.</p>
     *
     * @param scanlinesi a {@link java.util.List} object.
     * @param scanlinesj a {@link java.util.List} object.
     * @param horizontal a {@link java.util.List} object.
     * @param vertical a {@link java.util.List} object.
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    public BufferedImage createImage(List<Array> scanlinesi,
            List<Array> scanlinesj, final List<Point> horizontal,
            final List<Point> vertical) {

        Tuple2D<List<Array>, List<Array>> scanlines = new Tuple2D<>(
                scanlinesi, scanlinesj);

        if (horizontal != null) {
            scanlines = createNewScanlines(scanlines.getFirst(), scanlines.
                    getSecond(), horizontal, this.holdHorizontalI,
                    this.holdHorizontalJ);
        }

        if (vertical != null) {
            scanlines = createNewScanlines(ArrayTools2.transpose(scanlines.
                    getFirst()), ArrayTools2.transpose(scanlines.getSecond()),
                    vertical, this.holdVerticalI, this.holdVerticalJ);
            scanlines = new Tuple2D<>(ArrayTools2.
                    transpose(scanlines.getFirst()),
                    ArrayTools2.transpose(scanlines.getSecond()));
        }

        final Tuple2D<double[], Tuple2D<double[], double[]>> sb = getSampleAndBreakpointTable(
                scanlinesi, scanlinesj);

        return ci(scanlines.getFirst(), scanlines.getSecond(), sb.getFirst(),
                sb.getSecond().getFirst(), sb.getSecond().getSecond());
    }

    /**
     * <p>createNewScanlines.</p>
     *
     * @param scanlinesi a {@link java.util.List} object.
     * @param scanlinesj a {@link java.util.List} object.
     * @param warpPath a {@link java.util.List} object.
     * @param holdi a boolean.
     * @param holdj a boolean.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<List<Array>, List<Array>> createNewScanlines(
            List<Array> scanlinesi, List<Array> scanlinesj,
            List<Point> warpPath, final boolean holdi, final boolean holdj) {

        this.currentrasterline = -1;

        final List<Tuple2D<Array, Array>> outputintensities = new ArrayList<>();
        final List<Tuple2D<Integer, Integer>> outputintensitiescounter = new ArrayList<>();

        // log.info("Conserve left chromatogram axis: {}", this.holdi);
        // log.info("Conserve top chromatogram axis: {}", this.holdj);
        int oldi = -1, oldj = -1;
        for (Point p : warpPath) {
            final int scanindexi = p.x;
            final int scanindexj = p.y;

            // log.info("{} - {}", scanindexi, scanindexj);
            final Array scanlinei = scanlinesi.get(scanindexi);
            final Array scanlinej = scanlinesj.get(scanindexj);

            if (oldi != scanindexi && oldj != scanindexj) {
                // new pixel line
                outputintensities.add(new Tuple2D<>(scanlinei,
                        scanlinej));
                outputintensitiescounter.add(new Tuple2D<>(1, 1));
                this.currentrasterline++;
            } else if (oldi != scanindexi && oldj == scanindexj) {
                if (holdj) {
                    // adding pixelline to current sum
                    int c = outputintensitiescounter.get(this.currentrasterline).
                            getFirst();
                    outputintensitiescounter.get(this.currentrasterline).
                            setFirst(++c);
                    final Tuple2D<Array, Array> tmp = outputintensities.get(
                            this.currentrasterline);
                    tmp.setFirst(ArrayTools.sum(tmp.getFirst(), scanlinei));
                } else {
                    // adding pixelline
                    outputintensities.add(new Tuple2D<>(scanlinei,
                            scanlinej));
                    outputintensitiescounter.add(new Tuple2D<>(
                            1, 1));
                    this.currentrasterline++;
                }
            } else if (oldi == scanindexi && oldj != scanindexj) {
                if (holdi) {
                    // add pixelline to current sum
                    int c = outputintensitiescounter.get(this.currentrasterline).
                            getSecond();
                    outputintensitiescounter.get(this.currentrasterline).
                            setSecond(++c);
                    final Tuple2D<Array, Array> tmp = outputintensities.get(
                            this.currentrasterline);
                    tmp.setSecond(ArrayTools.sum(tmp.getSecond(), scanlinej));
                } else {
                    // adding pixelline
                    outputintensities.add(new Tuple2D<>(scanlinei,
                            scanlinej));
                    outputintensitiescounter.add(new Tuple2D<>(
                            1, 1));
                    this.currentrasterline++;
                }
            } else {
                throw new RuntimeException("Warppath exception. Invalid path.");
            }
            oldi = scanindexi;
            oldj = scanindexj;
        }

        return convertToScanlines(outputintensities, outputintensitiescounter);
    }

    private Tuple2D<List<Array>, List<Array>> convertToScanlines(
            List<Tuple2D<Array, Array>> outputintensities,
            List<Tuple2D<Integer, Integer>> outputintensitiescounter) {

        final List<Array> scanlinesi = new ArrayList<>();
        final List<Array> scanlinesj = new ArrayList<>();

        for (int i = 0; i < outputintensities.size(); i++) {
            scanlinesi.add(ArrayTools.mult(outputintensities.get(i).getFirst(),
                    1.0d / outputintensitiescounter.get(i).getFirst()));
            scanlinesj.add(ArrayTools.mult(
                    outputintensities.get(i).getSecond(),
                    1.0d / outputintensitiescounter.get(i).getSecond()));
        }

        return new Tuple2D<>(scanlinesi, scanlinesj);
    }

    /**
     * <p>ci.</p>
     *
     * @param scanlinesi a {@link java.util.List} object.
     * @param scanlinesj a {@link java.util.List} object.
     * @param samples an array of double.
     * @param breakpointsi an array of double.
     * @param breakpointsj an array of double.
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    protected BufferedImage ci(final List<Array> scanlinesi,
            final List<Array> scanlinesj, final double[] samples,
            final double[] breakpointsi, final double[] breakpointsj) {

        if (scanlinesi.size() != scanlinesj.size()) {
            log.info("ERROR!!! scanlines nicht gleichlang");
            return null;
        }

        final int imageheight = scanlinesi.get(0).getShape()[0];
        final int imagewidth = scanlinesi.size();
        final BufferedImage img = new BufferedImage(imagewidth, imageheight,
                BufferedImage.TYPE_INT_RGB);
        final WritableRaster raster = img.getRaster();

        IndexIterator iter1, iter2;
        double intensityi, intensityj;
        int[] rgbvalueEqual = null;
        int c = 0;
        int rasterline = 0;

        for (int i = 0; i < scanlinesi.size(); i++) {
            iter1 = scanlinesi.get(i).getIndexIterator();
            iter2 = scanlinesj.get(i).getIndexIterator();
            c = 0;
            while (iter1.hasNext() && iter2.hasNext()) {
                intensityi = iter1.getDoubleNext();
                intensityj = iter2.getDoubleNext();
                if (this.normalize) {
                    rgbvalueEqual = getRasterColor(ImageTools.getSample(
                            samples, breakpointsi, intensityi), 1.0d,
                            ImageTools.getSample(samples, breakpointsj,
                                    intensityj), 1.0d);
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

                if ((rasterline < imagewidth)
                        && ((imageheight - c - 1) < imageheight)
                        && (imageheight - c - 1) >= 0) {
                    raster.setPixel(rasterline, imageheight - c - 1,
                            rgbvalueEqual);
                }

                c++;
            }
            rasterline++;
        }

        return img;
    }

    /**
     * Will create an array int[3] containing the rgb values for the raster.
     *
     * @param ci current intensity of the first series
     * @param maxci maximum intensity of the first series
     * @param cj current intensity of the second series
     * @param maxcj maximum intensity of the second series
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
     * Creates the samples table and the breakpoint table for reference and
     * query arrays.
     *
     * @param scanlinesi scanlines of the reference
     * @param scanlinesj scanlines of the query
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

        return new Tuple2D<>(samples,
                new Tuple2D<>(breakpointsi, breakpointsj));
    }
}
