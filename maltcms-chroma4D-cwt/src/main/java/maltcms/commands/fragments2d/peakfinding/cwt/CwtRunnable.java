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
package maltcms.commands.fragments2d.peakfinding.cwt;

import cross.annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.FirstDerivativeFilter;
import maltcms.commands.filters.array.MultiplicationFilter;
import maltcms.commands.filters.array.wavelet.MexicanHatWaveletFilter;
import maltcms.commands.fragments2d.peakfinding.CwtChartFactory;
import maltcms.commands.fragments2d.peakfinding.picking.IPeakPicking;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.ms.Chromatogram2D;
import maltcms.datastructures.ms.IChromatogram2D;
import maltcms.datastructures.ms.IScan2D;
import maltcms.datastructures.peak.MaltcmsAnnotationFactory;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.Peak2D.Peak2DBuilder;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.datastructures.quadTree.QuadTree;
import maltcms.datastructures.quadTree.QuadTreeVisualizer;
import maltcms.datastructures.rank.Rank;
import maltcms.datastructures.rank.RankSorter;
import maltcms.datastructures.ridge.Ridge;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.tools.ImageTools;
import maltcms.ui.charts.GradientPaintScale;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.MAMath.MinMax;

/**
 * <p>
 * CwtRunnable class.</p>
 *
 * @author Nils Hoffmann
 *
 */
