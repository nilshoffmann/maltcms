/*
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.tools;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nils Hoffmann
 */
public class MathToolsTest {

    public MathToolsTest() {
    }

    /**
     * Test of average method, of class MathTools.
     */
    @Test
    public void testAverage() {
    }

    /**
     * Test of averageOfSquares method, of class MathTools.
     */
    @Test
    public void testAverageOfSquares() {
    }

    /**
     * Test of binCoeff method, of class MathTools.
     */
    @Test
    public void testBinCoeff() {
    }

    /**
     * Test of binomial method, of class MathTools.
     */
    @Test
    public void testBinomial() {
    }

    /**
     * Test of diff method, of class MathTools.
     */
    @Test
    public void testDiff() {
    }

    /**
     * Test of faculty method, of class MathTools.
     */
    @Test
    public void testFaculty() {
    }

    /**
     * Test of getLinearInterpolatedY method, of class MathTools.
     */
    @Test
    public void testGetLinearInterpolatedY() {
    }

    /**
     * Test of max method, of class MathTools.
     */
    @Test
    public void testMax_doubleArr() {
    }

    /**
     * Test of max method, of class MathTools.
     */
    @Test
    public void testMax_3args_1() {
    }

    /**
     * Test of max method, of class MathTools.
     */
    @Test
    public void testMax_intArr() {
    }

    /**
     * Test of max method, of class MathTools.
     */
    @Test
    public void testMax_3args_2() {
    }

    /**
     * Test of median method, of class MathTools.
     */
    @Test
    public void testMedian_Collection() {
    }

    /**
     * Test of median method, of class MathTools.
     */
    @Test
    public void testMedian_doubleArr() {
    }

    /**
     * Test of median method, of class MathTools.
     */
    @Test
    public void testMedian_3args() {
    }

    /**
     * Test of median method, of class MathTools.
     */
    @Test
    public void testMedian_doubleArrArr() {
    }

    /**
     * Test of median method, of class MathTools.
     */
    @Test
    public void testMedian_intArr() {
    }

    /**
     * Test of medianOnSorted method, of class MathTools.
     */
    @Test
    public void testMedianOnSorted() {
    }

    /**
     * Test of min method, of class MathTools.
     */
    @Test
    public void testMin_doubleArr() {
    }

    /**
     * Test of min method, of class MathTools.
     */
    @Test
    public void testMin_3args_1() {
    }

    /**
     * Test of min method, of class MathTools.
     */
    @Test
    public void testMin_intArr() {
    }

    /**
     * Test of min method, of class MathTools.
     */
    @Test
    public void testMin_3args_2() {
    }

    /**
     * Test of relativeBinCoeff method, of class MathTools.
     */
    @Test
    public void testRelativeBinCoeff() {
    }

    /**
     * Test of sum method, of class MathTools.
     */
    @Test
    public void testSum() {
    }

    /**
     * Test of weightedAverage method, of class MathTools.
     */
    @Test
    public void testWeightedAverage_int_doubleArr() {
    }

    /**
     * Test of weightedAverage method, of class MathTools.
     */
    @Test
    public void testWeightedAverage_3args() {
    }

    /**
     * Test of dilate method, of class MathTools.
     */
    @Test
    public void testDilate_int_doubleArr() {
    }

    /**
     * Test of erode method, of class MathTools.
     */
    @Test
    public void testErode_int_doubleArr() {
    }

    /**
     * Test of dilate method, of class MathTools.
     */
    @Test
    public void testDilate_3args() {
    }

    /**
     * Test of erode method, of class MathTools.
     */
    @Test
    public void testErode_3args() {
    }

    /**
     * Test of opening method, of class MathTools.
     */
    @Test
    public void testOpening() {
    }

    /**
     * Test of closing method, of class MathTools.
     */
    @Test
    public void testClosing() {
    }

    /**
     * Test of topHat method, of class MathTools.
     */
    @Test
    public void testTopHat() {
    }

    /**
     * Test of bottomHat method, of class MathTools.
     */
    @Test
    public void testBottomHat() {
    }

    /**
     * Tesf of seq method, of class MathTools.
     */
    @Test
    public void testSeqInt() {
        int[] referenceSequence = new int[]{3, 5, 7, 9};
        int[] testSequence = MathTools.seq(3, 10, 2);
        Assert.assertArrayEquals(referenceSequence, testSequence);
        referenceSequence = new int[]{-3, -5, -7, -9};
        testSequence = MathTools.seq(-3, -9, -2);
        Assert.assertArrayEquals(referenceSequence, testSequence);
    }

    /**
     * Tesf of seq method, of class MathTools.
     */
    @Test
    public void testSeqDouble() {
        double[] referenceSequence = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
        double[] testSequence = MathTools.seq(0.0, 0.5, 0.1);
        Assert.assertArrayEquals(referenceSequence, testSequence, 1.0e-10d);
        
        referenceSequence = new double[]{-2.1, -2.05, -2.0,-1.95, -1.9};
        testSequence = MathTools.seq(-2.1, -1.9, 0.05);
        Assert.assertArrayEquals(referenceSequence, testSequence, 1.0e-10d);
    }
}
