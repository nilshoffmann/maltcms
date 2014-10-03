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
package maltcms.datastructures.array;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cross.datastructures.tuple.Tuple2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D2;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayInt.D1;

/**
 * <p>SparseArray class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public final class SparseArray implements IArrayD2Double {

    private final SparseDoubleMatrix2D sdm;

    /**
     * <p>Constructor for SparseArray.</p>
     *
     * @param rows a int.
     * @param columns a int.
     */
    public SparseArray(int rows, int columns) {
        sdm = new SparseDoubleMatrix2D(rows, columns);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#columns()
     */
    /** {@inheritDoc} */
    @Override
    public int columns() {
        return this.sdm.columns();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#flatten()
     */
    /** {@inheritDoc} */
    @Override
    public Tuple2D<D1, ucar.ma2.ArrayDouble.D1> flatten() {
        ArrayInt.D1 nelems = new ArrayInt.D1(rows());
        ArrayDouble.D1 data = new ArrayDouble.D1(getNumberOfStoredElements());
        int dtidx = 0;
        for (int i = 0; i < rows(); i++) {
            nelems.set(i, dtidx);
            for (int j = 0; j < columns(); j++) {
                double d = this.sdm.get(i, j);
                if (d != 0) {
                    data.set(dtidx++, d);
                }
            }
        }
        return new Tuple2D<>(nelems, data);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#get(int, int)
     */
    /** {@inheritDoc} */
    @Override
    public double get(int row, int col) {
        return this.sdm.get(row, col);
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getArray()
     */
    /** {@inheritDoc} */
    @Override
    public D2 getArray() {
        ArrayDouble.D2 arr = new ArrayDouble.D2(rows(), columns());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                arr.set(i, j, this.sdm.get(i, j));
            }
        }
        return arr;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getColumnBounds(int)
     */
    /** {@inheritDoc} */
    @Override
    public int[] getColumnBounds(final int row) {
        return new int[]{0, columns()};
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.alignment.IArrayD2Double#getDefaultValue()
     */
    /** {@inheritDoc} */
    @Override
    public double getDefaultValue() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.alignment.IArrayD2Double#getNumberOfStoredElements
     * ()
     */
    /** {@inheritDoc} */
    @Override
    public int getNumberOfStoredElements() {
        return sdm.cardinality();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getShape()
     */
    /** {@inheritDoc} */
    @Override
    public Area getShape() {
        return new Area(new Rectangle(0, 0, columns(), rows()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean inRange(final int i, final int j) {
        if ((i > 0) && (j > 0) && (i < rows()) && (j < columns())) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#rows()
     */
    /** {@inheritDoc} */
    @Override
    public int rows() {
        return this.sdm.rows();
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#set(int, int, double)
     */
    /** {@inheritDoc} */
    @Override
    public void set(int row, int col, double d)
            throws ArrayIndexOutOfBoundsException {
        this.sdm.set(row, col, d);
    }
}
