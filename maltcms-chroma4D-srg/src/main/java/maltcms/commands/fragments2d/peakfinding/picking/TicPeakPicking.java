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

import cross.datastructures.tuple.Tuple2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Data;

import maltcms.datastructures.ms.IChromatogram2D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.quadTree.QuadTree;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;

/**
 * Uses 2d peaks from variable tic_peaks, as provided by CWTPeakFinder.
 *
 * @author Nils Hoffmann
 *
 * @since 1.3.2
 */

@Data
@ServiceProvider(service = IPeakPicking.class)
public class TicPeakPicking implements IPeakPicking {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TicPeakPicking.class);

    private QuadTree<Peak2D> quadTree;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    private QuadTree<Peak2D> getQuadTree(IChromatogram2D chrom) {
        if (this.quadTree == null) {
            quadTree = new QuadTree<>(0, 0, chrom.getNumberOfModulations(), chrom.getNumberOfScansPerModulation(), 6);
            for (Peak2D p : Peak2D.fromFragment2D(chrom.getParent(), "tic_peaks")) {
                Point pt = chrom.getPointFor(p.getApexIndex());
                quadTree.put(new Point2D.Double(pt.x, pt.y), p);
            }
        }
        return quadTree;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Point> findPeaks(IChromatogram2D chrom) {
        log.info("Running {} with:", this.getClass().getName());
        List<Peak2D> peaks = Peak2D.fromFragment2D(chrom.getParent(), "tic_peaks");
        List<Point> pointList = new ArrayList<>();
        for (Peak2D peak : peaks) {
            pointList.add(chrom.getPointFor(peak.getApexIndex()));
        }
        return pointList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration cfg) {

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
        QuadTree<Peak2D> tree = getQuadTree(chrom);
        List<Tuple2D<Point2D, Peak2D>> l = tree.getChildrenInRange(new Rectangle2D.Double(p.x - dx, p.y - dy, 2 * dx, 2 * dy));
        ArrayList<Point> al = new ArrayList<>();
        for (Tuple2D<Point2D, Peak2D> t : l) {
            al.add(new Point((int) t.getSecond().getFirstScanIndex(), (int) t.getSecond().getSecondScanIndex()));
        }
        return al;
    }
}
