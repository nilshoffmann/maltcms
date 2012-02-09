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
package maltcms.commands.fragments2d.peakfinding.picking;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
public class MaxSortPeakPicking implements IPeakPicking {

    @Configurable(name = "totalIntensityRedoVar", value = "total_intensity")
    private String totalIntensityRedoVar = "total_intensity";
    @Configurable(name = "totalIntensityVar", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private String secondScanIndexVar = "second_column_scan_index";
    @Configurable(value = "1")
    private int maxDx = 1;
    @Configurable(value = "1")
    private int maxDy = 1;
    @Configurable(value = "-1")
    private int minVerticalScanIndex = -1;
    @Configurable(value = "100")
    private int k = 100;

    @Override
    public String toString() {
        return getClass().getName();
    }

    private List<ArrayDouble.D1> getIntensities(IFileFragment ff,
            final String localTotalIntensityVar) {
        ff.getChild(localTotalIntensityVar).setIndex(
                ff.getChild(this.secondScanIndexVar));
        List<ArrayDouble.D1> intensities = new ArrayList<ArrayDouble.D1>();

        for (Array a : ff.getChild(localTotalIntensityVar).getIndexedArray()) {
            intensities.add((ArrayDouble.D1) a);
        }

        return intensities;
    }

    private List<Point> getKMax(final List<ArrayDouble.D1> intensities,
            List<Point> peaks, int count) {
        Comparator<Point> c = new Comparator<Point>() {

            @Override
            public int compare(Point o1, Point o2) {
                return -1
                        * Double.compare(intensities.get(o1.x).get(o1.y),
                        intensities.get(o2.x).get(o2.y));
            }
        };

        Collections.sort(peaks, c);

        List<Point> finalPeaks = new ArrayList<Point>();
        for (int i = 0; i < Math.min(count, peaks.size()); i++) {
            finalPeaks.add(peaks.get(i));
        }
        
        Collections.sort(finalPeaks, new PointComparator());

        return finalPeaks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point> findPeaks(IFileFragment ff) {
        log.info("Running {} with:", this.getClass().getName());
        log.info("	total_intensity: {}", this.totalIntensityVar);
        log.info("	maxDx: {}, maxDy: {}", this.maxDx, this.maxDy);
        log.info("	k: {}", this.k);

        final List<ArrayDouble.D1> intensities = getIntensities(ff,
                this.totalIntensityVar);
        List<Point> peaks = getPeaks(intensities, 0, Integer.MAX_VALUE,
                this.minVerticalScanIndex, Integer.MAX_VALUE, this.maxDx,
                this.maxDy);
        return getKMax(intensities, peaks, this.k);
    }
    
    public class PointComparator implements Comparator<Point> {

        @Override
        public int compare(Point p1, Point p2) {
            if (Double.compare(p1.x, p2.x) == 0) {
                return Double.compare(p1.y, p2.y);
            } else {
                return Double.compare(p1.x, p2.x);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point> findPeaksNear(IFileFragment ff, Point p, int dx, int dy) {
        dx = Math.max(dx, 3);
        dy = Math.max(dy, 8);
        int minX = p.x - dx, maxX = p.x + dx, minY = p.y - dy, maxY = p.y + dy;
        final List<ArrayDouble.D1> intensities = getIntensities(ff,
                this.totalIntensityRedoVar);
        List<Point> peaks = getPeaks(intensities, minX, maxX, minY, maxY, 1, 1);
        return getKMax(intensities, peaks, dx * dy);
    }

    private List<Point> getPeaks(List<ArrayDouble.D1> intensities, int minX,
            int maxX, int minY, int maxY, int maxdx, int maxdy) {
        final List<Point> peaks = new ArrayList<Point>();
        int scanLineCount = intensities.size();
        int scansPerModulation = intensities.get(0).getShape()[0];

        double nHeight;
        double currentHeight;
        for (int x = maxdx + minX; x <= Math.min(scanLineCount, maxX) - maxdx; x++) {
            if (x < intensities.size() && x >= 0) {

                for (int y = maxdy + minY; y <= Math.min(scansPerModulation,
                        maxY)
                        - maxdy; y++) {
                    if (y < intensities.get(x).getShape()[0] && y >= 0) {
                        currentHeight = intensities.get(x).get(y);
                        boolean max = true;
                        for (int i = -maxdx; i <= maxdx; i++) {
                            for (int j = -Math.min(y, maxdy); j <= Math.min(
                                    scansPerModulation - y, maxdy); j++) {
                                if (x + i > 0
                                        && x + i < intensities.size()
                                        && y + j > 0
                                        && y + j < intensities.get(x + i).
                                        getShape()[0]) {
                                    if ((i != 0) || (j != 0)) {
                                        nHeight = (intensities.get(x + i)).get(
                                                y + j);
                                        if (currentHeight < nHeight) {
                                            max = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!max) {
                                break;
                            }
                        }

                        if (max) {
                            peaks.add(new Point(x, y));
                            // y += maxdy - 1;
                        }
                    }
                }
            }
        }
        return peaks;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration cfg) {
        // this.totalIntensityVar = cfg.getString(SeededRegionGrowing.class
        // .getName()
        // + ".totalIntensityVar", "total_intensity");
        this.totalIntensityVar = cfg.getString(this.getClass().getName()
                + ".totalIntensityVar", "total_intensity");
        this.totalIntensityRedoVar = cfg.getString(this.getClass().getName()
                + ".totalIntensityRedoVar", "total_intensity");
        this.maxDx = cfg.getInt(this.getClass().getName() + ".maxDx", 1);
        this.maxDy = cfg.getInt(this.getClass().getName() + ".maxDy", 1);
        this.minVerticalScanIndex = cfg.getInt(this.getClass().getName()
                + ".minVerticalScanIndex", -1);
        this.k = cfg.getInt(this.getClass().getName() + ".k", 100);
    }
}
