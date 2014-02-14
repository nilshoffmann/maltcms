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

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the quad tree data structure.
 *
 * @author Nils Hoffmann
 */
public class QuadTreeTest {

    /**
     * Test of put method, of class QuadTree.
     */
    @Test
    public void testPut() {
        QuadTree<Integer> qt = new QuadTree<Integer>(new Rectangle2D.Double(0, 0, 100, 100));
        Point2D pt = new Point2D.Double(10.023, 83.87);
        qt.put(pt, 5);
        Assert.assertTrue(qt.contains(5));
    }

    /**
     * Test of remove method, of class QuadTree.
     */
    @Test
    public void testRemove() {
        QuadTree<Integer> qt = new QuadTree<Integer>(new Rectangle2D.Double(0, 0, 100, 100));
        Point2D pt = new Point2D.Double(10.023, 83.87);
        qt.put(pt, 5);
        qt.remove(pt);
        Assert.assertFalse(qt.contains(pt));
    }

    /**
     * Test of get method, of class QuadTree.
     */
    @Test
    public void testGet() {
        QuadTree<Integer> qt = new QuadTree<Integer>(new Rectangle2D.Double(0, 0, 100, 100));
        Point2D pt = new Point2D.Double(10.023, 83.87);
        qt.put(pt, 5);
        Integer retrieved = qt.get(pt);
        Assert.assertTrue(5 == retrieved);
    }

    /**
     * Test of getClosestInRadius method, of class QuadTree.
     */
    @Test
    public void testGetClosestInRadius() {
        QuadTree<Integer> qt = new QuadTree<Integer>(new Rectangle2D.Double(0, 0, 100, 100));
        Point2D pt = new Point2D.Double(10.023, 83.87);
        qt.put(pt, 5);
        Point2D pt2 = new Point2D.Double(50.0213, 23.213);
        qt.put(pt2, 928);
        Tuple2D<Point2D, Integer> t = qt.getClosestInRadius(pt, 10);
        Assert.assertTrue(5 == t.getSecond());
    }

    /**
     * Test of getNeighborsInRadius method, of class QuadTree.
     */
    @Test
    public void testGetNeighborsInRadius() {
        QuadTree<Integer> qt = new QuadTree<Integer>(new Rectangle2D.Double(0, 0, 100, 100));
        Point2D pt = new Point2D.Double(10.023, 83.87);
        qt.put(pt, 5);
        Point2D pt2 = new Point2D.Double(50.0213, 23.213);
        qt.put(pt2, 928);
        Point2D pt3 = new Point2D.Double(12.023, 82.87);
        qt.put(pt3, 10);
        //includes query point
        List<Tuple2D<Point2D, Integer>> t = qt.getNeighborsInRadius(pt, 5);
        for (Tuple2D<Point2D, Integer> tpl : t) {
            switch (tpl.getSecond()) {
                case 5:
                    Assert.assertEquals(pt.getX(), tpl.getFirst().getX(), 0);
                    Assert.assertEquals(pt.getY(), tpl.getFirst().getY(), 0);
                    break;
                case 10:
                    Assert.assertEquals(pt3.getX(), tpl.getFirst().getX(), 0);
                    Assert.assertEquals(pt3.getY(), tpl.getFirst().getY(), 0);
                    break;
                default:
                    Assert.fail();
            }
        }
    }

    /**
     * Test of getClosestPerpendicularToLine method, of class QuadTree.
     */
    @Test
    public void testGetClosestPerpendicularToLine() {
    }

    /**
     * Test of getHorizontalNeighborsInRadius method, of class QuadTree.
     */
    @Test
    public void testGetHorizontalNeighborsInRadius() {
    }

    /**
     * Test of getVerticalNeighborsInRadius method, of class QuadTree.
     */
    @Test
    public void testGetVerticalNeighborsInRadius() {
    }

    /**
     * Test of getChildrenInRange method, of class QuadTree.
     */
    @Test
    public void testGetChildrenInRange() {
    }

    /**
     * Test of isEmpty, size and clear methods, of class QuadTree.
     */
    @Test
    public void testIsEmptySizeAndClear() {
        QuadTree<Integer> qt = new QuadTree<Integer>(new Rectangle2D.Double(0, 0, 100, 100));
        Point2D pt = new Point2D.Double(10.023, 83.87);
        qt.put(pt, 5);
        Point2D pt2 = new Point2D.Double(50.0213, 23.213);
        qt.put(pt2, 928);
        Point2D pt3 = new Point2D.Double(12.023, 82.87);
        qt.put(pt3, 10);
        Assert.assertFalse(qt.isEmpty());
        Assert.assertEquals(3, qt.size());
        qt.clear();
        Assert.assertEquals(0, qt.size());
    }

    /**
     * Test of iterator method, of class QuadTree.
     */
    @Test
    public void testIterator() {
        QuadTree<Integer> qt = new QuadTree<Integer>(new Rectangle2D.Double(0, 0, 100, 100));
        Point2D pt = new Point2D.Double(10.023, 83.87);
        qt.put(pt, 5);
        Point2D pt2 = new Point2D.Double(50.0213, 23.213);
        qt.put(pt2, 928);
        Point2D pt3 = new Point2D.Double(12.023, 82.87);
        qt.put(pt3, 10);
        int elements = 4;
        int cnt = 0;
        Iterator<Tuple2D<Point2D, Integer>> iter = qt.iterator();
        while (iter.hasNext()) {
            cnt++;
            Tuple2D<Point2D, Integer> tple = iter.next();
            Assert.assertNotNull(tple);
            Assert.assertNotNull(tple.getFirst());
            Assert.assertNotNull(tple.getSecond());
        }
        Assert.assertEquals(elements, cnt);
    }
}
