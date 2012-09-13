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
package maltcms.commands.fragments2d.peakfinding.bbh;

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
//import maltcms.math.functions.ProductSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;

/**
 * Will compute a list of bidirectional best hits.
 *
 * FIXME: Es gibt fälle, bei denen ein bbh mit score 0.88 gefunden wird, obwohl
 * es nicht die gleichen peaks sind. Das sind nah beieinander liegende peaks,
 * welche in den anderen chromatogrammen nicht vorkommen. irgendwie muss das
 * fixiert werden
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
public class FastBidirectionalBestHit implements IBidirectionalBestHit {

    @Configurable(value = "true", type = boolean.class)
    private boolean useMeanMS = true;
    @Configurable(value = "0.9d", type = double.class)
    private Double threshold = 0.9d;
    @Configurable(value = "25.0d", type = double.class)
    private double maxRetDiff = 500.0d;
    private IScalarArraySimilarity similarity;
    private List<Map<Integer, Boolean>> doneList;
    private int counter = 0;
    private int fcounter = 0;

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Default constructor. Sets up all needed variables.
     */
    public FastBidirectionalBestHit() {
        this.doneList = new ArrayList<Map<Integer, Boolean>>();
//        similarity = new ProductSimilarity();
//        similarity.setArraySimilarities(new ArrayCos());
//        similarity.setScalarSimilarities(new GaussianDifferenceSimilarity());
    }

    /**
     * Adds a peak list to a internal peak list.
     *
     * @param peakList peak list
     */
    public void addPeakLists(final List<Peak2D> peakList) {
        this.doneList.add(new HashMap<Integer, Boolean>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.threshold = cfg.getDouble(
                this.getClass().getName() + ".threshold", 0.9d);
        this.useMeanMS = cfg.getBoolean(this.getClass().getName()
                + ".useMeanMS", true);
//        this.distClass = cfg.getString(
//                this.getClass().getName() + ".distClass",
//                "maltcms.commands.distances.ArrayCos");
//        this.dist = Factory.getInstance().getObjectFactory().instantiate(
//                this.distClass, IArrayDoubleComp.class);
        this.maxRetDiff = cfg.getDouble(this.getClass().getName()
                + ".maxRetDiff", 500.0d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double sim(Peak2D p, Peak2D np) {
        double sim = Double.NEGATIVE_INFINITY;
        if (this.useMeanMS) {
            sim = this.similarity.apply(new double[]{p.getFirstRetTime(), p.
                        getSecondRetTime()}, new double[]{np.getFirstRetTime(),
                        np.getSecondRetTime()}, p.getPeakArea().
                    getMeanMS(), np.getPeakArea().getMeanMS());
        } else {
            sim = this.similarity.apply(new double[]{p.getFirstRetTime(), p.
                        getSecondRetTime()}, new double[]{np.getFirstRetTime(),
                        np.getSecondRetTime()}, p.getPeakArea().
                    getSeedMS(), np.getPeakArea().getSeedMS());
        }
        return sim;
    }

    /**
     * Will find the best hit in list of peak p.
     *
     * @param p peak
     * @param list list
     * @return <code>-1</code> if no one was found
     */
    private int findBidiBestHist(final Peak2D p, final List<Peak2D> list) {
        int maxI = -1;
        double max;
        max = Double.MIN_VALUE;
        double sim;
        Peak2D np;
        double diff;
        for (int i = 0; i < list.size(); i++) {
            np = list.get(i);
            //                               200  -> 1000 - 200  =  800
            // p = 1000, maxdiff = 500, np = 500  -> 1000 - 500  =  500
            // p = 1000, maxdiff = 500, np = 1500 -> 1000 - 1500 = -500
            //                               1800 -> 1000 - 1800 = -800
            diff = p.getFirstRetTime() - np.getFirstRetTime();
            if (Math.abs(diff) < this.maxRetDiff) {
                this.counter++;
                sim = sim(p, np);
                if (sim > max) {
                    maxI = i;
                    max = sim;
                }
            } else {
                if (diff < -this.maxRetDiff) {
                    this.fcounter += list.size() - i;
                    return maxI;
                }
                this.fcounter++;
            }
        }
        if (this.threshold != 0) {

            if (max < this.threshold) {
                return -1;
            }
        }
        return maxI;
    }

    /**
     * Getter.
     *
     * @return a list of all bidirectional best hits. List contains the indices
     * of peak in the peaklist.
     */
    @Override
    public List<List<Point>> getBidiBestHitList(
            final List<List<Peak2D>> peaklists) {
        System.out.println(this.maxRetDiff);
        log.info("Dist: {}", this.similarity.toString());
        log.info("Threshold: {}", this.threshold);
        log.info("Use mean MS: {}", this.useMeanMS);

        this.doneList = new ArrayList<Map<Integer, Boolean>>(peaklists.size());
        for (int i = 0; i < peaklists.size(); i++) {
            this.doneList.add(new HashMap<Integer, Boolean>(peaklists.get(i).
                    size()));
        }

        log.info("peaklistsize {}:", peaklists.size());
        for (List<Peak2D> l : peaklists) {
            log.info("	{}", l.size());
        }

        final List<List<Point>> indexList = new ArrayList<List<Point>>();
        List<Point> bidibestlist = new ArrayList<Point>();
        int ii;
        // Runtime runtime = Runtime.getRuntime();
        int r, l;
        // int c = 0;
        int bidibestr, bidibestl;
        for (int h = 0; h < peaklists.size() - 1; h++) {
            for (int i = 0; i < peaklists.get(h).size(); i++) {
                // log.info("free memory is: {}", runtime.freeMemory());

                if (!this.doneList.get(h).containsKey(i)) {
                    r = h + 1;
                    l = h;
                    for (int j = 0; j < h; j++) {
                        bidibestlist.add(new Point(-1, j));
                    }
                    bidibestlist.add(new Point(i, l));
                    ii = i;
                    while (true) {
                        bidibestr = findBidiBestHist(peaklists.get(l).get(ii),
                                peaklists.get(r));
                        if (bidibestr != -1 && !this.doneList.get(r).containsKey(
                                bidibestr)) {
                            bidibestl = findBidiBestHist(peaklists.get(r).get(
                                    bidibestr), peaklists.get(l));
                            if (bidibestl == ii) {
                                //TODO check whether group is still a consistent bidibest hit group
                                boolean consistent = true;
                                for (Point tp : bidibestlist) {
                                    int tmpm;
                                    // what if peak bidibestr in chromatogram r has a bidibest hit in chromatogram tp.y, but one of the first ones does not have it?
                                    if (tp.x != -1 && tp.y != l) {
                                        //check tp.x peaklist against bidibestr in r
                                        tmpm = findBidiBestHist(peaklists.get(r).
                                                get(bidibestr),
                                                peaklists.get(tp.y));
                                        if (tmpm != tp.x) {
                                            consistent = false;
                                            break;
                                        }
                                    }
                                }
                                if (consistent) {
                                    bidibestlist.add(new Point(bidibestr, r));
                                    this.doneList.get(l).put(ii, true);
                                    this.doneList.get(r).put(bidibestr, true);
                                    l = r;
                                    r++;
                                    ii = bidibestr;
                                } else {
//                                                                    System.out.println("would result in an inconsistent group! will not add peak to group!");
                                    bidibestlist.add(new Point(-1, r));
                                    r++;
                                }
                            } else {
                                // is not a bidibest hit
                                bidibestlist.add(new Point(-1, r));
                                r++;
                            }
                        } else {
                            // does not have a hit, or hit is already in a peak clique
                            bidibestlist.add(new Point(-1, r));
                            r++;
                        }
                        if (r == peaklists.size()) {
                            break;
                        }
                    }
                    indexList.add(bidibestlist);
                    bidibestlist = new ArrayList<Point>();
                }
            }
        }

        for (Map<Integer, Boolean> li : this.doneList) {
            System.out.println("Donelistsize: " + li.keySet().size());
        }

        final int lastIndex = peaklists.size() - 1;
        for (int i = 0; i < peaklists.get(lastIndex).size(); i++) {
            if (!this.doneList.get(lastIndex).containsKey(i)) {
                bidibestlist = new ArrayList<Point>();
                for (int j = 0; j < lastIndex; j++) {
                    bidibestlist.add(new Point(-1, j));
                }
                bidibestlist.add(new Point(i, lastIndex));
                indexList.add(bidibestlist);
            }
        }

        log.info("Did: {}", this.counter);
        log.info("Skipped: {}", this.fcounter);

        return indexList;
    }

    @Override
    public void clear() {
//        this.dist = Factory.getInstance().getObjectFactory().instantiate(
//                this.distClass, IArrayDoubleComp.class);
        // this.peaklists = new ArrayList<List<Peak2D>>();
        this.doneList = new ArrayList<Map<Integer, Boolean>>();
        this.counter = 0;
        this.fcounter = 0;
    }
}
