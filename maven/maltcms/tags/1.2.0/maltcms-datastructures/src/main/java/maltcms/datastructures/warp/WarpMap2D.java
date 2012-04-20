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

    private List<Tuple2D<Point,Point>> anchors;

    public void setWarpFrom(Image im1) {
        this.im1 = im1;
    }

    public void setWarpTo(Image im2) {
        this.im2 = im2;
    }

    public void setAnchors(Tuple2D<Point,Point>...p) {
        this.anchors = new ArrayList<Tuple2D<Point,Point>>(Arrays.asList(p));
    }

    /**
     * Calculates a warp function, based on the supplied anchors.
     * If no anchors are supplied or any of the images is null,
     * the original source image is returned unwarped.
     * @return
     */
    public Image applyWarp() {
        if(this.im1 != null && this.im2 != null && this.anchors != null) {
            
        }
        return this.im1;
    }

    class Triangulation {

        private List<Point> l;

        public Triangulation(List<Point> l) {
            this.l = l;
        }

        public void triangulate() {
            HashMap<Point,TriangulatedPoint> map = new HashMap<Point,TriangulatedPoint>();
            for(Point p:l) {
                TriangulatedPoint tp = new TriangulatedPoint();
                if(map.containsKey(p)) {
                    tp = map.get(p);
                }else{
                    map.put(p,tp);
                }
                for(Point q:l) {
                    if(p!=q) {
                        TriangulatedPoint tq = new TriangulatedPoint();
                        if(map.containsKey(q)) {
                            tq = map.get(q);
                        }else{
                            map.put(q, tq);
                        }
                        //now check distance
                    }
                }
            }
        }

    }

    class TriangulatedPoint {

        TreeMap<Double,TriangulatedPoint> neighbors = new TreeMap<Double,TriangulatedPoint>();



    }

}
