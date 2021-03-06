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
package maltcms.datastructures.quadTree;

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>QuadTree class.</p>
 *
 * @author Nils Hoffmann
 * @param <T> the type of elements stored in the quad tree.
 * 
 */
@Slf4j
public class QuadTree<T> {

    private QuadTreeNode<T> root;
    private final double x, y, width, height;
    private final int capacity;
    private HashMap<T, QuadTreeNode<T>> hs = new HashMap<>();

    /**
     * <p>Constructor for QuadTree.</p>
     *
     * @param dataBounds a {@link java.awt.geom.Rectangle2D} object.
     */
    public QuadTree(Rectangle2D dataBounds) {
        this(dataBounds.getX(), dataBounds.getY(), dataBounds.getWidth(), dataBounds.getHeight(), 20);
    }

    /**
     * <p>Constructor for QuadTree.</p>
     *
     * @param x a double.
     * @param y a double.
     * @param width a double.
     * @param height a double.
     * @param capacity a int.
     */
    public QuadTree(double x, double y, double width, double height, int capacity) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.capacity = capacity;
    }

    /**
     * <p>getDataBounds.</p>
     *
     * @return a {@link java.awt.geom.Rectangle2D} object.
     */
    public Rectangle2D getDataBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    /**
     * <p>Getter for the field <code>root</code>.</p>
     *
     * @return a {@link maltcms.datastructures.quadTree.QuadTreeNode} object.
     * @since 1.3.2
     */
    public QuadTreeNode<T> getRoot() {
        return root;
    }

    /**
     * <p>put.</p>
     *
     * @param p a {@link java.awt.geom.Point2D} object.
     * @param t a T object.
     * @return a T object.
     */
    public T put(Point2D p, T t) {
        //long s = System.nanoTime();
        if (root == null) {
            QuadTreeNode<T> qtn = new QuadTreeNode<>(this.x, this.y, this.width, this.height, new Tuple2D<>(p, t), capacity, 0);
            root = qtn;
        }
        QuadTreeNode<T> qtn = this.root.addChild(new Tuple2D<>(p, t));
        hs.put(t, qtn);
        //log.info("Time for put: "+(System.nanoTime()-s));
        return t;
    }

    /**
     * <p>remove.</p>
     *
     * @param p a {@link java.awt.geom.Point2D} object.
     * @return a T object.
     */
    public T remove(Point2D p) {
        //long s = System.nanoTime();
        T t = this.root.remove(p);
        hs.remove(t);
        //log.info("Time for remove: "+(System.nanoTime()-s));
        return t;
    }

    /**
     * <p>get.</p>
     *
     * @param p a {@link java.awt.geom.Point2D} object.
     * @return a T object.
     * @throws maltcms.datastructures.quadTree.ElementNotFoundException if any.
     */
    public T get(Point2D p) throws ElementNotFoundException {
        //long s = System.nanoTime();
        if (this.root == null) {
            throw new ElementNotFoundException();
        }
        T t = this.root.getChild(p);
        //log.info("Time for get: "+(System.nanoTime()-s));
        return t;
    }

    /**
     * <p>getClosestInRadius.</p>
     *
     * @param p a {@link java.awt.geom.Point2D} object.
     * @param radius a double.
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     * @throws maltcms.datastructures.quadTree.ElementNotFoundException if any.
     */
    public Tuple2D<Point2D, T> getClosestInRadius(Point2D p, double radius) throws ElementNotFoundException {
        //long s = System.nanoTime();
        if (this.root == null) {
            throw new ElementNotFoundException();
        }
        Tuple2D<Point2D, T> t = this.root.getClosestChild(p, radius);
        //log.info("Time for getClosestInRadius: "+(System.nanoTime()-s));
        return t;
    }

    /**
     * <p>getNeighborsInRadius.</p>
     *
     * @param p a {@link java.awt.geom.Point2D} object.
     * @param radius a double.
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Point2D, T>> getNeighborsInRadius(Point2D p, double radius) {
        if (this.root == null) {
            return Collections.emptyList();
        }
        return this.root.getChildrenInRadius(new LinkedList<Tuple2D<Point2D, T>>(), p, radius);
    }

    /**
     * <p>getClosestPerpendicularToLine.</p>
     *
     * @param l a {@link java.awt.geom.Line2D} object.
     * @param maxPerpendicularDistance a double.
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Point2D, T>> getClosestPerpendicularToLine(Line2D l, double maxPerpendicularDistance) {
        if (this.root == null) {
            throw new ElementNotFoundException();
        }
        return root.getClosestChildrenPerpendicularToLine(new LinkedList<Tuple2D<Point2D, T>>(), l, maxPerpendicularDistance);
    }

    /**
     * <p>getHorizontalNeighborsInRadius.</p>
     *
     * @param p a {@link java.awt.geom.Point2D} object.
     * @param radius a double.
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Point2D, T>> getHorizontalNeighborsInRadius(Point2D p, double radius) {
        List<Tuple2D<Point2D, T>> neighs = getNeighborsInRadius(p, radius);
        List<Tuple2D<Point2D, T>> hneighs = new LinkedList<>();
        for (Tuple2D<Point2D, T> t : neighs) {
            Point2D p2 = t.getFirst();
            if (Math.abs(p2.getX() - p.getX()) <= radius) {
                hneighs.add(t);
            }
        }
        return hneighs;
    }

    /**
     * <p>getVerticalNeighborsInRadius.</p>
     *
     * @param p a {@link java.awt.geom.Point2D} object.
     * @param radius a double.
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Point2D, T>> getVerticalNeighborsInRadius(Point2D p, double radius) {
        List<Tuple2D<Point2D, T>> neighs = getNeighborsInRadius(p, radius);
        List<Tuple2D<Point2D, T>> vneighs = new LinkedList<>();
        for (Tuple2D<Point2D, T> t : neighs) {
            Point2D p2 = t.getFirst();
            if (Math.abs(p2.getY() - p.getY()) <= radius) {
                vneighs.add(t);
            }
        }
        return vneighs;
    }

    /**
     * <p>getChildrenInRange.</p>
     *
     * @param r a {@link java.awt.geom.Rectangle2D} object.
     * @return a {@link java.util.List} object.
     * @throws maltcms.datastructures.quadTree.ElementNotFoundException if any.
     */
    public List<Tuple2D<Point2D, T>> getChildrenInRange(Rectangle2D r) throws ElementNotFoundException {
        if (this.root == null) {
            throw new ElementNotFoundException();
        }
        return this.root.getChildrenInRange(r);
    }

    /**
     * <p>clear.</p>
     */
    public void clear() {
        if (this.root != null) {
            this.root = null;
            this.hs.clear();
        }
    }

    /**
     * <p>size.</p>
     *
     * @return a int.
     */
    public int size() {
        return this.hs.size();
    }

    /**
     * <p>isEmpty.</p>
     *
     * @return a boolean.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * <p>contains.</p>
     *
     * @param o a {@link java.lang.Object} object.
     * @return a boolean.
     */
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object o) {
        return this.hs.containsKey(o);
    }

    /**
     * <p>iterator.</p>
     *
     * @return a {@link java.util.Iterator} object.
     */
    public Iterator<Tuple2D<Point2D, T>> iterator() {
        QuadTreeNodeDepthFirstVisitor<T> qtn = new QuadTreeNodeDepthFirstVisitor<>(root);
        LinkedList<Tuple2D<Point2D, T>> l = new LinkedList<>();
        return qtn.visit(l).iterator();
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        double maxx = Math.pow(2.0d, 64.0d);
        QuadTree<String> qt = new QuadTree<>(0, 0, maxx, maxx, 5);
        Random x = new Random(System.nanoTime());
        Random y = new Random(System.nanoTime());
        int npoints = (int) (Integer.MAX_VALUE * (Math.random()) / 10000);
        log.info("Creating quad tree for " + npoints + " points");
        long start = 0;
        double tdiff = 0;
        List<Point2D> l = new ArrayList<>();
        for (int i = 0; i < npoints; i++) {
            Point2D p = new Point2D.Double((x.nextDouble() * maxx), (y.nextDouble() * maxx));
//            log.info("Adding item at point: "+p);
            qt.put(p, "" + i);
            l.add(p);

        }
        log.info("Done");
        log.info("Timing get operation!");
        List<Point2D> testList = new ArrayList<>();
        for (int i = 0; i < npoints; i++) {
//            Point2D p = new Point2D.Double((x.nextDouble()*maxx),(y.nextDouble()*maxx));
//            log.info("Adding item at point: "+p);
//            qt.put(p, ""+i);
//            l.add(p);
            int idx = (int) (npoints * Math.random());
            Point2D p = l.get(idx);
            testList.add(p);
        }

        tdiff = 0;
        Comparator<Point2D> pc = new Comparator<Point2D>() {
            @Override
            public int compare(Point2D p1, Point2D p2) {
                if (p1.getX() < p2.getX()) {
                    return -1;
                }
                if (p1.getX() > p2.getX()) {
                    return 1;
                }
                if (p1.getY() < p2.getY()) {
                    return -1;
                }
                if (p1.getY() > p2.getY()) {
                    return 1;
                }
                return 0;
            }
        };

        for (Point2D p : testList) {
            start = System.nanoTime();
            int idx = Collections.binarySearch(l, p, pc);
            if (idx < 0) {
                log.info("Warning: Point not found in List!");
            }
            tdiff += (System.nanoTime() - start);
        }

        log.info("Average time for list point retrieval: " + (tdiff / (double) npoints));

        tdiff = 0;
        for (Point2D p : testList) {
            start = System.nanoTime();
            String s = qt.get(p);
            tdiff += (System.nanoTime() - start);
        }

        log.info("Average time for tree point retrieval: " + (tdiff / (double) npoints));
    }
}
