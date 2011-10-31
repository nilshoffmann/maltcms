package maltcms.commands.fragments2d.peakfinding;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.peak.PeakArea2D;


import ucar.ma2.ArrayDouble;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.math.functions.IScalarArraySimilarity;

@Slf4j
@Data
public class PeakSeparator {

    private double minDist = 0.995;

    public void startSeparationFor(List<PeakArea2D> peakAreaList,
            IScalarArraySimilarity separationSimilarity,
            IScalarArraySimilarity similarity, IScanLine slc,
            List<ArrayDouble.D1> intensities, boolean useMeanMsForSeparation) {

        log.info("Separator min dist: {}", this.minDist);

        long start = System.currentTimeMillis();
        log.info("Checking integrity:");
        PeakArea2D pa1, pa2;
        Map<PeakArea2D, List<PeakArea2D>> overlapping = new HashMap<PeakArea2D, List<PeakArea2D>>();
        for (int i = 0; i < peakAreaList.size(); i++) {
            if (i % 10 == 0) {
                log.info("	{} checked", i);
            }
            pa1 = peakAreaList.get(i);
            for (int j = i + 1; j < peakAreaList.size(); j++) {
                pa2 = peakAreaList.get(j);
                if (i != j) {
                    if (pa2.regionContains(pa1.getSeedPoint())
                            || pa1.regionContains(pa2.getSeedPoint())) {
                        if (overlapping.containsKey(pa1)) {
                            overlapping.get(pa1).add(pa2);
                            // log.info("  " + pa1.toString() + "<-" +
                            // pa2.toString());
                        } else if (overlapping.containsKey(pa2)) {
                            overlapping.get(pa2).add(pa1);
                            // log.info("  " + pa2.toString() + "<-" +
                            // pa1.toString());
                        } else {
                            boolean added = false;
                            for (PeakArea2D pa : overlapping.keySet()) {
                                if (overlapping.get(pa).contains(pa1)) {
                                    // log.info("  " + pa.toString() + "<-"
                                    // + pa2.toString());
                                    overlapping.get(pa).add(pa2);
                                    added = true;
                                } else if (overlapping.get(pa).contains(pa2)) {
                                    // log.info("  " + pa.toString() + "<-"
                                    // + pa1.toString());
                                    overlapping.get(pa).add(pa1);
                                    added = true;
                                }
                            }
                            if (!added) {
                                List<PeakArea2D> l = new ArrayList<PeakArea2D>();
                                l.add(pa2);
                                overlapping.put(pa1, l);
                                // log.info("N " + pa1.toString() + "<-" +
                                // pa2.toString());
                            }
                        }
                    }
                }
            }
        }
        // for (PeakArea2D pa : overlapping.keySet()) {
        // log.info("Peak {}:", pa.getSeedPoint());
        // for (PeakArea2D paa : overlapping.get(pa)) {
        // log.info("	{}", paa.getSeedPoint());
        // }
        // }
        log.info("Found {} overlapping classes", overlapping.size());
        int c = 0;
        // for (PeakArea2D pa : overlapping.keySet()) {
        // if (!overlapping.get(pa).contains(pa)) {
        // overlapping.get(pa).add(pa);
        // }
        // log.info("	class " + c++ + ": " + overlapping.get(pa).size()
        // + " peaks");
        // }
        log.info("Checking integrity take {} ms",
                System.currentTimeMillis()
                - start);

        start = System.currentTimeMillis();
        int separated = 0, merged = 0;
        for (PeakArea2D pa : overlapping.keySet()) {
            if (!overlapping.get(pa).contains(pa)) {
                overlapping.get(pa).add(pa);
            }
            log.info("	class " + c++ + ": " + overlapping.get(pa).size()
                    + " peaks");
            if (shouldBeSeparated(overlapping.get(pa), separationSimilarity, slc,
                    intensities, useMeanMsForSeparation)) {
                separatePeaks(overlapping.get(pa), similarity, slc, intensities);
                separated++;
            } else {
                mergePeaks(overlapping.get(pa), slc, intensities);
                merged++;
            }
            // for (PeakArea2D paa : overlapping.get(pa)) {
            // List<PeakArea2D> pas = new ArrayList<PeakArea2D>();
            // pas.add(paa);
            // List<Peak2D> lpa = createPeaklist("", pas,
            // getRetentiontime(ff), ff, false);
            // List<Array> intensities = ff.getChild(
            // this.totalIntensityVar).getIndexedArray();
            // // FIXME: should not be static!
            // intensities = intensities
            // .subList(0, intensities.size() - 2);
            // final BufferedImage biBoundary =
            // ImageTools.create2DImage(
            // ff.getName(), intensities, this.scansPerModulation,
            // this.doubleFillValue, this.threshold, colorRamp,
            // this.getClass());
            // createAndSaveImage(ImageTools.addPeakAreaToImage(
            // biBoundary, pas, new int[] { 0, 0, 0, 255 }, null,
            // new int[] { 0, 0, 0, 255, },
            // this.scansPerModulation), StringTools
            // .removeFileExt(ff.getName())
            // + "_boundary-"
            // + paa.getSeedPoint().x
            // + "-"
            // + paa.getSeedPoint().y,
            // "chromatogram with peak boundaries", lpa,
            // getRetentiontime(ff));
            // }
        }

        List<PeakArea2D> remove = new ArrayList<PeakArea2D>();
        for (PeakArea2D pa : peakAreaList) {
            if (pa.isMerged()) {
                remove.add(pa);
            }
        }
        for (PeakArea2D pa : remove) {
            peakAreaList.remove(pa);
        }

        log.info("	separated {} peak classes", separated);
        log.info("	merged {} peak classes", merged);

        log.info("Separation take {} ms", System.currentTimeMillis()
                - start);
    }

