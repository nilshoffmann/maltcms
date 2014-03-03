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
package maltcms.datastructures.array.tests;

import cross.datastructures.tuple.Tuple2D;
import junit.framework.Assert;
import junit.framework.TestCase;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.Sparse;

public class SparseTest extends TestCase {

    private Sparse s1 = null, s2 = null;
    private ArrayInt.D1 index = null, index1 = null;
    private ArrayDouble.D1 values1 = null, values = null;

    public SparseTest(final String arg0) {
        super(arg0);
        // System.out.println("Creating first array!");
        this.index = ArrayTools.indexArray(20, 10);
        // System.out.println(this.index);
        this.values = new ArrayDouble.D1(20);
        for (int i = 0; i < 20; i++) {
            final double val = ArrayTools.nextUniform();
            // System.out.println(i + " = " + val);
            this.values.set(i, val);
        }
        this.s1 = Sparse.create(this.index, this.values, 1.0d);
        this.index1 = ArrayTools.indexArray(10, 0);
        // System.out.println(this.index1);
        this.values1 = new ArrayDouble.D1(10);
        // System.out.println("Creating second array!");
        for (int i = 0; i < 10; i++) {
            final double val = ArrayTools.nextUniform();
            // System.out.println(i + " = " + val);
            this.values1.set(i, val);
        }
        this.s2 = Sparse.create(this.index1, this.values1, 1.0d);
    }

    public double diff(final Array a1, final Array a2) {
        if (MAMath.conformable(a1.getShape(), a2.getShape())) {
            final IndexIterator ii1 = a1.getIndexIterator();
            final IndexIterator ii2 = a2.getIndexIterator();
            double diff = 0.0d;
            double d = 0.0d;
            while (ii1.hasNext() && ii2.hasNext()) {
                final double d1 = ii1.getDoubleNext();
                final double d2 = ii2.getDoubleNext();
                // System.out.println(d1 + " " + d2);
                d = (d1 - d2);
                diff += d;
                // System.out.println(d);
            }
            return diff;
        } else {
            throw new IllegalArgumentException("Array shapes mismatch!");
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDot() {
        this.index = ArrayTools.indexArray(30, 10);
        this.values = new ArrayDouble.D1(30);
        for (int i = 0; i < 30; i++) {
            final double val = ArrayTools.nextUniform();
            // System.out.println((i) + " = " + val);
            this.values.set(i, val);
        }
        this.index1 = ArrayTools.indexArray(25, 5);
        this.values1 = new ArrayDouble.D1(25);
        // System.out.println("Creating second array!");
        for (int i = 0; i < 25; i++) {
            final double val = ArrayTools.nextUniform();
            // System.out.println(i + " = " + val);
            this.values1.set(i, val);
        }

        final int nbins = MaltcmsTools.getNumberOfIntegerMassBins(Math.min(5,
            10), Math.max(5 + 25 - 1, 10 + 30 - 1), 1.0d);
        this.s1 = Sparse.create(this.index, this.values, Math.min(5, 10), Math
            .max(5 + 25 - 1, 10 + 30 - 1), nbins, 1.0d);
        this.s2 = Sparse.create(this.index1, this.values1, Math.min(5, 10),
            Math.max(5 + 25 - 1, 10 + 30 - 1), nbins, 1.0d);
        // System.out.println("Shape 1: " + this.s1.getShape()[0] + " Shape 2: "
        // + this.s2.getShape()[0]);
        // Assert.assertEquals(3.90222d, SparseTools.dot(this.s1,
        // this.s2),0.01d);
    }

    public void testGetMaxIndex() {
        Assert.assertEquals(this.s1.getMaxIndex(),
            10 + this.s1.getShape()[0] - 1);
        Assert.assertEquals(this.s2.getMaxIndex(),
            0 + this.s2.getShape()[0] - 1);
    }

    // public void testMeanMerge() {
    // fail("Not yet implemented");
    // }
    public void testGetMinIndex() {
        Assert.assertEquals(this.s1.getMinIndex(), 10);
        Assert.assertEquals(this.s2.getMinIndex(), 0);
    }

    public void testGetShape() {
        Assert.assertEquals(this.s1.getShape()[0], 20);
        Assert.assertEquals(this.s2.getShape()[0], 10);
    }

    public void testToArrays() {
        final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> t1 = this.s1.toArrays();
        Assert.assertEquals(0.0d, diff(t1.getFirst(), this.index));
        Assert.assertEquals(0.0d, diff(t1.getSecond(), this.values));
        final Tuple2D<ArrayDouble.D1, ArrayDouble.D1> t2 = this.s2.toArrays();
        Assert.assertEquals(0.0d, diff(t2.getFirst(), this.index1));
        Assert.assertEquals(0.0d, diff(t2.getSecond(), this.values1));
    }
}
