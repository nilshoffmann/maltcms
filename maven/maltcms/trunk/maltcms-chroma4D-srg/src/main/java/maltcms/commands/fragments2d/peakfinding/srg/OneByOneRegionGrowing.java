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
package maltcms.commands.fragments2d.peakfinding.srg;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.tools.ArrayTools2;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;
import ucar.ma2.ArrayDouble.D1;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.similarities.ArrayCos;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class OneByOneRegionGrowing implements IRegionGrowing {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.used_mass_values", value = "used_mass_values")
    private final String usedMassValuesVar = "used_mass_values";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private String secondScanIndexVar = "second_column_scan_index";
    @Configurable(value = "0.99d")
    private double minDistance = 0.99d;
    private IArraySimilarity similarity = new ArrayCos();
    @Configurable(value = "2")
    private int minPeakSize = 2;
    @Configurable(value = "1000")
    private int maxPeakSize = 1000;
    @Configurable(value = "false")
    private boolean filterMS = false;
    @Configurable(value = "true")
    private boolean useAlternativFiltering = true;
    @Configurable(value = "true")
    private boolean useMeanMS = false;
    private boolean discardPeaksWithMaxArea = true;
    private boolean discardPeaksWithMinArea = true;
    private long timesum;
    private int count;
    private int scansPerModulation;
    private List<Integer> hold;
    private ArrayDouble.D1 intensities;
    private IScanLine slc;
    private IFileFragment ff;

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     *
     * @param seeds
     * @param ff
     * @param slc
     * @return
     */
    @Override
    public List<PeakArea2D> getAreasFor(List<Point> seeds, IFileFragment ff,
            IScanLine slc) {

        log.info("	Using distance {} with minDist:{}",
                this.similarity.toString(), this.minDistance);

        this.count = 0;
        this.timesum = 0;

        this.slc = slc;
        this.ff = ff;
        this.intensities = (ArrayDouble.D1) ff.getChild(this.totalIntensityVar).
                getArray();
        this.scansPerModulation = slc.getScansPerModulation();

        getFilter();

        final List<PeakArea2D> peakAreaList = new ArrayList<PeakArea2D>();

        for (final Point seed : seeds) {
            final PeakArea2D s = regionGrowing(seed);
            if (s != null) {
                peakAreaList.add(s);
            }
        }

        return peakAreaList;
    }

    /**
     * Will do the region growing for on seed.
     *
     * @param seed inital seed
     * @return {@link PeakArea2D} if the peak area size exceeds a specific
     * threshold, otherwise <code>null</code>
     */
    private PeakArea2D regionGrowing(final Point seed) {
        System.out.println("Building peak area of seed " + seed);
        final long start = System.currentTimeMillis();
        final PeakArea2D pa = new PeakArea2D(seed,
                slc.getMassSpectra(seed).copy(),
                this.intensities.get(idx(seed.x, seed.y)), idx(seed.x, seed.y),
                this.scansPerModulation);
        pa.addNeighOf(seed);
        Array meanMS = pa.getMeanMS();

        while (pa.hasActivePoints()) {
            while (pa.hasActivePoints()) {
                check(pa, pa.popActivePoint(), slc, meanMS);
                if (this.useMeanMS) {
                    meanMS = pa.getMeanMS();
                }
                if (pa.size() > this.maxPeakSize) {
                    break;
                }
            }
            if (pa.size() > this.maxPeakSize) {
                log.error(
                        "			Stopping region growing: Limit of {} points/peakarea exceeded (maxPeakSize)",
                        this.maxPeakSize);
                while (pa.hasActivePoints()) {
                    pa.addBoundaryPoint(pa.popActivePoint());
                }
                break;
            }
            if (this.useMeanMS) {
                for (final Point bp : pa.getBoundaryPoints()) {
                    check(pa, bp, slc, meanMS);
                    if (this.useMeanMS) {
                        meanMS = pa.getMeanMS();
                    }
                }
            }
        }

        this.timesum += System.currentTimeMillis() - start;
        this.count++;
        if (this.count % 10 == 0) {
            log.info("		Avg time {} in {} runs",
                    this.timesum / this.count, this.count);
        }
        if (pa.size() > this.maxPeakSize) {
            if (this.discardPeaksWithMaxArea) {
                return null;
            } else {
                return pa;
            }
        }
        if (pa.getRegionPoints().size() < this.minPeakSize) {
            if (this.discardPeaksWithMinArea) {
                return null;
            } else {
                return pa;
            }
        }

//		double meanI = pa.getAreaIntensity() / (double) (pa.getRegionPoints().size() + 1);
//		double frac = pa.getSeedIntensity() / meanI; 
//		System.out.println(frac);
//		if (frac < 1.02d) {
//			return null;
//		}

        return pa;
    }

    /**
     * This method add a given point ap to the region or the boundary list.
     *
     * @param snake snake
     * @param ap active point
     * @param slc scan line cache
     * @param meanMS mean mass spectra
     */
    private void check(final PeakArea2D snake, final Point ap,
            final IScanLine slc, final Array meanMS) {
        try {// FIXME Mathias, hier geht was schief
            final Array apMS = slc.getMassSpectra(ap);
            if (isNear(meanMS, apMS)) {
                try {
                    snake.addRegionPoint(ap, apMS, this.intensities.get(idx(
                            ap.x, ap.y)));
                    snake.addNeighOf(ap);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    log.error(
                            "Tried to use point {} and access index {}, allowed : [0,{}]",
                            new Object[]{ap, idx(ap.x, ap.y),
                                this.intensities.getShape()[0] - 1});
                }
            } else {
                snake.addBoundaryPoint(ap);
            }
        } catch (IndexOutOfBoundsException ex) {
            log.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Getter.
     *
     * @param seedMS seed ms
     * @param neighMS neighbour ms
     * @return true if dist is low enough, otherwise false
     */
    private boolean isNear(final Array seedMS, final Array neighMS) {
        if ((seedMS != null) && (neighMS != null)) {
            Array sms = seedMS.copy();
            Array nms = neighMS.copy();
            if (this.filterMS) {
                sms = ArrayTools2.filter(sms, this.hold, false);
                nms = ArrayTools2.filter(nms, this.hold, false);
            }
            final double d = this.similarity.apply(sms, nms);
            // log.info("{}", d);
            return (d >= this.minDistance);
        }
        return false;
    }

    /**
     * Index map.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return index
     */
    private int idx(final int x, final int y) {
        return x * this.scansPerModulation + y;
    }

    /**
     * Tries to find the used_mass_values array and converts it to an List of
     * Integer.
     *
     * TODO: Duplicated method. See MeanVarVis - Should be one in ArrayTools?
     *
     * @param ff file fragment
     */
    private void getFilter() {
        if (this.filterMS) {
            log.info("	Filtering mass spectra");
            try {
                final Array holdA = this.ff.getChild(this.usedMassValuesVar).
                        getArray();
                final IndexIterator iter = holdA.getIndexIterator();
                this.hold = new ArrayList<Integer>();
                while (iter.hasNext()) {
                    this.hold.add(iter.getIntNext());
                }
                // FIXME: wenn cahcedlist benutzt werden, klappt das über slc
                // nicht
                log.info("		Using {} of {} masses", holdA.getShape()[0],
                        750);
            } catch (final ResourceNotAvailableException e) {
                log.error("Resource {} not available",
                        this.usedMassValuesVar);
                if (this.useAlternativFiltering) {
                    log.info("Using filter 73,74,75,147,148,149");
                    this.hold = new ArrayList<Integer>();
                    for (int i = 1; i < 751; i++) {
                        if (i != 73 && i != 74 && i != 75 && i != 147
                                && i != 148 && i != 149) {
                            this.hold.add(i);
                        }
                    }
                } else {
                    log.error("Turning off filtering.");
                    this.filterMS = false;
                }
            }
        }
    }

    /**
     *
     * @param cfg
     */
    @Override
    public void configure(Configuration cfg) {
        this.minPeakSize = cfg.getInt(this.getClass().getName()
                + ".minPeakSize", 2);
        this.useMeanMS = cfg.getBoolean(this.getClass().getName()
                + ".useMeanMS", true);
        this.maxPeakSize = cfg.getInt(this.getClass().getName()
                + ".maxPeakSize", 1000);
        this.filterMS = cfg.getBoolean(this.getClass().getName() + ".filterMS",
                false);
//        String distClass = cfg.getString(this.getClass().getName()
//                + ".distClass", "maltcms.commands.distances.ArrayCos");
//        this.similarity = Factory.getInstance().getObjectFactory().instantiate(
//                distClass, IArraySimilarity.class);
        this.minDistance = cfg.getDouble(
                this.getClass().getName() + ".minDist", 0.99d);
    }

    /**
     *
     * @return
     */
    @Override
    public double getMinDist() {
        return this.minDistance;
    }

    /**
     *
     * @return
     */
    @Override
    public List<D1> getIntensities() {
        IVariableFragment tiv = ff.getChild(this.totalIntensityVar);
		IVariableFragment ssiv = ff.getChild(this.secondScanIndexVar);
		tiv.setIndex(ssiv);
        final List<ArrayDouble.D1> intensitiesD = new ArrayList<ArrayDouble.D1>();
        for (Array a : this.ff.getChild(this.totalIntensityVar).getIndexedArray()) {
            intensitiesD.add((ArrayDouble.D1) a);
        }
        return intensitiesD;
    }
}
