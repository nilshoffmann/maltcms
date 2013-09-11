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
package maltcms.datastructures.feature;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cross.tools.MathTools;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class PairwiseValueMapTest {

    /**
     * Test of dense matrix.
     */
    @Test
    public void testDenseMatrix() {
        DoubleMatrix2D matrix = DoubleFactory2D.dense.make(3, 4, Double.NEGATIVE_INFINITY);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,matrix.get(0, 1),1.0e-10);
        matrix.setQuick(0, 2, 0.89);
        matrix.setQuick(1, 2, 0.414);
        final DoubleMatrix1D column = matrix.viewColumn(2);
        Assert.assertEquals(column.size(),3);
        final int[] permutation = MathTools.seq(0, column.size()-1, 1);
        Assert.assertEquals(column.size(),permutation.length);
        Swapper swapper = new Swapper() {
            @Override
            public void swap(int a, int b) {
                int tmp = permutation[a];
                permutation[a] = permutation[b];
                permutation[b] = tmp;
            }
        };
        IntComparator comp = new IntComparator() {
            @Override
            public int compare(int a, int b) {
                return column.get(a) == column.get(b) ? 0 : (column.get(a) < column.get(b) ? -1 : 1);
            }
        };
        GenericSorting.mergeSort(0, column.size(), comp, swapper);
        //return index of largest element
        Assert.assertArrayEquals(new int[]{2,1,0}, permutation);
        Assert.assertEquals(0.89, column.getQuick(permutation[permutation.length-1]),1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, column.getQuick(permutation[0]),1.0e-10);
    }
    
    /**
     * Test of sparse matrix.
     */
    @Test
    public void testSparseMatrix() {
        DoubleMatrix2D matrix = DoubleFactory2D.sparse.make(3, 4, Double.NEGATIVE_INFINITY);
        Assert.assertEquals(Double.NEGATIVE_INFINITY,matrix.get(0, 1),1.0e-10);
        matrix.setQuick(0, 2, 0.89);
        matrix.setQuick(1, 2, 0.414);
        final DoubleMatrix1D column = matrix.viewColumn(2);
        Assert.assertEquals(column.size(),3);
        final int[] permutation = MathTools.seq(0, column.size()-1, 1);
        Assert.assertEquals(column.size(),permutation.length);
        Swapper swapper = new Swapper() {
            @Override
            public void swap(int a, int b) {
                int tmp = permutation[a];
                permutation[a] = permutation[b];
                permutation[b] = tmp;
            }
        };
        IntComparator comp = new IntComparator() {
            @Override
            public int compare(int a, int b) {
                return column.get(a) == column.get(b) ? 0 : (column.get(a) < column.get(b) ? -1 : 1);
            }
        };
        GenericSorting.mergeSort(0, column.size(), comp, swapper);
        //return index of largest element
        Assert.assertArrayEquals(new int[]{2,1,0}, permutation);
        Assert.assertEquals(0.89, column.getQuick(permutation[permutation.length-1]),1.0e-10);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, column.getQuick(permutation[0]),1.0e-10);
    }

    /**
     * Test of indexOfMax and indexOfMin method, of class PairwiseValueMap.
     */
    @Test
    public void testIndexOfMaxAndMinSparse() {
        PairwiseValueMap pvm = new PairwiseValueMap(3, 4, false, PairwiseValueMap.StorageType.SPARSE, Double.NEGATIVE_INFINITY);
        pvm.setValue(0, 2, 0.89);
        pvm.setValue(1, 2, 0.414);
        Assert.assertEquals(0, pvm.indexOfMaxInColumn(2));
        Assert.assertEquals(0.89, pvm.getValue(pvm.indexOfMaxInColumn(2),2),1.0e-10);
        Assert.assertEquals(2, pvm.indexOfMinInColumn(2));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, pvm.getValue(pvm.indexOfMinInColumn(2),2),1.0e-10);
    }
    
    /**
     * Test of indexOfMax and indexOfMin method, of class PairwiseValueMap.
     */
    @Test
    public void testIndexOfMaxAndMinRcs() {
        PairwiseValueMap pvm = new PairwiseValueMap(3, 4, false, PairwiseValueMap.StorageType.ROW_COMPRESSED, Double.NEGATIVE_INFINITY);
        pvm.setValue(0, 2, 0.89);
        pvm.setValue(1, 2, 0.414);
        Assert.assertEquals(0, pvm.indexOfMaxInColumn(2));
        Assert.assertEquals(0.89, pvm.getValue(pvm.indexOfMaxInColumn(2),2),1.0e-10);
        Assert.assertEquals(2, pvm.indexOfMinInColumn(2));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, pvm.getValue(pvm.indexOfMinInColumn(2),2),1.0e-10);
    }
    
    /**
     * Test of indexOfMax and indexOfMin method, of class PairwiseValueMap.
     */
    @Test
    public void testIndexOfMaxAndMinDense() {
        PairwiseValueMap pvm = new PairwiseValueMap(3, 4, false, PairwiseValueMap.StorageType.DENSE, Double.NEGATIVE_INFINITY);
        pvm.setValue(0, 2, 0.89);
        pvm.setValue(1, 2, 0.414);
        Assert.assertEquals(0, pvm.indexOfMaxInColumn(2));
        Assert.assertEquals(0.89, pvm.getValue(pvm.indexOfMaxInColumn(2),2),1.0e-10);
        Assert.assertEquals(2, pvm.indexOfMinInColumn(2));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, pvm.getValue(pvm.indexOfMinInColumn(2),2),1.0e-10);
    }
}
