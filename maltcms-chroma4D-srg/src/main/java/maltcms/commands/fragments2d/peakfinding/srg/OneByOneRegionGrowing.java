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
package maltcms.commands.fragments2d.peakfinding.srg;

import cross.annotations.Configurable;
import cross.datastructures.collections.CachedReadWriteList;
import cross.datastructures.fragments.IFileFragment;
import java.awt.Point;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.ms.IChromatogram2D;
import maltcms.datastructures.peak.PeakArea2D;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * <p>
 * OneByOneRegionGrowing class.</p>
 *
 * @author Mathias Wilhelm
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
@ServiceProvider(service = IRegionGrowing.class)
public class OneByOneRegionGrowing implements IRegionGrowing {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(value = "0.99d")
    private double minDistance = 0.99d;
    private IArraySimilarity similarity = new ArrayCos();
    @Configurable(value = "2")
    private int minPeakSize = 2;
    @Configurable(value = "1000")
    private int maxPeakSize = 1000;
    @Configurable(value = "true")
    private boolean useMeanMS = false;
    private boolean discardPeaksWithMaxArea = true;
    private boolean discardPeaksWithMinArea = true;
    private long timesum;
    private int count;
    private int scansPerModulation;
    private ArrayDouble.D1 intensities;
    private IChromatogram2D chrom;
    private IFileFragment ff;

    /**
     * <p>
     * setUseAlternativeFiltering.</p>
     *
     * @param b a boolean.
     * @since 1.3.2
     */
    public void setUseAlternativeFiltering(boolean b) {
        log.warn("Parameter useAlternativeFiltering has been deprecated. Please use maltcms.commands.fragments.preprocessing.MassFilter for selective removal or inclusion of m/z,intensity pairs!");
    }

    /**
     * <p>
     * setFilterMS.</p>
     *
     * @param b a boolean.
     */
    public void setFilterMS(boolean b) {
        log.warn("Parameter filterMS has been deprecated. Please use maltcms.commands.fragments.preprocessing.MassFilter for selective removal or inclusion of m/z,intensity pairs!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PeakArea2D> getAreasFor(List<Point> seeds, IFileFragment ff,
            IChromatogram2D chrom) {

        log.info("Using distance {} with minDist:{}",
                this.similarity.toString(), this.minDistance);

        this.count = 0;
        this.timesum = 0;

        this.chrom = chrom;
        this.ff = ff;
        this.intensities = (ArrayDouble.D1) ff.getChild(this.totalIntensityVar).
                getArray();
        this.scansPerModulation = chrom.getNumberOfScansPerModulation();
        final List<PeakArea2D> peakAreaList = new CachedReadWriteList<>(ff.getName() + "-peakArea2D-cache", 100);
        int i = 0;
        int stepSize = seeds.size() / 10;
        for (final Point seed : seeds) {
            if (i % stepSize == 0 || i == seeds.size() - 1) {
                if (log.isDebugEnabled()) {
                    log.info("{}%", (int) (100.0f * ((float) (i + 1) / (float) seeds.size())));
                }
            }
            final PeakArea2D s = regionGrowing(seed);
            if (s != null) {
                peakAreaList.add(s);
            }
            i++;
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
        if (log.isDebugEnabled()) {
            log.debug("Building peak area of seed " + seed);
        }
        final long start = System.currentTimeMillis();
        Array ms = chrom.getScanLineImpl().getMassSpectrum(seed);
        if (ms == null) {
            return null;
        }
        final PeakArea2D pa = new PeakArea2D(seed,
                chrom.getScanLineImpl().getMassSpectrum(seed).copy(),
                this.intensities.get(idx(seed.x, seed.y)), idx(seed.x, seed.y),
                this.scansPerModulation);
        pa.addNeighborOf(seed);
        Array meanMS = pa.getMeanMS();

        while (pa.hasActivePoints()) {
            while (pa.hasActivePoints()) {
                Point p = pa.popActivePoint();
                if (p == null) {
                    log.warn("Active point was null!");
                    break;
                }
                try {
                    check(pa, p, chrom.getScanLineImpl(), meanMS);
                    if (this.useMeanMS) {
                        meanMS = pa.getMeanMS();
                    }
                } catch (IndexOutOfBoundsException ex) {
                    log.error("Index of out bounds for point " + p + " within region around " + pa.getSeedPoint(), ex);
                }

                if (pa.size() > this.maxPeakSize) {
                    break;
                }
            }
            if (pa.size() > this.maxPeakSize) {
                log.error(
                        "Stopping region growing: Limit of {} points/peakarea exceeded (maxPeakSize)",
                        this.maxPeakSize);
                while (pa.hasActivePoints()) {
                    pa.addBoundaryPoint(pa.popActivePoint());
                }
                break;
            }
            if (this.useMeanMS) {
                for (final Point bp : pa.getBoundaryPoints()) {
                    try {
                        check(pa, bp, chrom.getScanLineImpl(), meanMS);
                        if (this.useMeanMS) {
                            meanMS = pa.getMeanMS();
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        log.error("Index of out bounds for point " + bp + " within region around " + pa.getSeedPoint(), ex);
                    }
                }
            }

            this.timesum += System.currentTimeMillis() - start;
            this.count++;
            if (this.count % 10 == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Avg time {} in {} runs",
                            this.timesum / this.count, this.count);
                }
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
        }
        return pa;
    }

    /**
     * This method add a given point to the region or the boundary list.
     *
     * @param snake snake
     * @param ap active point
     * @param slc scan line cache
     * @param meanMS mean mass spectra
     */
    private void check(final PeakArea2D snake, final Point ap,
            final IScanLine slc, final Array meanMS) {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving ms for point ", ap);
        }
        if (ap.y >= 0 && ap.y < slc.getScansPerModulation() && ap.x >= 0 && ap.x < slc.getScanLineCount()) {
            final Array apMS = slc.getMassSpectrum(ap);
            if (isNear(similarity, meanMS, apMS, minDistance)) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding region point " + ap);
                    }
                    snake.addRegionPoint(ap, apMS, this.intensities.get(idx(
                            ap.x, ap.y)));
                    if (log.isDebugEnabled()) {
                        log.debug("Adding neighbor point " + ap);
                    }
                    snake.addNeighborOf(ap);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    log.error(
                            "Tried to use point {} and access index {}, allowed : [0,{}]",
                            new Object[]{ap, idx(ap.x, ap.y),
                                this.intensities.getShape()[0] - 1});
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Adding boundary point " + ap);
                }
                snake.addBoundaryPoint(ap);
            }
        }
    }

    /**
     *
     * @param seedMS seed ms
     * @param neighMS neighbour ms
     * @param minSimilarity
     * @return true if similarity is higher than thre, otherwise false
     */
    private boolean isNear(final IArraySimilarity similarity, final Array seedMS, final Array neighMS, final double minSimilarity) {
        if ((seedMS != null) && (neighMS != null)) {
            final double d = similarity.apply(seedMS, neighMS);
            return (d >= minSimilarity);
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
        return chrom.getScanLineImpl().mapPoint(x, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration cfg) {
        this.totalIntensityVar = cfg.getString(this.getClass().getName()
                + ".totalIntensityVar", "total_intensity");
    }

}
