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
package maltcms.tools;

import cross.Factory;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.MathTools;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.MinMaxNormalizationFilter;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.io.csv.ColorRampReader;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;
import ucar.ma2.Sparse;

/**
 * Utility class concerned with creation and saving of images.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class ImageTools {

    /**
     * Adds all peaks to an image. If one of the colors are
     * <code>null</code>, this component wont be drawn.
     *
     * @param img                img
     * @param peaklist           snakes
     * @param seedPointColor     color of the seed point
     * @param regionColor        color of the peak area
     * @param boundaryColor      color of the boundary
     * @param scansPerModulation scans per modulation
     * @return new image
     */
    public static BufferedImage addPeakToImage(final BufferedImage img,
        final List<Peak2D> peaklist, final int[] seedPointColor,
        final int[] regionColor, final int[] boundaryColor,
        final int scansPerModulation) {
        final WritableRaster raster = img.getRaster();

        for (final Peak2D peak : peaklist) {
            if (regionColor != null) {
                for (final Point p : peak.getPeakArea().getRegionPoints()) {
                    raster.setPixel(p.x, scansPerModulation - p.y - 1,
                        regionColor);
                }
            }
            if (boundaryColor != null) {
                for (final Point p : peak.getPeakArea().getBoundaryPoints()) {
                    try {
                        raster.setPixel(p.x, scansPerModulation - p.y - 1,
                            boundaryColor);
                    } catch (final ArrayIndexOutOfBoundsException e) {
                        // System.out.println("Skipping Pixel");
                    }
                }
            }
        }
        for (final Peak2D snake : peaklist) {
            if (seedPointColor != null) {
                final Point seed = snake.getPeakArea().getSeedPoint();
                if (seed.x < img.getWidth()
                    && (scansPerModulation - seed.y - 1) > 0) {
                    raster.setPixel(seed.x, scansPerModulation - seed.y - 1,
                        seedPointColor);
                }
            }
        }

        img.setData(raster);
        return img;
    }

    /**
     * Adds all peaks to an image. If one of the colors are
     * <code>null</code>, this component wont be drawn.
     *
     * @param img                img
     * @param peaklist           snakes
     * @param seedPointColor     color of the seed point
     * @param regionColor        color of the peak area
     * @param boundaryColor      color of the boundary
     * @param scansPerModulation scans per modulation
     * @return new image
     */
    public static BufferedImage addPeakAreaToImage(final BufferedImage img,
        final List<PeakArea2D> peaklist, final int[] seedPointColor,
        final int[] regionColor, final int[] boundaryColor,
        final int scansPerModulation) {
        final WritableRaster raster = img.getRaster();

        for (final PeakArea2D peak : peaklist) {
            if (regionColor != null) {
                for (final Point p : peak.getRegionPoints()) {
                    raster.setPixel(p.x, scansPerModulation - p.y - 1,
                        regionColor);
                }
            }
            if (boundaryColor != null) {
                for (final Point p : peak.getBoundaryPoints()) {
                    try {
                        raster.setPixel(p.x, scansPerModulation - p.y - 1,
                            boundaryColor);
                    } catch (final ArrayIndexOutOfBoundsException e) {
                        // System.out.println("Skipping Pixel");
                    }
                }
            }
        }
        for (final PeakArea2D peak : peaklist) {
            if (seedPointColor != null) {
                final Point seed = peak.getSeedPoint();
                if (seed.x < img.getWidth()
                    && (scansPerModulation - seed.y - 1) > 0) {
                    raster.setPixel(seed.x, scansPerModulation - seed.y - 1,
                        seedPointColor);
                }
            }
        }

        img.setData(raster);
        return img;
    }

    /**
     * Creates an image of an 2D chromatogramm.
     *
     * @param <T>                should extend {@link AFragmentCommand}
     * @param ffName             file fragment name
     * @param intensity          intensity array
     * @param scansPerModulation scans per modulation
     * @param fillValue          fill value
     * @param threshold          threshold
     * @param colorRamp          color ramp
     * @param creator            creator of this image
     * @return image
     */
    public static synchronized <T> BufferedImage create2DImage(
        final String ffName, final List<Array> intensity,
        final int scansPerModulation, final double fillValue,
        final double threshold, final int[][] colorRamp,
        final Class<T> creator) {
        int maxIndex = 0;
        Array intensities = cross.datastructures.tools.ArrayTools.glue(intensity);
        final IndexIterator ii = intensities.getIndexIterator();
        while (ii.hasNext()) {
            if (ii.getDoubleNext() == fillValue) {
                break;
            } else {
                maxIndex++;
            }
        }
        if (maxIndex < intensities.getShape()[0]) {
            try {
                intensities = intensities.section(new int[]{0},
                    new int[]{maxIndex}, new int[]{1});
            } catch (final InvalidRangeException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
        final BufferedImage bi = maltcms.tools.ImageTools.fullSpectrum(ffName,
            intensity, scansPerModulation, colorRamp,
            1024, true, threshold);

        return bi;
    }

    public static double[] createSampleTable(final int nsamples) {
        final double[] samples = new double[nsamples];
        ImageTools.log.debug("Creating sample table with size {}", nsamples);
        for (int i = 0; i < samples.length; i++) {
            samples[i] = ((double) i) / ((double) nsamples);
        }
        return samples;
    }

    public static BufferedImage applyLut(BufferedImage im, LookupTable lt) {
        LookupOp lo = new LookupOp(lt, null);
        return lo.filter(im, null);
    }

    public static BufferedImage createCompatibleImage(int width, int height,
        int transparencyMode) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.isHeadlessInstance()) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        return gc.createCompatibleImage(width, height, transparencyMode);
    }

    public static BufferedImage createColorRampImage(double[] sampleTable,
        int transparencyMode, Color... colors) {

        BufferedImage im = createCompatibleImage(sampleTable.length, 1,
            transparencyMode);
        Graphics2D g = im.createGraphics();
        float[] fractions = new float[colors.length];
        float step = 1.0f / (fractions.length);
        for (int i = 0; i < fractions.length; ++i) {
            fractions[i] = i * step;
        }
        LinearGradientPaint gradient = new LinearGradientPaint(0, 0,
            sampleTable.length, 1, fractions, colors,
            MultipleGradientPaint.CycleMethod.NO_CYCLE);
        g.setPaint(gradient);
        g.fillRect(0, 0, sampleTable.length, 1);
        g.dispose();
        return im;

    }

    public static BufferedImage modifyImageLookupTable(BufferedImage bi,
        int[][] activeColorRamp, double[] sampleTable, double alpha,
        double beta, int transparencyMode, float transparency) {
        double[] mappedSampleTable = mapSampleTable(sampleTable, alpha, beta);
        BufferedImage cri = createColorRampImage(mappedSampleTable,
            transparencyMode, rampToColorArray(activeColorRamp));
        return applyLut(bi, createLookupTable(cri, transparency,
            sampleTable.length));
    }

    public static BufferedImage createModifiedLookupImage(Color[] colors,
        double[] sampleTable, double alpha, double beta,
        int transparencyMode, float transparency) {
        double[] mappedSampleTable = mapSampleTable(sampleTable, alpha, beta);
        BufferedImage cri = createColorRampImage(mappedSampleTable,
            transparencyMode, colors);
        BufferedImage cri2 = createCompatibleImage(cri.getWidth(),
            cri.getHeight(), cri.getTransparency());
        for (int i = 0; i < mappedSampleTable.length; i++) {
            double frac = mappedSampleTable[i];
            // System.out.println("Original value: " + sampleTable[i]);
            // System.out.println("Mapped value: " + frac);
            int index = Math.max(0, Math.min(mappedSampleTable.length - 1,
                (int) (frac * mappedSampleTable.length)));
            // System.out.println("Index before: " + i + " index after: " +
            // index);
            cri2.setRGB(i, 0, cri.getRGB(index, 0));
        }
        return cri2;
        // return applyLut(bi, createLookupTable(cri, transparency,
        // sampleTable.length));
    }

    public static Color[] rampToColorArray(int[][] colorRamp) {
        Color[] c = new Color[colorRamp.length];
        for (int i = 0; i < colorRamp.length; i++) {
            c[i] = new Color(colorRamp[i][0], colorRamp[i][1], colorRamp[i][2]);
        }
        return c;
    }

    public static LookupTable createLookupTable(BufferedImage im,
        float transparency, int tableSize) {
        Raster imageRaster = im.getData();
        double sampleStep = 1.0d * im.getWidth() / tableSize; // Sample pixels
        // evenly
        byte[][] colorTable = new byte[4][tableSize];
        int[] pixel = new int[1]; // Sample pixel
        Color c;
        for (int i = 0; i < tableSize; ++i) {
            imageRaster.getDataElements((int) (i * sampleStep), 0, pixel);
            c = new Color(pixel[0]);
            colorTable[0][i] = (byte) c.getRed();
            colorTable[1][i] = (byte) c.getGreen();
            colorTable[2][i] = (byte) c.getBlue();
            colorTable[3][i] = (byte) (transparency * 0xff);
        }
        LookupTable lookupTable = new ByteLookupTable(0, colorTable);
        return lookupTable;
    }

    public static void main(String[] args) {
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.getDefaultRamp();
        Color[] cRamp = new Color[]{Color.BLACK, Color.orange, Color.yellow,
            Color.GRAY, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.WHITE};// rampToColorArray(colorRamp);
        // new Color[] { Color.BLUE.darker(), Color.CYAN,
        // Color.GREEN.darker(), Color.RED, Color.orange, Color.YELLOW,
        // Color.white };
        int elems = 512;
        ArrayDouble.D1 imx = ArrayTools.randomGaussian(elems, 0.3, 8.0);
        ArrayDouble.D1 imy = ArrayTools.randomGaussian(elems, 1.5, 41.0);
        ArrayDouble.D2 img = new ArrayDouble.D2(elems, elems);
        for (int i = 0; i < elems; i++) {
            for (int j = 0; j < elems; j++) {
                img.set(i, j, ((double) i) * ((double) j));
            }
        }
        // System.out.println(img);
        MinMax mm = MAMath.getMinMax(img);
        // System.out.println("Min : " + mm.min + " Max: " + mm.max);
        MinMaxNormalizationFilter mmnf = new MinMaxNormalizationFilter(mm.min,
            mm.max);
        img = (ArrayDouble.D2) mmnf.apply(img);
        // System.out.println(img);
        MinMax mm2 = MAMath.getMinMax(img);
        // System.out.println("Min : " + mm2.min + " Max: " + mm2.max);
        int nsamples = 256;
        double[] sampleTable = createSampleTable(nsamples);
        // System.out.println("Sampletable: " + Arrays.toString(sampleTable));
        BufferedImage crampImg = createColorRampImage(sampleTable,
            Transparency.TRANSLUCENT, cRamp);
        BufferedImage sourceImg = makeImage2D(img, nsamples);
        BufferedImage destImg = applyLut(sourceImg, createLookupTable(crampImg,
            1.0f, nsamples));
        JPanel jp = new JPanel();
        BoxLayout bl = new BoxLayout(jp, BoxLayout.X_AXIS);
        jp.setLayout(bl);
        JLabel orig = new JLabel(new ImageIcon(sourceImg));
        JLabel lut = new JLabel(new ImageIcon(crampImg));
        JLabel lutMapped = new JLabel(new ImageIcon(destImg));
        jp.add(orig);
        jp.add(lut);
        jp.add(lutMapped);
        JFrame jf = new JFrame();
        jf.add(jp);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static BufferedImage drawSquareMatrixWithLabels(
        final List<Integer> heightPerRow, final List<String> labels,
        final Array a, final double skipvalue) {
        final String fontFamily = "Lucida Sans";
        final int fontsize = 10;// Collections.min(heightPerRow).intValue() * 2
        // / 3;
        final Font f = new Font(fontFamily, Font.PLAIN, fontsize);
        // add legendWidth, draw legend
        final BufferedImage fullImage = new BufferedImage(a.getShape()[0], a.
            getShape()[1], BufferedImage.TYPE_INT_RGB);
        final ColorRampReader crr = new ColorRampReader();
        final int[][] colorRamp = crr.readColorRamp(Factory.getInstance().
            getConfiguration().getString("images.colorramp",
                "res/colorRamps/bw.csv"));
        final double[] breakpoints = ImageTools.getBreakpoints(a, 1024,
            skipvalue);
        ImageTools.makeImage2D(fullImage.getSubimage(0, 0, a.getShape()[0],
            a.getShape()[1]).getRaster(), a, 1024, colorRamp, 0.0,
            breakpoints);
        int prevHeight = 0;
        for (int i = 0; i < heightPerRow.size(); i++) {
            final BufferedImage label = fullImage.getSubimage(prevHeight,
                prevHeight, heightPerRow.get(i), heightPerRow.get(i));
            prevHeight += heightPerRow.get(i);
            final Graphics2D g = label.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, label.getWidth(), label.getHeight());
            g.setColor(Color.BLACK);
            g.setFont(f);
            final TextLayout tl = new TextLayout(labels.get(i), f, g.
                getFontRenderContext());
            tl.draw(g, heightPerRow.get(i)
                - (int) Math.ceil(tl.getBounds().getWidth() / 2),
                heightPerRow.get(i) - tl.getAscent());
        }
        return fullImage;
    }

    public static ArrayList<Array> filter(final ArrayList<Array> arrays,
        final int channels) {
        final ArrayList<Array> ret = new ArrayList<Array>(arrays.size());
        final ArrayDouble.D2 a = new ArrayDouble.D2(arrays.size(), channels);
        // ArrayDouble.D2 c = new ArrayDouble.D2(arrays.size(), channels);
        // Loop over scans
        int scan = 0;
        int minMass = 0;
        for (final Array arr : arrays) {
            final Index ind = arr.getIndex();
            if (arr instanceof Sparse) {
                final Sparse s = (Sparse) arr;
                minMass = s.getMinIndex();
                // Loop over mass channels
                for (int i = 0; i < s.getShape()[0]; i++) {
                    a.set(scan, i, s.get(i + minMass));
                }
            } else {
                // Loop over mass channels
                for (int i = 0; i < arr.getShape()[0]; i++) {
                    a.set(scan, i, arr.getDouble(ind.set(i)));
                }
            }

            scan++;
        }
        for (int j = 0; j < arrays.size(); j++) {
            ret.add(a.slice(0, j));
        }
        return ret;
    }

    public static BufferedImage fullSpectrum(final String chromatogramName,
        final List<Array> arrays, final int height,
        final int[][] colorRamp, final int sampleSize, final boolean flip,
        final double threshold) {
        if (arrays.get(0).getRank() != 1) {
            ImageTools.log.error("Only rank 1 arrays allowed!");
            return null;
        }
        // if (!(arrays.get(0) instanceof Sparse)) {
        // log.error("Only Sparse arrays allowed!");
        // return;
        // }
        ImageTools.log.debug("Creating full spectrum image!");
        ImageTools.log.debug("Number of bins: " + height);
        BufferedImage bim = new BufferedImage(arrays.size(), height,
            BufferedImage.TYPE_INT_RGB);
        final WritableRaster r = bim.getRaster();
        // Array glued = cross.tools.ArrayTools.flatten(arrays).getSecond()
        // .getSecond();
        ImageTools.makeImage(r, arrays, sampleSize, colorRamp, threshold);
        if (flip) {
            // Create a transposed image, flipped about the vertical axis
            final RenderedOp ro = TransposeDescriptor.create(bim,
                TransposeDescriptor.FLIP_VERTICAL, null);
            bim = ro.getAsBufferedImage();
        }
        return bim;
    }

    public static RenderedImage flipVertical(final RenderedImage bi) {
        // Create a transposed image, flipped about the vertical axis
        final RenderedOp ro = TransposeDescriptor.create(bi,
            TransposeDescriptor.FLIP_VERTICAL, null);
        return ro.getAsBufferedImage();
    }

    public static RenderedImage flipHorizontal(final RenderedImage bi) {
        // Create a transposed image, flipped about the horizontal axis
        final RenderedOp ro = TransposeDescriptor.create(bi,
            TransposeDescriptor.FLIP_HORIZONTAL, null);
        return ro.getAsBufferedImage();
    }

    public static double[] getBreakpoints(final Array values1,
        final int samples, final double skipvalue) {
        final Array values = values1.copy();
        final double[] sorted = (double[]) values.get1DJavaArray(double.class);
        Arrays.sort(sorted);
        int skipped = 0;
        int skipoff = 0;
        ImageTools.log.debug("Skipvalue: {}", skipvalue);
        for (int i = 0; i < sorted.length; i++) {
            if (sorted[i] == skipvalue) {
                if (skipvalue == Double.POSITIVE_INFINITY) {
                    sorted[i] = sorted[sorted.length - 1];
                    skipped++;
                } else if (skipvalue == Double.NEGATIVE_INFINITY) {
                    sorted[i] = sorted[0];
                    skipoff++;
                } else {
                    sorted[i] = sorted[0];
                    skipped++;
                }
            }
        }
        double[] svals = null;
        if ((skipped == 0) && (skipoff == 0)) {
            svals = sorted;
        } else {
            if ((skipoff == 0) && (skipped > 0)) {
                svals = new double[sorted.length - skipped];
                System.arraycopy(sorted, 0, svals, 0, svals.length);
            } else if ((skipoff > 0) && (skipped == 0)) {
                svals = new double[sorted.length - skipoff];
                System.arraycopy(sorted, skipoff, svals, 0, svals.length);
            } else {
                svals = new double[sorted.length - skipoff - skipped];
                System.arraycopy(sorted, skipoff, svals, 0, svals.length);
            }
        }

        ImageTools.log.info("Size of svals array={}", svals.length);

        double sum = 0;
        // IndexIterator ii = values.getIndexIterator();
        for (int i = 0; i < svals.length; i++) {
            sum += Math.abs(svals[i]);
        }

        ImageTools.log.debug("Total sum of intensities: " + sum);
        ImageTools.log.debug("Min value: " + svals[0] + " Max value: "
            + svals[svals.length - 1]);
        final double nthPart = sum / (samples);
        ImageTools.log.debug("Intensity per sample: " + nthPart);
        final double[] breakpoints = new double[samples];
        double psum = 0.0d;
        int cnt = 0;
        for (int i = 0; i < samples; i++) {
            while ((Math.abs(psum) < (i + 1) * nthPart)
                && (cnt < (svals.length))) {
                psum += svals[cnt++];
            }
            ImageTools.log.debug("Breakpoint " + i + " = "
                + svals[Math.max(0, cnt - 1)]);
            breakpoints[i] = svals[Math.max(0, cnt - 1)];
        }
        ImageTools.log.info("Using {} breakpoints", breakpoints.length);
        ImageTools.log.info(Arrays.toString(breakpoints));
        return breakpoints;
    }

    public static int getMaxStringLengthForTuple(
        final Collection<Tuple2D<String, Array>> c, final Font f) {
        // determine maximum allowed string length
        int maxStringLength = 0;
        final BufferedImage gtest = new BufferedImage(1, 1,
            BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = (Graphics2D) gtest.getGraphics();
        g2d.setFont(f);
        for (final Tuple2D<String, Array> t : c) {
            final int swidth = g2d.getFontMetrics().stringWidth(t.getFirst());
            maxStringLength = Math.max(maxStringLength, swidth);
        }
        return maxStringLength;
    }

    public static double getSample(final double[] samples,
        final double[] breakpoints, final double value) {
        final int i = Arrays.binarySearch(breakpoints, value);
        ImageTools.log.debug("Binary search index " + i);
        if (i < 0) {
            if (value < breakpoints[0]) {
                ImageTools.log.debug("Value is smaller, returning "
                    + samples[0]);
                return samples[0];
            }
            if (value > breakpoints[breakpoints.length - 1]) {
                ImageTools.log.debug("Value is greater, returning "
                    + samples[breakpoints.length - 1]);
                return samples[breakpoints.length - 1];
            }
            ImageTools.log.debug("Value in breakpoints, not exact, Returning sample "
                + samples[Math.abs(i) - 1]);
            return samples[Math.abs(i) - 1];
        } else if (i >= 0) {
            ImageTools.log.debug("i >= 0, Returning sample " + samples[i]);
            return samples[i];
        } else {
            ImageTools.log.debug("Else: Returning sample " + samples[0]);
            return samples[0];
        }
    }

    public static void makeImage(final WritableRaster w,
        final List<Array> arrays, final int nsamples,
        final int[][] colorRamp, final double threshold) {
        final Array values = cross.datastructures.tools.ArrayTools.glue(arrays);
        final double[] breakpoints = ImageTools.getBreakpoints(values,
            nsamples, Double.POSITIVE_INFINITY);
        ImageTools.makeImage(w, arrays, nsamples, colorRamp, threshold,
            breakpoints);
    }

    public static double mapTanH(final double alpha, final double beta,
        final double x) {
        final double a = Math.max(-1, Math.min(1, alpha));
        final double b = Math.max(1, Math.min(10, beta));
        final double v = (1.0d / b) * Math.tanh(beta * (a + x));
        return v;
    }

    public static double[] mapSampleTable(final double[] s, final double alpha,
        final double beta) {
        final double[] ret = new double[s.length];
        final double miny = mapTanH(alpha, beta, -1);
        final double maxy = mapTanH(alpha, beta, 1);
        for (int i = 0; i < s.length; i++) {
            double y = mapTanH(alpha, beta, 2 * s[i] - 1);
            ret[i] = (y - miny) / (maxy - miny);
        }
        return ret;
    }

    public static float[] toFloatArray(final double[] s) {
        final float[] a = new float[s.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Double.valueOf(s[i]).floatValue();
        }
        return a;
    }

    public static double[] toDoubleArray(final float[] s) {
        final double[] a = new double[s.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Float.valueOf(s[i]).doubleValue();
        }
        return a;
    }

    public static double[] mapSampleTable(final double[] s) {
        return mapSampleTable(s, 0, 1);
    }

    public static void makeImage(final WritableRaster w,
        final List<Array> arrays, final int nsamples,
        final int[][] colorRamp, final double threshold,
        final double[] breakpoints) {
        final double[] samples = mapSampleTable(ImageTools.createSampleTable(
            nsamples));
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (final Array a : arrays) {
            final MinMax m = MAMath.getMinMax(a);
            if (m.max > max) {
                max = m.max;
            }
            if (m.min < min) {
                min = m.min;
            }
        }

        double t = threshold;
        if (t > 1.0d) {
            t = 1.0d;
        }
        if (t < 0.0d) {
            t = 0.0d;
        }
        int minmass = 0;
        for (int i = 0; i < arrays.size(); i++) {
            final Array s = arrays.get(i);
            Index si = null;
            if (s instanceof Sparse) {
                minmass = ((Sparse) s).getMinIndex();
                ImageTools.log.debug("MinMass: {}", minmass);
            } else {
                minmass = 0;
            }
            si = s.getIndex();
            final int bins = s.getShape()[0];
            ImageTools.log.debug("Scan {} has {} bins!", i, bins);
            for (int j = 0; j < bins; j++) {
                double v = 0.0d;
                if (s instanceof Sparse) {
                    ImageTools.log.debug("Shape of s : {}, Setting index {}",
                        Arrays.toString(s.getShape()), (j));
                    ImageTools.log.debug("Offset: {}", minmass);
                    v = ImageTools.getSample(samples, breakpoints, ((Sparse) s).
                        get(j + minmass));
                    ImageTools.log.debug("Sample value: " + v + " original="
                        + ((Sparse) s).get(j + minmass));
                } else {
                    v = ImageTools.getSample(samples, breakpoints,
                        s.getDouble(si.set(j)));
                    ImageTools.log.debug("Sample value: " + v + " original="
                        + s.getDouble(si.set(j)));
                }
                if (v > 1.0) {
                    v = 1.0;
                } else if ((v < 0.0) || (v < threshold)) {
                    v = 0.0;
                }
                v *= 255.0d;

                final int floor = (int) Math.floor(v);
                final int ceil = (int) Math.ceil(v);
                int v1, v2, v3;
                if (floor == ceil) {
                    v1 = colorRamp[floor][0];
                    v2 = colorRamp[floor][1];
                    v3 = colorRamp[floor][2];
                    // fleqceil++;
                } else {
                    v1 = (int) Math.floor(MathTools.getLinearInterpolatedY(
                        floor, colorRamp[floor][0], ceil,
                        colorRamp[ceil][0], v));
                    v2 = (int) Math.floor(MathTools.getLinearInterpolatedY(
                        floor, colorRamp[floor][1], ceil,
                        colorRamp[ceil][1], v));
                    v3 = (int) Math.floor(MathTools.getLinearInterpolatedY(
                        floor, colorRamp[floor][2], ceil,
                        colorRamp[ceil][2], v));
                    // interp++;
                }
                // double v1 = colorRamp[(int)floor][0];
                // double v2 = colorRamp[(int)floor][1];
                // double v3 = colorRamp[(int)floor][2];
                try {
                    // if (Double.isNaN(v1) || Double.isNaN(v2)
                    // || Double.isNaN(v3)) {
                    // log.warn("Value of one of {},{},{} is NaN",
                    // new Object[] { v1, v2, v3 });
                    // }
                    if (floor >= t) {
                        w.setPixel(i, j, new int[]{
                            // Double.isNaN(v1) ? colorRamp[0][0] : v1,
                            // Double.isNaN(v2) ? colorRamp[0][1] : v2,
                            // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
                            v1, v2, v3});// colorRamp[floor][0],
                        // colorRamp[floor][1],
                        // colorRamp[floor][2] });
                    } else {
                        w.setPixel(i, j, new int[]{
                            // Double.isNaN(v1) ? colorRamp[0][0] : v1,
                            // Double.isNaN(v2) ? colorRamp[0][1] : v2,
                            // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
                            colorRamp[0][0], colorRamp[0][1],
                            colorRamp[0][2]});
                    }
                } catch (final ArrayIndexOutOfBoundsException aio) {
                    ImageTools.log.error("Index out of bounds at {},{}", i, j);
                    // throw aio;
                }
            }
        }
        ImageTools.log.debug("Maxval: {}  minval: {}", max, min);
        // log.info("{} ceil=floor, {} interpolations",fleqceil,interp);
    }

    public static RenderedImage makeImage2D(final Array values,
        final int nsamples, final double skipvalue) {
        final BufferedImage ba = new BufferedImage(values.getShape()[0], values.
            getShape()[1], BufferedImage.TYPE_3BYTE_BGR);
        final WritableRaster wr = ba.getRaster();
        final double[] breakpoints = ImageTools.getBreakpoints(values,
            nsamples, skipvalue);
        final int[][] colorRamp = new ColorRampReader().getDefaultRamp();
        ImageTools.makeImage2D(wr, values, nsamples, colorRamp, 0, breakpoints);
        return ba;
    }

    public static void makeImage2D(final WritableRaster w, final Array values,
        final int nsamples, final int[][] colorRamp,
        final double threshold, final double[] breakpoints) {
        if (values.getRank() != 2) {
            throw new IllegalArgumentException(
                "Method only accepts arrays of rank 2");
        }
        final double[] samples = ImageTools.createSampleTable(nsamples);
        final MinMax m = MAMath.getMinMax(values);
        // BufferedImage lookupImage = createColorRampImage(samples,
        // Transparency.TRANSLUCENT, rampToColorArray(colorRamp));
        double t = threshold;
        if (t > 255.0d) {
            t = 255.0d;
        }
        if (t < 0.0d) {
            t = 0.0d;
        }
        // WritableRaster lkupRaster = lookupImage.getRaster();
        // int[] tmp = new int[lkupRaster.getNumBands()];
        final double maxval = m.max;
        final double minval = m.min;
        final Index idx = values.getIndex();
        for (int i = 0; i < values.getShape()[0]; i++) {
            for (int j = 0; j < values.getShape()[1]; j++) {
                // int sample = (int) ((samples.length - 1) * ((values
                // .getDouble(idx.set(i, j)) - minval) / (maxval - minval)));
                // w.setPixel(i, j, lkupRaster.getPixel(sample, 0, tmp));
                double v = 0.0d;
                v = ImageTools.getSample(samples, breakpoints,
                    values.getDouble(idx.set(i, j)));
                if (v > 1.0) {
                    v = 1.0;
                } else if (v < 0.0) {
                    v = 0.0;
                }
                v *= 255.0d;

                final int floor = (int) Math.floor(v);
                final int ceil = (int) Math.ceil(v);
                int v1, v2, v3;
                if (floor == ceil) {
                    v1 = colorRamp[floor][0];
                    v2 = colorRamp[floor][1];
                    v3 = colorRamp[floor][2];
                    // fleqceil++;
                } else {
                    v1 = (int) Math.floor(MathTools.getLinearInterpolatedY(
                        floor, colorRamp[floor][0], ceil,
                        colorRamp[ceil][0], v));
                    v2 = (int) Math.floor(MathTools.getLinearInterpolatedY(
                        floor, colorRamp[floor][1], ceil,
                        colorRamp[ceil][1], v));
                    v3 = (int) Math.floor(MathTools.getLinearInterpolatedY(
                        floor, colorRamp[floor][2], ceil,
                        colorRamp[ceil][2], v));
                    // interp++;
                }
                // double v1 = colorRamp[(int)floor][0];
                // double v2 = colorRamp[(int)floor][1];
                // double v3 = colorRamp[(int)floor][2];
                try {
                    // if (Double.isNaN(v1) || Double.isNaN(v2)
                    // || Double.isNaN(v3)) {
                    // log.warn("Value of one of {},{},{} is NaN",
                    // new Object[] { v1, v2, v3 });
                    // }
                    if (floor >= t) {
                        w.setPixel(i, j, new int[]{
                            // Double.isNaN(v1) ? colorRamp[0][0] : v1,
                            // Double.isNaN(v2) ? colorRamp[0][1] : v2,
                            // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
                            v1, v2, v3});// colorRamp[floor][0],
                        // colorRamp[floor][1],
                        // colorRamp[floor][2] });
                    } else {
                        w.setPixel(i, j, new int[]{
                            // Double.isNaN(v1) ? colorRamp[0][0] : v1,
                            // Double.isNaN(v2) ? colorRamp[0][1] : v2,
                            // Double.isNaN(v3) ? colorRamp[0][2] : v3 });
                            colorRamp[0][0], colorRamp[0][1],
                            colorRamp[0][2]});
                    }
                } catch (final ArrayIndexOutOfBoundsException aio) {
                    ImageTools.log.error("Index out of bounds at {},{}", i, j);
                    throw aio;
                }
            }
        }
        ImageTools.log.debug("Maxval: {}  minval: {}", maxval, minval);
    }

    public static BufferedImage makeImage2D(final Array matrix,
        final int nsamples) {
        if (matrix.getRank() != 2) {
            throw new IllegalArgumentException(
                "Method only accepts arrays of rank 2");
        }
        MinMax mm = MAMath.getMinMax(matrix);
        MinMaxNormalizationFilter mmnf = new MinMaxNormalizationFilter(mm.min,
            mm.max);
        Array img = (ArrayDouble.D2) mmnf.apply(matrix);
        double[] sampleTable = createSampleTable(nsamples);
        // double[] breakpoints = getBreakpoints(img, 256,
        // Double.NEGATIVE_INFINITY);
        int width = matrix.getShape()[0];
        int height = matrix.getShape()[1];
        BufferedImage bi = createCompatibleImage(width, height,
            Transparency.TRANSLUCENT);
        final Index idx = img.getIndex();
        WritableRaster wr = bi.getRaster();
        final float[] colors = new float[4];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                float v = 0.0f;
                v = (float) sampleTable[((int) Math.floor(img.getFloat(idx.set(
                    i, j))
                    * (nsamples - 1)))];
                // v = (float) ImageTools.getSample(sampleTable, breakpoints, );
                colors[0] = v * 255.0f;
                colors[1] = v * 255.0f;
                colors[2] = v * 255.0f;
                colors[3] = 1.0f * 255.0f;
                wr.setPixel(i, j, colors);
            }
        }
        return bi;
    }

    /**
     *
     * @param bim       the image to save
     * @param imgname   the filename
     * @param format    the format, e.g. "png", "jpeg"
     * @param outputDir
     * @param iw        may be null
     * @param resources resources, which were used to create the image
     */
    public static File saveImage(final RenderedImage bim, final String imgname,
        final String format, final File outputDir,
        final IWorkflowElement iw, final IFileFragment... resources) {
        final String name = StringTools.removeFileExt(imgname);
        File out = null;
        if (name.contains(File.separator)) {
            throw new IllegalArgumentException(
                "Name of chromatogram must not include file separator character!");
        }
        out = new File(outputDir, imgname + "." + format);
        if (iw != null) {
            final DefaultWorkflowResult dwr = new DefaultWorkflowResult(out,
                iw, WorkflowSlot.VISUALIZATION, resources);
            iw.getWorkflow().append(dwr);
        }
        try {
            ImageTools.log.info("Saving image to " + out.getAbsolutePath());

            ImageIO.write(bim, format, out);
        } catch (final IOException e) {
            ImageTools.log.error(e.getLocalizedMessage());
        }

        return out;
    }

    public static void writeImage(final JFreeChart chart, final File file,
        final int imgwidth, final int imgheight) {
        try {
            final String ext = StringTools.getFileExtension(
                file.getAbsolutePath());
            if (ext.equalsIgnoreCase("svg")) {
//                ImageTools.log.info("Saving to file {}", file.getAbsolutePath());
//                final FileOutputStream fos = new FileOutputStream(file);
//                ImageTools.writeSVG(chart, fos, imgwidth, imgheight);
                ImageTools.log.info("svg output currently not supported, reverting to png!");
                final File f = file;
                f.getParentFile().mkdir();
                ImageTools.log.info("Saving to file {}", f.getAbsolutePath());
                final FileOutputStream fos = new FileOutputStream(f);
                ImageTools.writePNG(chart, fos, imgwidth, imgheight);
            } else if (ext.equalsIgnoreCase("png")) {// use png as default
                final File f = file;
                f.getParentFile().mkdir();
                ImageTools.log.info("Saving to file {}", f.getAbsolutePath());
                final FileOutputStream fos = new FileOutputStream(f);
                ImageTools.writePNG(chart, fos, imgwidth, imgheight);
            } else {
                if (ext.isEmpty() || ext.equals(file.getAbsolutePath())) {
                    ImageTools.log.info("Using default image format png");
                } else {
                    ImageTools.log.warn("Cannot handle image of type " + ext
                        + "! Saving as png!");
                }
                final File f = new File(file.getParentFile(), StringTools.removeFileExt(file.
                    getName())
                    + ".png");
                f.getParentFile().mkdir();
                ImageTools.log.info("Saving to file {}", f.getAbsolutePath());
                final FileOutputStream fos = new FileOutputStream(f);
                ImageTools.writePNG(chart, fos, imgwidth, imgheight);
            }
        } catch (final FileNotFoundException e) {
            ImageTools.log.error(e.getLocalizedMessage());
        }
    }

    public static void writePNG(final JFreeChart chart,
        final FileOutputStream fos, final int imgwidth, final int imgheight) {
        try {
            EncoderUtil.writeBufferedImage(chart.createBufferedImage(imgwidth,
                imgheight), "png", fos);
        } catch (final IOException e) {
            ImageTools.log.error(e.getLocalizedMessage());
        }
    }