@ServiceProvider(service = IPeakPicking.class)
@Data
@ToString(exclude = {"ridgeTree"})
@EqualsAndHashCode(exclude = {"ridgeTree"})
@Slf4j
public class CwtRunnable implements Callable<File>, IPeakPicking, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -517839315109598824L;
    @Configurable(value = "5", description = "The minimum required scale for a ridge.")
    private int minScale = 5;
    @Configurable(value = "20", description = "The maximum scale to calculate the Continuous Wavelet Transform for.")
    private int maxScale = 20;
    @Configurable(description = "The input file URI.")
    private URI inputFile;
    @Configurable(value = "", description = "The output directory. Should be an absolute file path.")
    private String outputDir = "";
    @Configurable(name = "var.modulation_time.default",
            value = "5.0d", description = "The modulation time. Default value is var.modulation_time.default.")
    private double modulationTime = 5.0d;
    @Configurable(name = "var.scan_rate.default", type = double.class,
            value = "100.0d", description = "The scan rate. Default value is var.scan_rate.default.")
    private double scanRate = 100.0d;
    @Configurable(description = "The maximum radius around a peak to search for neighboring peaks.")
    private double radius = 10.0d;
    @Configurable(description = "The maxmimum number of neighbors expected in the given radius.")
    private int maxNeighbors = 15;
    @Configurable(description = "Whether the scaleogram image should be saved.")
    private boolean saveScaleogramImage = false;
    @Configurable(description = "Whether the quad tree image should be saved.")
    private boolean saveQuadTreeImage = false;
    @Configurable(description = "Whether the 2D TIC ridge overlay images should be saved, before and after filtering.")
    private boolean saveRidgeOverlayImages = false;
    @Configurable(description = "The maximum number of ridges to report. Actual number of ridges reported may be lower, depending on the other parameters.")
    private int maxRidges = 5000;
    @Configurable(description = "Percentile of the intensity value distribution to use as minimum intensity for peaks.")
    private int minPercentile = 95;
    @Configurable(name = "var.peak_index_list", value = "peak_index_list")
    private String peakListVar = "peak_index_list";

    private QuadTree<Ridge> ridgeTree = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public File call() {

        IFileFragment inputFragment = new FileFragment(this.inputFile);
        IFileFragment f = new FileFragment(new File(outputDir, inputFragment.getName()));
        f.addSourceFile(inputFragment);
        IVariableFragment mt = null;
        try {
            mt = f.getChild("modulation_time");
        } catch (ResourceNotAvailableException rnae) {
            mt = new VariableFragment(f, "modulation_time");
            mt.setArray(maltcms.tools.ArrayTools.factoryScalar(
                    this.modulationTime));
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
        StatsMap sm = ass.apply(new Array[]{tic})[0];
        MultiplicationFilter mf = new MultiplicationFilter(
                1.0 / (sm.get(Vars.Max.name()) - sm.get(Vars.Min.name())));
        tic = mf.apply(tic);
//		Percentile p = new Percentile(99);
        double[] tica = (double[]) tic.get1DJavaArray(double.class);
//		double ninetyFivePercent = p.evaluate(tica);
        Percentile p = new Percentile(minPercentile);
        double minPercentileValue = p.evaluate(tica);
//		log.info("95% quantile value: " + fivePercent
//			+ " 99% quantile value: " + ninetyFivePercent);

        Array sat = f.getChild("scan_acquisition_time").getArray();
//		log.info(tic.getShape()[0] + " values");
        int spm = (int) (mt.getArray().getDouble(0) * sr.getArray().getDouble(0));
        int modulations = tic.getShape()[0] / spm;
        log.info("Using " + spm
                + " scans per modulation, total modulations: " + modulations);
        List<Ridge> r = apply(f.getName(), tic, sat, f, 0, modulations, spm, minPercentileValue);//,
        //fivePercent);
        // r = findRidgeMaxima(r,tic);
        List<Peak2D> l = createPeaksForRidges(f, tic, sat, r, spm);
        saveToAnnotationsFile(f, l);
        return new File(f.getUri());
    }

    private List<Peak2D> createPeaksForRidges(IFileFragment f, Array tic,
            Array sat, List<Ridge> r, int spm) {
        int index = 0;
        List<Peak2D> p2 = new LinkedList<>();
        Chromatogram2D chrom = new Chromatogram2D(f);
        Tuple2D<Double, Double> massRange = chrom.getMassRange();
        ScanLineCacheFactory.setMinMass(massRange.getFirst());
        ScanLineCacheFactory.setMaxMass(massRange.getSecond());
        for (Ridge ridge : r) {
//			log.info("Processing Ridge " + (index + 1) + "/"
//				+ r.size());
            int scanIndex = ridge.getGlobalScanIndex();
            IScan2D scan = chrom.getScan(scanIndex);
            Peak2DBuilder builder = Peak2D.builder2D();
            Peak2D p = null;
            double apexTime = sat.getDouble(scanIndex);
            // Point pt = ic2d.getPointFor(ridge.getGlobalScanIndex());
            builder.index(index++).
            firstRetTime(scan.getFirstColumnScanAcquisitionTime()).
            secondRetTime(scan.getSecondColumnScanAcquisitionTime()).
            startIndex(scanIndex).
            apexIndex(scanIndex).
            stopIndex(scanIndex).
            file(f.getName()).
            apexIntensity(tic.getDouble(scanIndex)).
            apexTime(apexTime).
            startTime(apexTime).
            stopTime(apexTime);
            p = builder.build();
            Point2D.Double ps = getPointForRidge(ridge, spm);
            Point seed = new Point((int) ps.getX(), (int) ps.getY());
            Array ms = scan.getIntensities();
            if (ms != null) {
                PeakArea2D pa2 = new PeakArea2D(seed, ms,
                        p.getApexIntensity(), p.getApexIndex(), spm);
                p.setPeakArea(pa2);
            } else {
                log.warn("Could not retrieve mass spectrum for point " + seed);
            }
            p2.add(p);
        }
        return p2;
    }

    /**
     * Index map.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return index
     */
    private int idx(final int x, final int y) {
        return x * (int) (modulationTime * scanRate) + y;
    }

    /**
     * @param f
     * @param tic
     * @param sat
     * @param r
     */
    private void saveToAnnotationsFile(IFileFragment f, List<Peak2D> l) {
        MaltcmsAnnotationFactory maf = new MaltcmsAnnotationFactory();
        MaltcmsAnnotation ma = maf.createNewMaltcmsAnnotationType(f.getUri());
        // List<Scan2D> scans = new ArrayList<Scan2D>();
        log.info("Building scans");
        final ArrayInt.D1 peakindex = new ArrayInt.D1(l.size());
        final IndexIterator iter = peakindex.getIndexIterator();
        for (Peak2D p : l) {
            maf.addPeakAnnotation(ma, CwtPeakFinder.class.getName(), p);
            iter.setIntNext(idx(p.getPeakArea().getSeedPoint().x, p.getPeakArea().getSeedPoint().y));
        }
        File outf = new File(outputDir, StringTools.removeFileExt(f.getName())
                + ".mann.xml");
        maf.save(ma, outf);
        final IVariableFragment var = new VariableFragment(f,
                this.peakListVar);
        var.setArray(peakindex);
        Peak2D.append2D(f, new LinkedList<IPeakNormalizer>(), l, "tic_peaks");
        f.save();
    }

    private QuadTree<Ridge> getRidgeTree() {
        return this.ridgeTree;
    }

    private List<Ridge> apply(String filename, final Array arr,
            final Array sat, final IFileFragment f, final int x,
            final int modulations, final int spm,
            final double minPercentileValue) {
        MexicanHatWaveletFilter cwt = new MexicanHatWaveletFilter();

        List<Double> scales = new LinkedList<>();

        final ArrayDouble.D2 scaleogram = new ArrayDouble.D2(arr.getShape()[0],
                maxScale);
        for (int i = 1; i <= maxScale; i++) {
            double scale = ((double) i);
            // log.info("Scale: " + scale);
            cwt.setScale(scale);
            Array res = cwt.apply(arr);
            Index resI = res.getIndex();
            for (int j = 0; j < res.getShape()[0]; j++) {
                scaleogram.set(j, i - 1, res.getDouble(resI.set(j)));
            }
            scales.add(scale);
        }
        List<Ridge> ridges = followRidgesBottomUp(minPercentileValue,
                scaleogram, scales, minScale, maxScale);
        if (saveScaleogramImage) {
            saveScaleogramImage(f, scales, scaleogram, arr, sat);
        }
        if (saveRidgeOverlayImages) {
            BufferedImage bi1 = createRidgeOverlayImage(
                    StringTools.removeFileExt(filename), "allRidges", arr,
                    modulations, spm, ridges);
        }
        Rectangle2D.Double boundingBox = getBoundingBox(ridges, spm);
        QuadTree<Ridge> qr = getQuadTree(ridges, boundingBox, spm);
        if (saveQuadTreeImage) {
            QuadTreeVisualizer qtv = new QuadTreeVisualizer();
            RenderedImage qtimg = qtv.createImage(qr);//, modulationTime, Math.ceil(1.0 / scanRate));
            String fname = StringTools.removeFileExt(filename);
            File dir = new File(outputDir, fname);
            dir.mkdirs();
            ImageTools.saveImage(ImageTools.flipVertical(qtimg), "quad-tree", "png",
                    dir, null);
        }
        List<Rank<Ridge>> ranks = new LinkedList<>();
        for (Ridge r : ridges) {
            ranks.add(new Rank<>(r));
        }
        filterByRidgeCost(ranks);
        ranks = filterByRidgeNeighborhood(ranks, radius, spm, qr, maxNeighbors);
        Collections.sort(ranks);

        ridges = filterRidges(ranks, maxRidges);

        if (saveRidgeOverlayImages) {
            BufferedImage bi4 = createRidgeOverlayImage(
                    StringTools.removeFileExt(filename),
                    "afterRidgeResponseMaxKFilter", arr, modulations, spm, ridges);
        }
        log.info("Found " + ridges.size() + " ridges at maxScale="
                + maxScale);
        this.ridgeTree = getQuadTree(ridges, boundingBox, spm);
        return ridges;
    }

    private List<Ridge> filterRidges(List<Rank<Ridge>> l, int topk) {
        RankSorter rs = new RankSorter(l);
        String[] fields = new String[]{
            "ridgeNeighborhood"};
        rs.sortToOrder(Arrays.asList(fields), l);
        LinkedList<Ridge> ridges = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(l.size(), topk); i++) {
            Rank<Ridge> rank = l.get(i);
            sb.append("Rank ").append(i + 1).append("/").append(topk).append(": ");
            for (int j = fields.length - 1; j >= 0; j--) {
                sb.append("[").append(fields[j]).append(": ").append(rank.getRank(fields[j])).append("]");
            }
            sb.append("\n\r");
            ridges.add(rank.getRidge());
        }
        log.info(sb.toString());
        return ridges;
    }

    private void filterByRidgeCost(List<Rank<Ridge>> ranks) {
        for (Rank<Ridge> rank : ranks) {
            Ridge r = rank.getRidge();
            double penalty = r.getRidgeCost();
            rank.addRank("ridgeCost", penalty);
        }
    }

    private List<Rank<Ridge>> filterByRidgeNeighborhood(List<Rank<Ridge>> r, double i,
            int spm, QuadTree<Ridge> qt, int threshold) {
        double[] vals = new double[r.size()];
        log.info("Using threshold: " + threshold);
        int cnt = 0;
        for (Rank<Ridge> rank : r) {
            Ridge ridge = rank.getRidge();
            Point2D root = ridge.getRidgePoints().get(0).getFirst();
            double x = root.getX() / spm;
            double y = root.getX() % spm;
            double v = qt.getNeighborsInRadius(new Point2D.Double(x, y), i).size();
            vals[cnt] = v;
            cnt++;
        }
        LinkedHashSet<Rank<Ridge>> filtered = new LinkedHashSet<>();
        for (int k = 0; k < vals.length; k++) {
            if (vals[k] <= threshold) {// keep
                r.get(k).addRank("ridgeNeighborhood", vals[k]);
                filtered.add(r.get(k));
            } else {
                r.get(k).addRank("ridgeNeighborhood", vals[k]);
            }

        }
        return new ArrayList<>(filtered);
    }

