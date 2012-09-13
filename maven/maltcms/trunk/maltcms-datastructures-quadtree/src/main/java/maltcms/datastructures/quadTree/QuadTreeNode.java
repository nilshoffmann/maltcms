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
package maltcms.datastructures.quadTree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import cross.datastructures.tuple.Tuple2D;
import cross.exception.ConstraintViolationException;
import java.awt.geom.Ellipse2D;
import java.util.Collections;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class QuadTreeNode<T> {

    private List<Tuple2D<Point2D, T>> t;
    private List<QuadTreeNode<T>> children = null;
    private final Rectangle2D.Double r;
    private final String s;
    private final int capacity;
    private final int level;

    public QuadTreeNode(double originX, double originY, double width, double height, Tuple2D<Point2D, T> t, int capacity, int level) {
        this.capacity = capacity;
        this.t = new ArrayList<Tuple2D<Point2D, T>>(this.capacity);
        this.t.add(t);
        this.r = new Rectangle2D.Double(originX, originY, width, height);
        this.level = level;
        if (!this.r.contains(t.getFirst())) {
            throw new ConstraintViolationException("Initial Point not in range of quad!");
        }
        this.s = "Node[" + level + "] x:" + this.r.x + ", y:" + this.r.y + ", width:" + this.r.width + ", height:" + this.r.height;
    }

    public List<Tuple2D<Point2D, T>> getChildrenInRadius(List<Tuple2D<Point2D, T>> children, Point2D p, double radius) {
        //System.out.println("Querying node "+toString()+ " for point: "+p.toString());
        if (this.t != null) {//this node has no children yet
            //System.out.println("Looking for Point in local list!");
            Point2D closest = null;
            Tuple2D<Point2D, T> type = null;
            double mindist = Double.POSITIVE_INFINITY;
            //iterate over local points
            for (Tuple2D<Point2D, T> tple : this.t) {
                double dist1 = l1distance(tple.getFirst(), p);//tple.getFirst().distance(p);
                //check if query is in radius
                if (dist1 <= radius) {
                    if (dist1 < mindist && !p.equals(tple.getFirst())) {
                        children.add(tple);
                    }
                }
            }
        }
        //System.out.println("Looking for Point in children!");
        if (this.children != null) {//this node has children
            //create elliptical search region
            Ellipse2D.Double e = new Ellipse2D.Double(p.getX() - radius, p.getY() - radius, 2 * radius, 2 * radius);
            Tuple2D<Point2D, T> mintup = null;
            double mindist = Double.POSITIVE_INFINITY;
            //iterate over child nodes
            for (QuadTreeNode<T> qtn : this.children) {
                if (qtn != null) {
                    //intersection with quadrant
                    if (e.intersects(qtn.getArea())) {
                        //check for closest child
                        qtn.getChildrenInRadius(children, p, radius);
                    }
                }
            }
        }
        return children;
    }

    private double l1distance(Point2D p1, Point2D p2) {
        double dist = Math.abs(p1.getX() - p2.getX());
        dist += Math.abs(p1.getY() - p2.getY());
        return dist;
    }

    public List<Tuple2D<Point2D, T>> getChildrenInRange(Rectangle2D r) throws ElementNotFoundException {
        if (!r.intersects(getArea())) {
            return Collections.emptyList();
        }
        List<Tuple2D<Point2D, T>> l = new ArrayList<Tuple2D<Point2D, T>>();
        if (this.t != null) {//this node has no children yet
            //iterate over local points
            for (Tuple2D<Point2D, T> tple : this.t) {
                if (r.contains(tple.getFirst())) {
                    l.add(tple);
                }
            }
        }
        //System.out.println("Looking for Point in children!");
        if (this.children != null) {//this node has children
            //iterate over child nodes
            for (QuadTreeNode<T> qtn : this.children) {
                if (qtn != null) {
                    l.addAll(qtn.getChildrenInRange(r));
                }
            }
        }
        return l;
    }

    public Tuple2D<Point2D, T> getClosestChild(Point2D p, double radius) throws ElementNotFoundException {
        //System.out.println("Querying node "+toString()+ " for point: "+p.toString());
        if (this.t != null) {//this node has no children yet
            //System.out.println("Looking for Point in local list!");
            Point2D closest = null;
            Tuple2D<Point2D, T> type = null;
            double mindist = Double.POSITIVE_INFINITY;
            //iterate over local points
            for (Tuple2D<Point2D, T> tple : this.t) {
                double dist1 = tple.getFirst().distance(p);
                //check if query is in radius
                if (dist1 <= radius) {
                    if (dist1 < mindist) {
                        mindist = dist1;
                        closest = tple.getFirst();
                        type = tple;
                    }
                }
            }
            if (type != null) {
                return type;
            }
        }
        //System.out.println("Looking for Point in children!");
        if (this.children != null) {//this node has children
            //create elliptical search region
            Ellipse2D.Double e = new Ellipse2D.Double(p.getX() - radius, p.getY() - radius, 2 * radius, 2 * radius);
            Tuple2D<Point2D, T> mintup = null;
            double mindist = Double.POSITIVE_INFINITY;
            //iterate over child nodes
            for (QuadTreeNode<T> qtn : this.children) {
                if (qtn != null) {
                    //intersection with quadrant
                    if (e.intersects(qtn.getArea())) {
                        //check for closest child
                        try {
                            Tuple2D<Point2D, T> tple = qtn.getClosestChild(p, radius);
                            double dist = tple.getFirst().distance(p);
                            if (dist <= radius) {
                                if (dist < mindist) {
                                    mindist = dist;
                                    mintup = tple;
                                }
                            }
                        } catch (ElementNotFoundException enfe) {
                        }
                    }
                }
            }
            if (mintup != null) {
                return mintup;
            }
        }
        throw new ElementNotFoundException();
    }

    public T getChild(Point2D p) throws ElementNotFoundException {
        //System.out.println("Querying node "+toString()+ " for point: "+p.toString());
        if (this.t != null) {//this node has no children yet
            //System.out.println("Looking for Point in local list!");
            for (Tuple2D<Point2D, T> tple : this.t) {
                if (tple.getFirst().equals(p)) {
                    return tple.getSecond();
                }
            }
        }
        //System.out.println("Looking for Point in children!");
        if (this.children != null) {//this node has children
            for (QuadTreeNode<T> qtn : this.children) {
                if (qtn != null) {
                    try {
                        if (qtn.contains(p)) {
                            return qtn.getChild(p);
                        }
                    } catch (ConstraintViolationException cve) {
                        //System.out.println("Looking for Point in children!");
                    }
                }
            }
        }
        throw new ElementNotFoundException();
    }

    @Override
    public String toString() {
        return this.s;
    }

    public boolean contains(Point2D p) {
        if (this.r.contains(p)) {
            return true;
        }
        return false;
    }

    public T remove(Point2D p) {
        if (!contains(p)) {
            return null;
        }
        if (this.t != null) {
            int i = 0;
            int ti = -1;
            for (Tuple2D<Point2D, T> tple : this.t) {
                if (p.getX() == tple.getFirst().getX() && p.getY() == tple.getFirst().getY()) {
                    ti = i;
                }
                i++;
            }
            if (ti >= 0) {
                return this.t.remove(ti).getSecond();
            }
        } else {
            return removeNode(p);
        }
        return null;
    }

    private T removeNode(Point2D p) {
        Quad q = getQuadrant(p);
        QuadTreeNode<T> node = this.children.get(q.ordinal());
        return node.remove(p);
    }

    public QuadTreeNode<T> addChild(Tuple2D<Point2D, T> tpl) {
        if (!contains(tpl.getFirst())) {
            //System.out.println("Node does not contain element, not adding!");
            return null;
        }
        //add point to local list, until capacity is reached
        if (this.t != null) {
            this.t.add(tpl);
            QuadTreeNode<T> qtn = this;
            //split, if capacity is exceeded
            if (this.t.size() == this.capacity) {
                if (this.children == null || this.children.isEmpty()) {
                    this.children = new ArrayList<QuadTreeNode<T>>(4);
                    for (int i = 0; i < 4; i++) {
                        this.children.add(null);
                    }
                }

                for (Tuple2D<Point2D, T> tple : this.t) {
                    if (tpl == tple) {
                        qtn = addNode(tple);
                    } else {
                        addNode(tple);
                    }
                }
                this.t = null;
            }
            return qtn;
        } else {//t is null, so we have children
            return addNode(tpl);
        }

    }

    private QuadTreeNode<T> addNode(Tuple2D<Point2D, T> tpl) {
        //System.out.println("Children present, trying to find quadrant!");
        Quad q = getQuadrant(tpl.getFirst());
        QuadTreeNode<T> node = this.children.get(q.ordinal());
        double width = this.r.width / 2.0d, height = this.r.height / 2.0d;
        if (node == null) {
            switch (q) {
                case NW:
                    node = new QuadTreeNode<T>(this.r.x, this.r.y + height, width, height, tpl, this.capacity, this.level + 1);
                    break;
                case NE:
                    node = new QuadTreeNode<T>(this.r.x + width, this.r.y + height, width, height, tpl, this.capacity, this.level + 1);
                    break;
                case SE:
                    node = new QuadTreeNode<T>(this.r.x + width, this.r.y, width, height, tpl, this.capacity, this.level + 1);
                    break;
                case SW:
                    node = new QuadTreeNode<T>(this.r.x, this.r.y, width, height, tpl, this.capacity, this.level + 1);
                    break;
                default:
                    throw new IllegalStateException("Unknown switch case: " + q);
            }
            this.children.set(q.ordinal(), node);
            return this;
        } else {
            return node.addChild(tpl);
        }
    }

    public Rectangle2D getArea() {
        return this.r;
    }

    private Quad getQuadrant(Point2D p) {
        Quad q = Quad.NW;
        if (p.getX() < this.r.x + (this.r.width / 2.0d)) {//west quadrants
            if (p.getY() < this.r.y + (this.r.height / 2.0d)) {//south quadrant
                q = Quad.SW;
            } else {
                q = Quad.NW;
            }
        } else {//east quadrants
            if (p.getY() < this.r.y + (this.r.height / 2.0d)) {//south quadrant
                q = Quad.SE;
            } else {
                q = Quad.NE;
            }
        }
        //System.out.println("Quadrant: "+q.name()+" Point: "+p);
        return q;
    }

    public List<Tuple2D<Point2D, T>> getImmediateChildren() {
        return this.t;
    }

    public List<QuadTreeNode<T>> getChildren() {
        return this.children;
    }
}
