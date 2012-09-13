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
package maltcms.datastructures.warp;

import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import cross.datastructures.tuple.Tuple2D;

/**
 *
 * @author nilshoffmann
 */
public class WarpMap2D {

    private Image im1, im2;
    private List<Tuple2D<Point, Point>> anchors;

    public void setWarpFrom(Image im1) {
        this.im1 = im1;
    }

    public void setWarpTo(Image im2) {
        this.im2 = im2;
    }

    public void setAnchors(Tuple2D<Point, Point>... p) {
        this.anchors = new ArrayList<Tuple2D<Point, Point>>(Arrays.asList(p));
    }

    /**
     * Calculates a warp function, based on the supplied anchors. If no anchors
     * are supplied or any of the images is null, the original source image is
     * returned unwarped.
     *
     * @return
     */
    public Image applyWarp() {
        if (this.im1 != null && this.im2 != null && this.anchors != null) {
        }
        return this.im1;
    }

    class Triangulation {

        private List<Point> l;

        public Triangulation(List<Point> l) {
            this.l = l;
        }

        public void triangulate() {
            HashMap<Point, TriangulatedPoint> map = new HashMap<Point, TriangulatedPoint>();
            for (Point p : l) {
                TriangulatedPoint tp = new TriangulatedPoint();
                if (map.containsKey(p)) {
                    tp = map.get(p);
                } else {
                    map.put(p, tp);
                }
                for (Point q : l) {
                    if (p != q) {
                        TriangulatedPoint tq = new TriangulatedPoint();
                        if (map.containsKey(q)) {
                            tq = map.get(q);
                        } else {
                            map.put(q, tq);
                        }
                        //now check distance
                    }
                }
            }
        }
    }

    class TriangulatedPoint {

        TreeMap<Double, TriangulatedPoint> neighbors = new TreeMap<Double, TriangulatedPoint>();
    }
}
