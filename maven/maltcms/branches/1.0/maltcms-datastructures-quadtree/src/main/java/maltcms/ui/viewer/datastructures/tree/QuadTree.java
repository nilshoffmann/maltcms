/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */
package maltcms.ui.viewer.datastructures.tree;

import java.awt.geom.Point2D;

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class QuadTree<T> {

    private QuadTreeNode<T> root;
    private final double x, y, width, height;
    private final int capacity;
    private HashMap<T, QuadTreeNode<T>> hs = new HashMap<T, QuadTreeNode<T>>();

    public QuadTree(Rectangle2D dataBounds) {
        this(dataBounds.getX(), dataBounds.getY(), dataBounds.getWidth(), dataBounds.getHeight(), 20);
    }

    public QuadTree(double x, double y, double width, double height, int capacity) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.capacity = capacity;
    }

    public T put(Point2D p, T t) {
        //long s = System.nanoTime();
        if (root == null) {
            QuadTreeNode<T> qtn = new QuadTreeNode<T>(this.x, this.y, this.width, this.height, new Tuple2D<Point2D, T>(p, t), capacity, 0);
            root = qtn;
        }
        QuadTreeNode<T> qtn = this.root.addChild(new Tuple2D<Point2D, T>(p, t));
        hs.put(t, qtn);
        //System.out.println("Time for put: "+(System.nanoTime()-s));
        return t;
    }

    public T remove(Point2D p) {
        //long s = System.nanoTime();
        T t = this.root.remove(p);
        hs.remove(t);
        //System.out.println("Time for remove: "+(System.nanoTime()-s));
        return t;
    }

    public T get(Point2D p) throws ElementNotFoundException {
        //long s = System.nanoTime();
        if (this.root == null) {
            throw new ElementNotFoundException();
        }
        T t = this.root.getChild(p);
        //System.out.println("Time for get: "+(System.nanoTime()-s));
        return t;
    }
    
    public List<Tuple2D<Point2D, T>> getNeighborsInRadius(Point2D p, double radius) {
        if (this.root == null) {
            return Collections.emptyList();
        }
        return this.root.getChildrenInRadius(new LinkedList<Tuple2D<Point2D, T>>(), p, radius);
    }
    
    public List<Tuple2D<Point2D, T>> getHorizontalNeighborsInRadius(Point2D p, double radius) {
        List<Tuple2D<Point2D,T>> neighs = getNeighborsInRadius(p, radius);
        List<Tuple2D<Point2D,T>> hneighs = new LinkedList<Tuple2D<Point2D,T>>();
        for(Tuple2D<Point2D,T> t:neighs) {
        	Point2D p2 = t.getFirst();
        	if(Math.abs(p2.getX()-p.getX())<=radius) {
        		hneighs.add(t);
        	}
        }
        return hneighs;
    }
    
    public List<Tuple2D<Point2D, T>> getVerticalNeighborsInRadius(Point2D p, double radius) {
    	List<Tuple2D<Point2D,T>> neighs = getNeighborsInRadius(p, radius);
        List<Tuple2D<Point2D,T>> vneighs = new LinkedList<Tuple2D<Point2D,T>>();
        for(Tuple2D<Point2D,T> t:neighs) {
        	Point2D p2 = t.getFirst();
        	if(Math.abs(p2.getY()-p.getY())<=radius) {
        		vneighs.add(t);
        	}
        }
        return vneighs;
    }

    public Tuple2D<Point2D, T> getClosestInRadius(Point2D p, double radius) throws ElementNotFoundException {
        //long s = System.nanoTime();
        if (this.root == null) {
            throw new ElementNotFoundException();
        }
        Tuple2D<Point2D, T> t = this.root.getClosestChild(p, radius);
        //System.out.println("Time for getClosestInRadius: "+(System.nanoTime()-s));
        return t;
    }

    public List<Tuple2D<Point2D, T>> getChildrenInRange(Rectangle2D r) throws ElementNotFoundException {
        if (this.root == null) {
            throw new ElementNotFoundException();
        }
        return this.root.getChildrenInRange(r);
    }

    public void clear() {
        if (this.root != null) {
            this.root = null;
            this.hs.clear();
        }
    }

    public int size() {
        return this.hs.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object o) {
        return this.hs.containsKey(o);
    }

    public Iterator<Tuple2D<Point2D,T>> iterator() {
        QuadTreeNodeDepthFirstVisitor<T> qtn = new QuadTreeNodeDepthFirstVisitor<T>(root);
        LinkedList<Tuple2D<Point2D,T>> l = new LinkedList<Tuple2D<Point2D,T>>();
        return qtn.visit(l).iterator();
    }

    public static void main(String[] args) {
        double maxx = Math.pow(2.0d, 64.0d);
        QuadTree<String> qt = new QuadTree<String>(0, 0, maxx, maxx, 5);
        Random x = new Random(System.nanoTime());
        Random y = new Random(System.nanoTime());
        int npoints = (int) (Integer.MAX_VALUE * (Math.random()) / 10000);
        System.out.println("Creating quad tree for " + npoints + " points");
        long start = 0;
        double tdiff = 0;
        List<Point2D> l = new ArrayList<Point2D>();
        for (int i = 0; i < npoints; i++) {
            Point2D p = new Point2D.Double((x.nextDouble() * maxx), (y.nextDouble() * maxx));
//            System.out.println("Adding item at point: "+p);
            qt.put(p, "" + i);
            l.add(p);

        }
        System.out.println("Done");
        System.out.println("Timing get operation!");
        List<Point2D> testList = new ArrayList<Point2D>();
        for (int i = 0; i < npoints; i++) {
//            Point2D p = new Point2D.Double((x.nextDouble()*maxx),(y.nextDouble()*maxx));
//            System.out.println("Adding item at point: "+p);
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

        for (int i = 0; i < testList.size(); i++) {
            Point2D p = testList.get(i);
            start = System.nanoTime();
            int idx = Collections.binarySearch(l, p, pc);
            if(idx<0) {
                System.out.println("Warning: Point not found in List!");
            }
            tdiff += (System.nanoTime() - start);
        }

        System.out.println("Average time for list point retrieval: " + (tdiff / (double) npoints));

        tdiff = 0;
        for (int i = 0; i <  testList.size(); i++) {
//            Point2D p = new Point2D.Double((x.nextDouble()*maxx),(y.nextDouble()*maxx));
//            System.out.println("Adding item at point: "+p);
//            qt.put(p, ""+i);
//            l.add(p);
            Point2D p = testList.get(i);
            start = System.nanoTime();
            String s = qt.get(p);
            tdiff += (System.nanoTime() - start);
        }

        System.out.println("Average time for tree point retrieval: " + (tdiff / (double) npoints));
    }
}