//	private void exportPeaks(String name, List<Ridge> r, Array tic, Array sat,
//		IFileFragment f) {
//		int index = 0;
//		Index tidx = tic.getIndex();
//		Index sidx = sat.getIndex();
//		List<Peak2D> peaks = new ArrayList<Peak2D>(r.size());
//		for (Ridge ridge : r) {
//			Peak2D p = new Peak2D();
//			// Point pt = ic2d.getPointFor(ridge.getGlobalScanIndex());
//			p.setIndex(index++);
//			// Scan2D s2d = ic2d.getScan(ridge.getGlobalScanIndex());
//			// scans.add(s2d);
//			p.setApexIndex(ridge.getGlobalScanIndex());
//			p.setFile(f.getName());
//			p.setApexIntensity(tic.getDouble(tidx.set(p.getApexIndex())));
//			p.setApexTime(sat.getDouble(sidx.set(p.getApexIndex())));
//		}
//		PeakExporter pe = new PeakExporter();
//		DefaultWorkflow dw = new DefaultWorkflow();
//		pe.setWorkflow(dw);
//		pe.exportPeakInformation(name, peaks);
//	}
    private List<Integer> getPeakMaxima(ArrayDouble.D2 scaleogram, int row) {
        double[] scaleResponse = (double[]) scaleogram.slice(1, row).
                get1DJavaArray(double.class);
        FirstDerivativeFilter fdf = new FirstDerivativeFilter();
        double[] res = (double[]) fdf.apply(Array.factory(scaleResponse)).
                get1DJavaArray(double.class);
        List<Integer> peakMaxima = new LinkedList<>();
        for (int i = 1; i < scaleResponse.length - 1; i++) {
            if (res[i - 1] >= 0 && res[i + 1] <= 0) {
                // remove peaks, which are not true maxima
                peakMaxima.add(i);
            }
        }
        return peakMaxima;
    }

    private List<Ridge> followRidgesBottomUp(double minPercentileValue,
            ArrayDouble.D2 scaleogram, List<Double> scales, int minScale,
            int maxScale) {
        int columns = scaleogram.getShape()[0];
        // get peak maxima for first scale
        List<Integer> seeds = getPeakMaxima(scaleogram, 0);
        HashMap<Integer, Ridge> ridges = buildRidges(seeds, 0, scaleogram);
        // build array for maxima
        for (int i = 1; i < maxScale; i++) {
            List<Integer> maxima = getPeakMaxima(scaleogram, i);
            int scaleDiff = 1;
            // log.info("Checking scale " + scales.get(i)
            // + " with max trace diff " + scaleDiff);
            double[] newSeedlings = fillSeeds(columns, maxima, scaleogram, i);
            List<Integer> ridgesToRemove = new LinkedList<>();
            for (Integer key : ridges.keySet()) {
                Ridge r = ridges.get(key);
                if (r.addPoint(scaleDiff, i, newSeedlings)) {
                    // log.info("Extended ridge: " + r);
                } else {
                    // log.info("Marking ridge for removal: " + r);
                    ridgesToRemove.add(key);
                }
            }
            for (Integer key : ridgesToRemove) {
                // log.info("Removing ridge: " + ridges.get(key));
                Ridge r = ridges.get(key);
                if (r.getSize() < minScale) {
                    ridges.remove(key);
                }
            }
            ridgesToRemove.clear();
            // log.info("Maxima at scale: " + maxima);
            // ridges.put(Integer.valueOf(i),findDiffs(seedlings,newSeedlings,i));
        }
        // put all Ridges with size>=maxScale into return list
        List<Ridge> l = new LinkedList<>();
        for (Integer key : ridges.keySet()) {
            Ridge r = ridges.get(key);
            if (r.getSize() >= minScale && r.getRidgePoints().get(0).getSecond() >= minPercentileValue) {
                // log.info("RidgePenalty: " + r.getRidgePenalty());
                l.add(r);
            }
        }
        log.info("Found " + l.size() + " initial ridges.");
        return l;
    }

    private HashMap<Integer, Ridge> buildRidges(List<Integer> seeds,
            int scaleIdx, ArrayDouble.D2 scaleogram) {
        // log.info("Peak maxima: "+seeds);
        HashMap<Integer, Ridge> l = new LinkedHashMap<>();
        for (Integer itg : seeds) {
            Ridge r = new Ridge(new Point2D.Double(itg, scaleIdx),
                    scaleogram.get(itg, scaleIdx));
            // log.info("Adding ridge: "+r);
            l.put(itg, r);
        }
        return l;
    }

    private double[] fillSeeds(int size, List<Integer> seeds,
            ArrayDouble.D2 scaleogram, int scaleIdx) {
        double[] b = new double[size];
        for (Integer itg : seeds) {
            int idx = itg;
            b[idx] = scaleogram.get(idx, scaleIdx);
        }
        return b;
    }

    /**
     * @param filename the filename
     * @param prefix an arbitrary prefix for the filename
     * @param arr the 1D TIC value array
     * @param modulations number of modulations
     * @param spm scans per modulation
     * @param ridges the ridges
     * @return a buffered image of the 2D tic with overlayed peak positions
     */
    private BufferedImage createRidgeOverlayImage(final String filename,
            final String prefix, final Array arr, final int modulations,
            final int spm, List<Ridge> ridges) {
        Index aidx = arr.getIndex();
        ArrayDouble.D2 heatmap = new ArrayDouble.D2(modulations, spm);
        for (int i = 0; i < modulations; i++) {
            for (int j = 0; j < spm; j++) {
                heatmap.set(i, j, arr.getDouble(aidx.set((i * spm) + j)));
            }
        }
        BufferedImage hmImg = CwtChartFactory.createAdaptiveColorHeatmap(heatmap);
        Graphics2D hmg2 = hmImg.createGraphics();
        for (Ridge r : ridges) {
            hmg2.setColor(Color.BLACK);
            Point2D p = r.getRidgePoints().get(0).getFirst();
            double xl = Math.floor(p.getX() / spm);
            double yl = p.getX() - (xl * spm);
            Rectangle2D.Double r2d = new Rectangle2D.Double(xl, yl, 1, 1);
            hmg2.fill(r2d);
        }
        String fname = StringTools.removeFileExt(filename);
        File dir = new File(outputDir, fname);
        dir.mkdirs();
        ImageTools.saveImage(ImageTools.flipVertical(hmImg), prefix + "-simp-with-ridgeseeds", "png",
                dir, null);
        return hmImg;
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
                log.info("Writing scaleogram to " + selection.length
                        + " parts");
                for (int j = 0; j < selection.length; j++) {
                    int partWidth = spm;
                    int i = selection[j];
                    if ((i * partWidth) + partWidth > width) {
                        partWidth = width - (i * partWidth);
                    }
                    // log.info("Part " + (i + 1) + "/" + parts +
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

                        e1.printStackTrace();
                    }
                }
            } else {
                log.info("Writing scaleogram to " + parts + " parts");
                for (int i = 0; i < parts; i++) {
                    int partWidth = spm;
                    if ((i * partWidth) + partWidth > width) {
                        partWidth = width - (i * partWidth);
                    }
                    // log.info("Part " + (i + 1) + "/" + parts +
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

                        e1.printStackTrace();
                    }
                }
            }
        } else {
            try {
                String fname = StringTools.removeFileExt(filename);
                File dir = new File(outputDir, fname);
                dir.mkdirs();
                ImageIO.write(ImageTools.flipVertical(bi), "PNG", new File(dir, "ridges.png"));
            } catch (IOException e1) {

                e1.printStackTrace();
            }
        }
    }

    /**
     * @param f the file fragment
     * @param scales the scales
     * @param scaleImages the scale images (scales->rows, values->cols)
     * @param signalValues the original signal values
     * @param domainValues the original domain values, e.g. time
     */
    private void saveScaleogramImage(IFileFragment f,
            List<Double> scales, ArrayDouble.D2 scaleImages,
            Array signalValues, Array domainValues) {
        NumberAxis domainAxis = new NumberAxis("Retention Time");
        domainAxis.setAutoRangeIncludesZero(false);
        NumberAxis scalesAxis = new NumberAxis("Scale");
        scalesAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        scalesAxis.setAutoRangeIncludesZero(false);
        //scales
        DefaultXYZDataset scalesDataset = new DefaultXYZDataset();
        double[][] scalesData = new double[3][];
        double minScaleValue = Double.POSITIVE_INFINITY;
        double maxScaleValue = Double.NEGATIVE_INFINITY;
        MinMax scaleResponse = MAMath.getMinMax(scaleImages);
        MultiplicationFilter mf = new MultiplicationFilter(1.0d / (scaleResponse.max - scaleResponse.min));
        ArrayDouble.D2 normalizedScaleImages = (ArrayDouble.D2) mf.apply(scaleImages);
        scalesData[0] = new double[domainValues.getShape()[0] * scales.size()];
        scalesData[1] = new double[signalValues.getShape()[0] * scales.size()];
        scalesData[2] = new double[signalValues.getShape()[0] * scales.size()];
        int idx = 0;
        for (int i = 0; i < scales.size(); i++) {
            for (int j = 0; j < domainValues.getShape()[0]; j++) {
                scalesData[0][idx] = domainValues.getDouble(j);
                scalesData[1][idx] = scales.get(i);
                scalesData[2][idx] = normalizedScaleImages.get(j, i);
                minScaleValue = Math.min(minScaleValue, scalesData[2][idx]);
                maxScaleValue = Math.max(maxScaleValue, scalesData[2][idx]);
                idx++;
            }
        }
        scalesDataset.addSeries("Signal", scalesData);
        XYBlockRenderer xybr = new XYBlockRenderer();
        GradientPaintScale gps = new GradientPaintScale(ImageTools.createSampleTable(4096), minScaleValue, maxScaleValue, new Color[]{Color.BLUE, Color.MAGENTA, Color.RED, Color.ORANGE, Color.YELLOW, Color.WHITE});
        xybr.setPaintScale(gps);
        xybr.setBlockHeight(1.0);
        double sar = f.getChild("scan_rate").getArray().getDouble(0);
        xybr.setBlockWidth(1.0d / sar);
        PaintScaleLegend psl = new PaintScaleLegend(gps, new NumberAxis("Response"));
        XYPlot scalePlot = new XYPlot(scalesDataset, domainAxis, scalesAxis, xybr);
        //signal
        NumberAxis signalValueAxis = new NumberAxis("Intensity");
        DefaultXYDataset signalDataset = new DefaultXYDataset();
        double[][] signalData = new double[2][];
        signalData[0] = new double[domainValues.getShape()[0]];
        signalData[1] = new double[signalValues.getShape()[0]];
        for (int j = 0; j < domainValues.getShape()[0]; j++) {
            signalData[0][j] = domainValues.getDouble(j);
            signalData[1][j] = signalValues.getDouble(j);
        }
        signalDataset.addSeries("Signal", signalData);
        XYPlot signalPlot = new XYPlot(signalDataset, domainAxis, signalValueAxis, new XYLineAndShapeRenderer(true, false));

        CombinedDomainXYPlot cd = new CombinedDomainXYPlot(domainAxis);
        cd.add(scalePlot, 4);
        cd.add(signalPlot, 2);
//		domainAxis.setRange(3100, 3150);
        savePlot(cd, psl, new File(outputDir, StringTools.removeFileExt(f.getName())), "scaleogram.png", "Scaleogram of " + f.getName());
    }

    private void savePlot(XYPlot p, PaintScaleLegend psl, File outputDir, String filename, String title) {
        final StandardChartTheme sct = (StandardChartTheme) StandardChartTheme
                .createLegacyTheme();
        final Font elf = new Font("Lucida Sans", Font.BOLD, 22);
        final Font lf = new Font("Lucida Sans", Font.BOLD, 16);
        final Font rf = new Font("Lucida Sans", Font.PLAIN, 14);
        final Font sf = new Font("Lucida Sans", Font.PLAIN, 12);
        // color of outside of chart
        sct.setChartBackgroundPaint(Color.WHITE);
        sct.setDomainGridlinePaint(Color.LIGHT_GRAY);
        // background of chart???
        sct.setChartBackgroundPaint(Color.WHITE);
        sct.setLegendBackgroundPaint(Color.WHITE);
        // background of plot
        sct.setPlotBackgroundPaint(Color.WHITE);
        sct.setRangeGridlinePaint(Color.DARK_GRAY);
        sct.setExtraLargeFont(elf);
        sct.setLargeFont(lf);
        sct.setRegularFont(rf);
        sct.setSmallFont(sf);
//		p.setInsets(RectangleInsets.ZERO_INSETS);
        final JFreeChart jfc = new JFreeChart(p);
        sct.apply(jfc);
        jfc.addSubtitle(psl);

        jfc.setAntiAlias(true);
        jfc.setTitle(title);
        jfc.setBackgroundPaint(Color.WHITE);
        outputDir.mkdirs();
        File outputFile = new File(outputDir, filename);
        log.info("Writing scaleogram to file " + outputFile);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputFile);
            try {
                EncoderUtil.writeBufferedImage(jfc.createBufferedImage(1600,
                        1024), "png", fos);
            } catch (final IOException e) {
                Logger.getLogger(CwtRunnable.class.getName()).log(Level.SEVERE, null, e);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CwtRunnable.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(CwtRunnable.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//		ImageTools.writeImage(jfc, outputFile, 1600, 1024);
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
        QuadTree<Ridge> qt = new QuadTree<>(boundingBox.x, boundingBox.y,
                boundingBox.width + 1, boundingBox.height + 1, 5);
        for (Ridge r : ridges) {
            qt.put(getPointForRidge(r, spm), r);
        }
        return qt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point> findPeaks(IChromatogram2D chrom) {
        this.inputFile = chrom.getParent().getUri();
        call();
        List<Point> l = new ArrayList<>(ridgeTree.size());
        Iterator<Tuple2D<Point2D, Ridge>> iter = ridgeTree.iterator();
        while (iter.hasNext()) {
            Ridge r = iter.next().getSecond();
            Point2D p2 = r.getRidgePoints().get(0).getFirst();
            l.add(new Point((int) p2.getX(), (int) p2.getY()));
        }
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point> findPeaksNear(IChromatogram2D chrom, Point p, int dx, int dy) {
        if (this.ridgeTree == null) {
            findPeaks(chrom);
        }
        List<Tuple2D<Point2D, Ridge>> l = ridgeTree.getNeighborsInRadius(p,
                Math.max(dx, dy));
        List<Point> list = new ArrayList<>(l.size());
        for (Tuple2D<Point2D, Ridge> tpl : l) {
            Point2D p2 = tpl.getFirst();
            list.add(new Point((int) p2.getX(), (int) p2.getY()));
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration pc) {

    }
}
