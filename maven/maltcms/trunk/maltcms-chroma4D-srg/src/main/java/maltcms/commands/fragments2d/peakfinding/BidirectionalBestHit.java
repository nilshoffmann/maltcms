/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: BidirectionalBestHit.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.commands.fragments2d.peakfinding;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maltcms.datastructures.peak.Peak2D;

import org.apache.commons.configuration.Configuration;

import cross.annotations.Configurable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.math.functions.IScalarArraySimilarity;
import maltcms.math.functions.ProductSimilarity;
import maltcms.math.functions.similarities.ArrayCorr;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;

/**
 * Will compute a list of bidirectional best hits.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
public class BidirectionalBestHit implements IBidirectionalBestHit {

//    @Configurable(value = "maltcms.commands.distances.ArrayCos")
//    private String distClass = "maltcms.commands.distances.ArrayCos";
    @Configurable(value = "true")
    private boolean useMeanMS = true;
    @Configurable(value = "0.9d")
    private Double threshold = 0.9d;
    private final List<List<Peak2D>> peaklists;
    private IScalarArraySimilarity similarityFunction;
    private final List<Map<Integer, Boolean>> doneList;
    private double rt1Delta = 15.0;
    private double rt2Delta = 1.0;
    private double rtScoreThreshold = 0.0d;

    /**
     * Default constructor. Sets up all needed variables.
     */
    public BidirectionalBestHit() {
        this.peaklists = new ArrayList<List<Peak2D>>();
        this.doneList = new ArrayList<Map<Integer, Boolean>>();
        similarityFunction = new ProductSimilarity();
        GaussianDifferenceSimilarity gds = new GaussianDifferenceSimilarity();
        similarityFunction.setScalarSimilarities(gds);
        ArrayCorr ac = new ArrayCorr();
        similarityFunction.setArraySimilarities(ac);
    }

    /**
     * Adds a peak list to a internal peak list.
     * 
     * @param peakList
     *            peak list
     */
    public void addPeakLists(final List<Peak2D> peakList) {
        this.peaklists.add(peakList);
        this.doneList.add(new HashMap<Integer, Boolean>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.threshold = cfg.getDouble(
                this.getClass().getName() + ".threshold", 0.9d);
        this.useMeanMS = cfg.getBoolean(this.getClass().getName() + ".useMeanMS",
                true);
//        this.distClass = cfg.getString(this.getClass().getName()
//                + ".dist", "maltcms.commands.distances.ArrayCos");
//        this.dist = Factory.getInstance().getObjectFactory().instantiate(
//                this.distClass, IArrayDoubleComp.class);
    }

    @Override
    public double sim(Peak2D p, Peak2D np) {
        double sim = Double.NEGATIVE_INFINITY;
        if (this.useMeanMS) {
            sim = this.similarityFunction.apply(new double[]{p.getFirstRetTime(),p.getSecondRetTime()}, new double[]{np.
                    getFirstRetTime(),np.getSecondRetTime()}, p.getPeakArea().getMeanMS(), np.
                    getPeakArea().getMeanMS());
        } else {
            sim = this.similarityFunction.apply(new double[]{p.getFirstRetTime(),p.getSecondRetTime()}, new double[]{np.
                    getFirstRetTime(),np.getSecondRetTime()}, p.getPeakArea().getSeedMS(), np.
                    getPeakArea().getSeedMS());
        }
        return sim;
    }

    /**
     * Will find the best hit in list of peak p.
     * 
     * @param p
     *            peak
     * @param list
     *            list
     * @param done
     *            donelist to skip calculation
     * @return <code>-1</code> if no one was found
     */
    private int findBidiBestHist(final Peak2D p, final List<Peak2D> list,
            final Map<Integer, Boolean> done) {
        int maxI = -1;
        double max;
//        if (this.dist.minimize()) {
//            max = Double.MAX_VALUE;
//        } else {
            max = Double.MIN_VALUE;
//        }
        double sim;
        Peak2D np;
        // FIXME faster!
        for (int i = 0; i < list.size(); i++) {
            if (!done.containsKey(i)) {
                np = list.get(i);

                sim = sim(p, np);
//                if (this.dist.minimize()) {
//                    if (sim < max) {
//                        maxI = i;
//                        max = sim;
//                    }
//                } else {
                    if (sim > max) {
                        maxI = i;
                        max = sim;
                    }
//                }
            }
        }
        if (this.threshold != 0) {
//            if (this.dist.minimize()) {
//                if (max > this.threshold) {
//                    return -1;
//                }
//            } else {
                if (max < this.threshold) {
                    return -1;
                }
//            }
        }

        System.out.print(max + "-");
        return maxI;
    }

    /**
     * Getter.
     * 
     * @return a list of all bidirectional best hits. List contains the indices
     *         of peak in the peaklist.
     */
    public List<List<Point>> getBidiBestHitList() {
        log.info("Dist: {}", this.similarityFunction.toString());
        log.info("Threshold: {}", this.threshold);
        log.info("Use mean MS: {}", this.useMeanMS);

        final List<List<Point>> indexList = new ArrayList<List<Point>>();
        List<Point> bidibestlist = new ArrayList<Point>();
        for (int h = 0; h < this.peaklists.size() - 1; h++) {
            for (int i = 0; i < this.peaklists.get(h).size(); i++) {
                if (!this.doneList.get(h).containsKey(i)) {
                    int r = h + 1;
                    int l = h;
                    for (int j = 0; j < h; j++) {
                        bidibestlist.add(new Point(-1, j));
                    }
                    bidibestlist.add(new Point(i, l));
                    while (true) {
                        final int bidibestr = findBidiBestHist(this.peaklists.
                                get(l).get(i), this.peaklists.get(r),
                                this.doneList.get(r));
                        if (bidibestr != -1) {
                            final int bidibestl = findBidiBestHist(
                                    this.peaklists.get(r).get(bidibestr),
                                    this.peaklists.get(l), this.doneList.get(l));
                            if (bidibestl == i) {
                                bidibestlist.add(new Point(bidibestr, r));
                                this.doneList.get(l).put(i, true);
                                this.doneList.get(r).put(bidibestr, true);
                                l = r;
                                r++;
                            } else {
                                bidibestlist.add(new Point(-1, r));
                                r++;
                            }
                        } else {
                            bidibestlist.add(new Point(-1, r));
                            r++;
                        }
                        if (r == this.peaklists.size()) {
                            break;
                        }
                    }
                    indexList.add(bidibestlist);
                    bidibestlist = new ArrayList<Point>();
                }
            }
        }

        final int lastIndex = this.peaklists.size() - 1;
        for (int i = 0; i < this.peaklists.get(lastIndex).size(); i++) {
            if (!this.doneList.get(lastIndex).containsKey(i)) {
                bidibestlist = new ArrayList<Point>();
                for (int j = 0; j < lastIndex; j++) {
                    bidibestlist.add(new Point(-1, j));
                }
                bidibestlist.add(new Point(i, lastIndex));
                indexList.add(bidibestlist);
            }
        }

        return indexList;
    }

    /**
     * Getter.
     * 
     * @return peak list
     */
    public List<List<Peak2D>> getPeakLists() {
        return this.peaklists;
    }
}