    public boolean shouldBeSeparated(List<PeakArea2D> list,
            IScalarArraySimilarity similarity, IScanLine slc,
            List<ArrayDouble.D1> intensities, boolean useMeanMs) {

        double min = Double.POSITIVE_INFINITY, c;
        double minG = Double.POSITIVE_INFINITY, g;
        PeakArea2D pa1, pa2;
        for (int i = 0; i < list.size(); i++) {
            pa1 = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                pa2 = list.get(j);
                if (useMeanMs) {
                    c = similarity.apply(new double[]{}, new double[]{}, pa1.
                            getMeanMS(), pa2.getMeanMS());
                } else {
                    //TODO should at least gibt scan index difference to distance class
                    c = similarity.apply(new double[]{}, new double[]{}, pa1.
                            getSeedMS(), pa2.getSeedMS());
                    // log.info("		Score(" + pa1.getSeedPoint().x + ", "
                    // + pa1.getSeedPoint().y + " - " + pa2.getSeedPoint().x
                    // + ", " + pa2.getSeedPoint().y + "): {}", c);
                    // g = getGeodedicDist(pa1, pa2, intensities);
                }

                g = 0;
                if (c < min) {
                    min = c;
                }
                if (g < minG) {
                    minG = g;
                }
            }
        }
        log.info("		Min: " + min + " < {} = {}", this.minDist,
                min < this.minDist);

