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
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;

/**
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
public class SimplePeakPicking implements IPeakPicking {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
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
    @Configurable(value = "1")
    private double stdPerc = 1.0d;

    @Override
    public String toString() {
        return getClass().getName();
    }

    private List<ArrayDouble.D1> getIntensities(IFileFragment ff) {
        IVariableFragment tiv = ff.getChild(this.totalIntensityVar);
        IVariableFragment ssiv = ff.getChild(this.secondScanIndexVar);
        tiv.setIndex(ssiv);
        List<ArrayDouble.D1> intensities = new ArrayList<>();

        for (Array a : tiv.getIndexedArray()) {
            intensities.add((ArrayDouble.D1) a);
        }

        return intensities;
    }

    @Override
    public List<Point> findPeaks(IFileFragment ff) {
        this.log.info("Running {} with:", this.getClass().getName());
        this.log.info("maxDx: {}, maxDy: {}", this.maxDx, this.maxDy);
        this.log.info("stdPerc: {}", this.stdPerc);

        return getPeaks(getIntensities(ff), 0, Integer.MAX_VALUE,
                this.minVerticalScanIndex, Integer.MAX_VALUE, this.maxDx,
                this.maxDy, this.stdPerc);
    }

    @Override
    public List<Point> findPeaksNear(IFileFragment ff, Point p, int dx, int dy) {
        // FIXME nicht statisch 7
        dx = Math.max(dx, 7);
        dy = Math.max(dy, 7);
        int minX = p.x - dx, maxX = p.x + dx, minY = p.y - dy, maxY = p.y + dy;
        // FIXME nicht statisch 1
        return getPeaks(getIntensities(ff), minX, maxX, minY, maxY, 2, 2,
                Double.MIN_VALUE);
    }

    private List<Point> getPeaks(List<ArrayDouble.D1> intensities, int minX,
            int maxX, int minY, int maxY, int maxdx, int maxdy, double threshold) {
        final List<Point> peaks = new ArrayList<>();
        int scanLineCount = intensities.size();
        int scansPerModulation = intensities.get(0).getShape()[0];

        double nHeight;
        double currentHeight;
        double sum = 0;
        double mu = 0, sigma = 0;
        IndexIterator iter;
        for (int x = maxdx + minX; x < Math.min(scanLineCount, maxX) - maxdx; x++) {
            if (x < intensities.size() && x >= 0) {
                iter = intensities.get(x).getIndexIterator();
                sum = 0;
                while (iter.hasNext()) {
                    sum += iter.getDoubleNext();
                }
                mu = sum / (double) scansPerModulation;
                iter = intensities.get(x).getIndexIterator();
                while (iter.hasNext()) {
                    sum += Math.pow(iter.getDoubleNext() - mu, 2);
                }
                sigma = Math.sqrt(sum / (double) scansPerModulation - 1);

                for (int y = maxdy + minY; y < Math.min(scansPerModulation,
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
                            if ((currentHeight > (mu + threshold * sigma))
                                    || threshold == Double.MIN_VALUE) {
                                peaks.add(new Point(x, y));
                            }
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
//        this.totalIntensityVar = cfg.getString(this.getClass().getName()
//                + ".totalIntensityVar", "total_intensity");
//        this.maxDx = cfg.getInt(this.getClass().getName() + ".maxDx", 1);
//        this.maxDy = cfg.getInt(this.getClass().getName() + ".maxDy", 1);
//        this.minVerticalScanIndex = cfg.getInt(this.getClass().getName()
//                + ".minVerticalScanIndex", -1);
//        this.stdPerc = cfg.getDouble(this.getClass().getName()
//                + ".peakThreshold", 1);
    }
}