//    public static void writeSVG(final JFreeChart chart,
//            final FileOutputStream fos, final int imgwidth, final int imgheight) {
//        try {
//            // Following code is adapted from JFreeChart Developers manual
//            final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
//            final DOMImplementation domImpl = SVGDOMImplementation.
//                    getDOMImplementation();
//            // Create an instance of org.w3c.dom.Document
//            final Document document = domImpl.createDocument(svgNS, "svg", null);
//            // Create an instance of the SVG Generator
//            final SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
//            // set the precision to avoid a null pointer exception in Batik 1.5
//            svgGenerator.getGeneratorContext().setPrecision(6);
//            // Ask the chart to render into the SVG Graphics2D implementation
//            svgGenerator.setSVGCanvasSize(new Dimension(imgwidth, imgheight));
//            chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, imgwidth,
//                    imgheight), null);
//            // Finally, stream out SVG to a file using UTF-8 character to
//            // byte encoding
//            final boolean useCSS = true;
//            Writer out;
//            try {
//                out = new OutputStreamWriter(fos, "UTF-8");
//                svgGenerator.stream(out, useCSS);
//            } catch (final UnsupportedEncodingException e) {
//                ImageTools.log.error(e.getLocalizedMessage());
//            } catch (final SVGGraphics2DIOException e) {
//                ImageTools.log.error(e.getLocalizedMessage());
//            }
//        } catch (final NoClassDefFoundError cnfe) {
//            ImageTools.log.warn("Batik is not present on the classpath!");
//        }
//
//    }
}
