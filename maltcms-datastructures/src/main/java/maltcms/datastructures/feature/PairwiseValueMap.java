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
package maltcms.datastructures.feature;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cross.tools.MathTools;
import java.io.Serializable;
import java.util.UUID;

/**
 * <p>PairwiseValueMap class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class PairwiseValueMap implements Serializable {

    private final DoubleMatrix2D matrix;
    private final boolean symmetric;
    private final UUID id;

    public enum StorageType {

        DENSE, ROW_COMPRESSED, SPARSE
    };

    /**
     * <p>Constructor for PairwiseValueMap.</p>
     *
     * @param rows a int.
     * @param columns a int.
     * @param symmetric a boolean.
     * @param type a {@link maltcms.datastructures.feature.PairwiseValueMap.StorageType} object.
     * @param missingValue a double.
     */
    public PairwiseValueMap(int rows, int columns, boolean symmetric, StorageType type, double missingValue) {
        this(UUID.randomUUID(), rows, columns, symmetric, type, missingValue);
    }

    /**
     * <p>Constructor for PairwiseValueMap.</p>
     *
     * @param id a {@link java.util.UUID} object.
     * @param rows a int.
     * @param columns a int.
     * @param symmetric a boolean.
     * @param type a {@link maltcms.datastructures.feature.PairwiseValueMap.StorageType} object.
     * @param missingValue a double.
     */
    public PairwiseValueMap(UUID id, int rows, int columns, boolean symmetric, StorageType type, double missingValue) {
        switch (type) {
            case DENSE:
                matrix = DoubleFactory2D.dense.make(rows, columns, missingValue);
                break;
            case ROW_COMPRESSED:
                matrix = DoubleFactory2D.rowCompressed.make(rows, columns, missingValue);
                break;
            case SPARSE:
                matrix = DoubleFactory2D.sparse.make(rows, columns, missingValue);
                break;
            default:
                throw new IllegalArgumentException("Unmatched case: " + type);

        }
        this.id = id;
        this.symmetric = symmetric;
    }

    /**
     * <p>getValue.</p>
     *
     * @param rowIndex a int.
     * @param columnIndex a int.
     * @return a double.
     */
    public double getValue(int rowIndex, int columnIndex) {
        return matrix.getQuick(rowIndex, columnIndex);
    }

    /**
     * <p>setValue.</p>
     *
     * @param rowIndex a int.
     * @param columnIndex a int.
     * @param value a double.
     */
    public void setValue(int rowIndex, int columnIndex, double value) {
        matrix.setQuick(rowIndex, columnIndex, value);
        if (symmetric) {
            matrix.setQuick(columnIndex, rowIndex, value);
        }
    }

    /**
     * <p>indexOfMaxInColumn.</p>
     *
     * @param columnIndex a int.
     * @return a int.
     */
    public int indexOfMaxInColumn(int columnIndex) {
        return indexOfMax(matrix.viewColumn(columnIndex));
    }

    /**
     * <p>indexOfMinInColumn.</p>
     *
     * @param columnIndex a int.
     * @return a int.
     */
    public int indexOfMinInColumn(int columnIndex) {
        return indexOfMin(matrix.viewColumn(columnIndex));
    }

    /**
     * <p>indexOfMaxInRow.</p>
     *
     * @param rowIndex a int.
     * @return a int.
     */
    public int indexOfMaxInRow(int rowIndex) {
        return indexOfMax(matrix.viewRow(rowIndex));
    }

    /**
     * <p>indexOfMinInRow.</p>
     *
     * @param rowIndex a int.
     * @return a int.
     */
    public int indexOfMinInRow(int rowIndex) {
        return indexOfMin(matrix.viewRow(rowIndex));
    }

    /**
     * <p>indexOfMax.</p>
     *
     * @param vector a {@link cern.colt.matrix.DoubleMatrix1D} object.
     * @return a int.
     */
    public int indexOfMax(DoubleMatrix1D vector) {
        PermutationSwapper swapper = new PermutationSwapper(vector);
        GenericSorting.mergeSort(0, vector.size(), new ColumnComparator(vector), swapper);
        int[] permutation = swapper.getPermutation();
        if (permutation.length != vector.size()) {
            throw new IllegalStateException();
        }
        //return index of largest element
        return permutation[permutation.length - 1];
    }

    /**
     * <p>indexOfMin.</p>
     *
     * @param vector a {@link cern.colt.matrix.DoubleMatrix1D} object.
     * @return a int.
     */
    public int indexOfMin(DoubleMatrix1D vector) {
        PermutationSwapper swapper = new PermutationSwapper(vector);
        GenericSorting.mergeSort(0, vector.size(), new ColumnComparator(vector), swapper);
        int[] permutation = swapper.getPermutation();
        if (permutation.length != vector.size()) {
            throw new IllegalStateException();
        }
        //return index of smallest element
        return permutation[0];
    }

    public class PermutationSwapper implements Swapper {

        private final int[] permutation;

        public PermutationSwapper(DoubleMatrix1D column) {
            permutation = MathTools.seq(0, column.size() - 1, 1);
        }

        @Override
        public void swap(int a, int b) {
            int tmp = permutation[a];
            permutation[a] = permutation[b];
            permutation[b] = tmp;
        }

        public int[] getPermutation() {
            return permutation;
        }
    }

    public class ColumnComparator implements IntComparator {

        private final DoubleMatrix1D column;
        private final boolean ascending;

        public ColumnComparator(DoubleMatrix1D column) {
            this(column, true);
        }

        public ColumnComparator(DoubleMatrix1D column, boolean ascending) {
            this.column = column;
            this.ascending = ascending;
        }

        @Override
        public int compare(int a, int b) {
            if (ascending) {
                return column.get(a) == column.get(b) ? 0 : (column.get(a) < column.get(b) ? -1 : 1);
            }
            return column.get(a) == column.get(b) ? 0 : (column.get(a) < column.get(b) ? 1 : -1);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PairwiseValueMap other = (PairwiseValueMap) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    /**
     * <p>rows.</p>
     *
     * @return a int.
     */
    public int rows() {
        return matrix.rows();
    }

    /**
     * <p>columns.</p>
     *
     * @return a int.
     */
    public int columns() {
        return matrix.columns();
    }
}
