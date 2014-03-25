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
import cross.datastructures.tuple.Tuple2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.datastructures.peak.Peak1D;
import maltcms.datastructures.quadTree.QuadTree;
import org.apache.commons.configuration.Configuration;

/**
 * Uses peaks from variable tic_peaks, as provided by CWTPeakFinder or
 * TICPeakFinder.
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class TicPeakPicking implements IPeakPicking {

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
    private QuadTree<Peak1D> quadTree;

    @Override
    public String toString() {
        return getClass().getName();
    }

    private QuadTree<Peak1D> getQuadTree(IFileFragment ff) {
        if (this.quadTree == null) {
            IScanLine isl = ScanLineCacheFactory.getSparseScanLineCache(ff);
            quadTree = new QuadTree<>(0, 0, isl.getScanLineCount(), isl.getScansPerModulation(), 6);
            for (Peak1D p : Peak1D.fromFragment(ff)) {
                Point pt = isl.mapIndex(p.getApexIndex());
                quadTree.put(new Point2D.Double(pt.x, pt.y), p);
            }
        }
        return quadTree;
    }

    @Override
    public List<Point> findPeaks(IFileFragment ff) {
        log.info("Running {} with:", this.getClass().getName());
        log.info("	total_intensity: {}", this.totalIntensityVar);
        List<Peak1D> peaks = Peak1D.fromFragment(ff);
        IScanLine isl = ScanLineCacheFactory.getSparseScanLineCache(ff);
        List<Point> pointList = new ArrayList<>();
        for (Peak1D peak : peaks) {
            pointList.add(isl.mapIndex(peak.getApexIndex()));
        }
        return pointList;
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

    @Override
    public List<Point> findPeaksNear(IFileFragment ff, Point p, int dx, int dy) {
        QuadTree<Peak1D> tree = getQuadTree(ff);
        List<Tuple2D<Point2D, Peak1D>> l = tree.getChildrenInRange(new Rectangle2D.Double(p.x - dx, p.y - dy, 2 * dx, 2 * dy));
        ArrayList<Point> al = new ArrayList<>();
        for (Tuple2D<Point2D, Peak1D> t : l) {
            al.add(new Point((int) t.getFirst().getX(), (int) t.getFirst().getY()));
        }
        return al;
    }

    @Override
    public void configure(Configuration cfg) {
        // this.totalIntensityVar = cfg.getString(SeededRegionGrowing.class
        // .getName()
        // + ".totalIntensityVar", "total_intensity");
//        this.totalIntensityVar = cfg.getString(this.getClass().getName()
//                + ".totalIntensityVar", "total_intensity");
//        this.totalIntensityRedoVar = cfg.getString(this.getClass().getName()
//                + ".totalIntensityRedoVar", "total_intensity");
//        this.maxDx = cfg.getInt(this.getClass().getName() + ".maxDx", 1);
//        this.maxDy = cfg.getInt(this.getClass().getName() + ".maxDy", 1);
//        this.minVerticalScanIndex = cfg.getInt(this.getClass().getName()
//                + ".minVerticalScanIndex", -1);
//        this.k = cfg.getInt(this.getClass().getName() + ".k", 100);
    }
}