        // return (min < this.minDist) || (minG < 0.4d);
        return (min < this.minDist);
    }

    public void separatePeaks(List<PeakArea2D> list,
            IScalarArraySimilarity similarity, IScanLine slc,
            List<ArrayDouble.D1> intensities) {

//        distance = new Array2DTimePenalized();
        // ((Array2DTimePenalized) distance).setSEcondTolerance(2.0d);
        // ((Array2DTimePenalized) distance).setFirstTolerance(50.0d);

        List<Point> area = new ArrayList<Point>();
        for (PeakArea2D pa : list) {
            for (Point p : pa.getRegionPoints()) {
                if (!area.contains(p)) {
                    area.add(p);
                }
            }
        }

        for (PeakArea2D pa : list) {
            pa.clear();
        }

        int c = 0, maxArg;
        double maxD, aD;
        double rt1diff = 0, rt2diff = 0;
        for (Point p : area) {
            c = 0;
            maxD = Double.NEGATIVE_INFINITY;
            maxArg = -1;
            for (PeakArea2D pa : list) {
//                rt1diff = Math.abs(p.x - pa.getSeedPoint().x)
//                        * (double) slc.getScansPerModulation();
//                rt2diff = Math.abs(p.y - pa.getSeedPoint().y);
                aD = similarity.apply(new double[]{p.x, p.y}, new double[]{pa.
                            getSeedPoint().x, pa.getSeedPoint().y},
                        pa.getSeedMS(), slc.getMassSpectra(p));
                if (aD > maxD) {
                    maxD = aD;
                    maxArg = c;
                }
                c++;
            }
            if (maxArg >= 0) {
                list.get(maxArg).addRegionPoint(p, slc.getMassSpectra(p),
                        intensities.get(p.x).get(p.y));
            }
        }

        for (PeakArea2D pa : list) {
            pa.findAndSetBoundary();
        }
    }

    public void mergePeaks(List<PeakArea2D> list,
            IScanLine slc, List<ArrayDouble.D1> intensities) {

        double max = Double.NEGATIVE_INFINITY;
        int maxArg = -1;
        for (int i = 0; i < list.size(); i++) {
            if (max < list.get(i).getSeedIntensity()) {
                max = list.get(i).getSeedIntensity();
                maxArg = i;
            }
        }

        PeakArea2D pa = list.get(maxArg);
        pa.getBoundaryPoints();
        for (int i = 0; i < list.size(); i++) {
            if (i != maxArg) {
                for (Point p : list.get(i).getRegionPoints()) {
                    pa.addRegionPoint(p, slc.getMassSpectra(p),
                            intensities.get(p.x).get(p.y));
                }
                list.get(i).setMerged(true);
            }
        }

        pa.findAndSetBoundary();

    }

    public void setMinDist(double minDist) {
        this.minDist = minDist;

    }

    private double e3(double x1, double y1, double i1, double x2, double y2,
            double i2) {
        return Math.sqrt(x1 * x2 + y1 * y2 + i1 * i2);
    }

    @SuppressWarnings("unused")
    private double getGeodesicDist(PeakArea2D pa1, PeakArea2D pa2,
            List<ArrayDouble.D1> intensities) {

        Point p1, p2;
        if (pa1.getSeedPoint().x < pa2.getSeedPoint().x) {
            p1 = pa1.getSeedPoint();
            p2 = pa2.getSeedPoint();
        } else {
            p1 = pa2.getSeedPoint();
            p2 = pa1.getSeedPoint();
        }

        double e3Dist = e3(p1.getX(), p1.getY(), intensities.get(p1.x).get(p1.y),
                p2.getX(), p2.getY(), intensities.get(p2.x).get(
                p2.y));

        double g1 = 0, i, li = Double.MAX_VALUE;
        int ly = -1, lx = -1;
        if (p1.x != p2.x) {
            double m = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
            double n = p1.getY() - m * p1.getX();

            int y;
            // for (int x = p1.x; x <= p2.x; x++) {
            for (int x = p1.x + 1; x < p2.x; x++) {
                y = (int) (m * x + n);
                i = intensities.get(x).get(y);
                if (i < li) {
                    li = i;
                    lx = x;
                    ly = y;
                }
                // log.info("					" + x + "," + y + ": " + i);
                // if (ly != -1) {
                // g1 += e3(x, y, i, x - 1, ly, li);
                // }
                // ly = y;
                // li = i;

            }
        } else {
            int x = p1.x;
            for (int y = Math.min(p1.y, p2.y); y <= Math.max(p1.y, p2.y); y++) {
                i = intensities.get(x).get(y);
                // log.info("					" + x + "," + y + ": " + i);
                // if (ly != -1) {
                // g1 += e3(x, y, i, x - 1, ly, li);
                // }
                // ly = y;
                // li = i;
            }
        }

        if (ly != -1) {
            g1 = e3(p1.getX(), p1.getY(), intensities.get(p1.x).get(p1.y), lx,
                    ly, li)
                    + e3(lx, ly, li, p2.getX(), p2.getY(), intensities.get(p2.x).
                    get(p2.y));
        }

        double s = e3Dist / g1;
        log.info("			e3: " + e3Dist + " g1: " + g1 + " e3/g1: " + s);

        return s;
    }
}