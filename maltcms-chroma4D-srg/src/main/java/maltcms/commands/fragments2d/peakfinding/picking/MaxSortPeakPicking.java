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
package maltcms.commands.fragments2d.peakfinding.picking;

import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.IChromatogram2D;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * <p>
 * MaxSortPeakPicking class.</p>
 *
 * @author Mathias Wilhelm
 *
 */
@Slf4j
@Data
@ServiceProvider(service = IPeakPicking.class)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    private List<ArrayDouble.D1> getIntensities(IFileFragment ff,
            final String localTotalIntensityVar) {
        IVariableFragment ltiv = ff.getChild(localTotalIntensityVar);
        IVariableFragment sciv = ff.getChild(this.secondScanIndexVar);
        ltiv.setIndex(sciv);
        List<ArrayDouble.D1> intensities = new ArrayList<>();

        for (Array a : ltiv.getIndexedArray()) {
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

        List<Point> finalPeaks = new ArrayList<>();
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
    public List<Point> findPeaks(IChromatogram2D chrom) {
        log.info("Running {} with:", this.getClass().getName());
        log.info("	total_intensity: {}", this.totalIntensityVar);
        log.info("	maxDx: {}, maxDy: {}", this.maxDx, this.maxDy);
        log.info("	k: {}", this.k);

        final List<ArrayDouble.D1> intensities = getIntensities(chrom.getParent(),
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
    public List<Point> findPeaksNear(IChromatogram2D chrom, Point p, int dx, int dy) {
        dx = Math.max(dx, 3);
        dy = Math.max(dy, 8);
        int minX = p.x - dx, maxX = p.x + dx, minY = p.y - dy, maxY = p.y + dy;
        final List<ArrayDouble.D1> intensities = getIntensities(chrom.getParent(),
                this.totalIntensityRedoVar);
        List<Point> peaks = getPeaks(intensities, minX, maxX, minY, maxY, 1, 1);
        return getKMax(intensities, peaks, dx * dy);
    }

    private List<Point> getPeaks(List<ArrayDouble.D1> intensities, int minX,
            int maxX, int minY, int maxY, int maxdx, int maxdy) {
        final List<Point> peaks = new ArrayList<>();
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
        this.secondScanIndexVar = cfg.getString(this.getClass().getName()
                + ".second_column_scan_index", "second_column_scan_index");
        this.totalIntensityVar = cfg.getString(this.getClass().getName()
                + ".totalIntensityVar", "total_intensity");
        this.totalIntensityRedoVar = cfg.getString(this.getClass().getName()
                + ".totalIntensityRedoVar", "total_intensity");
    }
}
