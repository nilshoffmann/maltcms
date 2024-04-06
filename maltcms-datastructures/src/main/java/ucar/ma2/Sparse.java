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
package ucar.ma2;

import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cross.datastructures.tuple.Tuple2D;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.MAMath.MinMax;

/**
 *
 * Sparse array implementation, mimicking a ucar.ma2.ArrayDouble.D1. Needs two
 *
 * ArrayDouble.D1 arrays for construction, one containing the indices, one
 *
 * containing a value for each index. Note that currently indices are mapped to
 *
 * the nearest integer to mimick default array behaviour and to allow using of
 *
 * sparse arrays like dense arrays.
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
public final class Sparse extends Array {

    /**
     * <p>create.</p>
     *
     * @param indices a {@link ucar.ma2.Array} object.
     * @param values a {@link ucar.ma2.Array} object.
     * @param massBinResolution a double.
     * @return a {@link ucar.ma2.Sparse} object.
     */
    public static Sparse create(Array indices, Array values,
            double massBinResolution) {
        MinMax mm = MAMath.getMinMax(indices);
        int nbins = MaltcmsTools.getNumberOfIntegerMassBins(mm.min,
                mm.max, massBinResolution);
        double min = mm.min;
        double max = mm.max;
        return Sparse.create(indices, values, (int) Math.floor(min), (int) Math
                .ceil(max), nbins, massBinResolution);
    }

    /**
     * <p>create.</p>
     *
     * @param indices a {@link ucar.ma2.Array} object.
     * @param values a {@link ucar.ma2.Array} object.
     * @param minindex a int.
     * @param maxindex a int.
     * @param nbins a int.
     * @param massBinResolution a double.
     * @return a {@link ucar.ma2.Sparse} object.
     */
    public static Sparse create(Array indices, Array values,
            int minindex, int maxindex, int nbins,
            double massBinResolution) {
        return new Sparse(indices, values, minindex, maxindex, nbins,
                massBinResolution);
    }
    private SparseDoubleMatrix1D indToVal = null;
    private int minindex = 0;
    private int maxindex = 0;
    private int bins = 0;

    /**
     * <p>Constructor for Sparse.</p>
     *
     * @param indices a {@link ucar.ma2.Array} object.
     * @param values a {@link ucar.ma2.Array} object.
     * @param minindex1 a int.
     * @param maxindex1 a int.
     * @param nbins a int.
     * @param massBinResolution a double.
     */
    public Sparse(Array indices, Array values, int minindex1,
            int maxindex1, int nbins, double massBinResolution) {
        super(values.getDataType(), Index.factory(new int[]{maxindex1 - minindex1}));
        if (indices.getShape()[0] != values.getShape()[0]) {
            throw new IllegalArgumentException(
                    "Length of input arrays differs!");

        }
        this.bins = nbins;
        this.indToVal = new SparseDoubleMatrix1D(nbins);
        this.minindex = minindex1;
        this.maxindex = maxindex1;
        IndexIterator ii1 = indices.getIndexIterator();
        IndexIterator ii2 = values.getIndexIterator();
        while (ii1.hasNext() && ii2.hasNext()) {
            int index = MaltcmsTools.binMZ(ii1.getDoubleNext(),
                    minindex1, maxindex1, massBinResolution);
            double val = ii2.getDoubleNext();
            if (this.indToVal.get(index) != 0) {
                this.indToVal.set(index, this.indToVal.get(index) + val);
            } else {
                this.indToVal.set(index, val);
            }
        }
    }

    /**
     * <p>Constructor for Sparse.</p>
     *
     * @param bins a int.
     * @param minindex1 a int.
     * @param maxindex1 a int.
     */
    public Sparse(DataType dataType, int bins, int minindex1, int maxindex1) {
        super(dataType, Index.factory(new int[]{maxindex1 - minindex1}));
        this.bins = bins;
        this.indToVal = new SparseDoubleMatrix1D(this.bins);
        this.minindex = minindex1;
        this.maxindex = maxindex1;
    }

    /**
     * <p>Constructor for Sparse.</p>
     *
     * @param sparse a {@link ucar.ma2.Sparse} object.
     */
    public Sparse(Sparse sparse) {
        super(sparse.getDataType(), Index.factory(new int[]{sparse.maxindex - sparse.minindex}));
        this.bins = sparse.bins;
        this.indToVal = (SparseDoubleMatrix1D) sparse.indToVal.clone();
        this.minindex = sparse.minindex;
        this.maxindex = sparse.maxindex;
    }

    /**
     * <p>get.</p>
     *
     * @param arg0 a int.
     * @return a double.
     */
    public double get(int arg0) {
        if (arg0 >= this.indToVal.cardinality()) {
            return 0.0;
        }
        return this.indToVal.get(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(Index arg0) {
        return get(arg0.currentElement());
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getElementType() {
        return DataType.DOUBLE.getClass();
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(Index arg0) {
        return (float) getDouble(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(Index arg0) {
        return (int) getDouble(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(Index arg0) {
        return (long) getDouble(arg0);
    }

    /**
     * <p>getMaxIndex.</p>
     *
     * @return a int.
     */
    public int getMaxIndex() {
        return this.maxindex;
    }

    /**
     * <p>getMinIndex.</p>
     *
     * @return a int.
     */
    public int getMinIndex() {
        return this.minindex;
    }

    /**
     * <p>getNumKeys.</p>
     *
     * @return a int.
     */
    public int getNumKeys() {
        return this.indToVal.cardinality();
    }

    /** {@inheritDoc} */
    @Override
    public int[] getShape() {
        return new int[]{this.indToVal.size()};// this.maxindex -
    }

    /** {@inheritDoc} */
    @Override
    public long getSize() {
        return getShape()[0];
    }

    /**
     * <p>set.</p>
     *
     * @param arg0 a int.
     * @param arg1 a double.
     */
    public void set(int arg0, double arg1) {
        if (arg0 < minindex || arg0 > maxindex) {
            throw new ArrayIndexOutOfBoundsException(arg0);
        }
        this.indToVal.set(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public void setDouble(Index arg0, double arg1) {
        set(arg0.currentElement(), arg1);
    }

    /** {@inheritDoc} */
    @Override
    public void setFloat(Index arg0, float arg1) {
        setDouble(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public void setInt(Index arg0, int arg1) {
        setDouble(arg0, arg1);
    }

    /** {@inheritDoc} */
    @Override
    public void setLong(Index arg0, long arg1) {
        setDouble(arg0, arg1);
    }

    /**
     * <p>toArrays.</p>
     *
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public Tuple2D<ArrayDouble.D1, ArrayDouble.D1> toArrays() {
        ArrayDouble.D1 indices = new ArrayDouble.D1(this.indToVal
                .cardinality());
        ArrayDouble.D1 values = new ArrayDouble.D1(this.indToVal
                .cardinality());
        int i = 0;
        IndexIterator indI = indices.getIndexIterator();
        IndexIterator valI = values.getIndexIterator();
        while ((i < this.indToVal.size()) && indI.hasNext() && valI.hasNext()) {
            Double value = this.indToVal.get(i);
            if (value != 0.0d) {
                indI.setIntNext(i + getMinIndex());
                valI.setDoubleNext(value);
            }
            i++;
        }
        return new Tuple2D<>(indices, values);
    }

    /**
     * <p>get.</p>
     *
     * @param i a {@link ucar.ma2.Index} object.
     * @return a double.
     */
    public double get(Index i) {
        return getDouble(i);
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(Index i) {
        return (short) getDouble(i);
    }

    /** {@inheritDoc} */
    @Override
    public void setShort(Index i, short value) {
        setDouble(i, value);
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(Index i) {
        return (byte) getDouble(i);
    }

    /** {@inheritDoc} */
    @Override
    public void setByte(Index i, byte value) {
        setDouble(i, value);
    }

    /** {@inheritDoc} */
    @Override
    public char getChar(Index i) {
        return (char) getDouble(i);
    }

    /** {@inheritDoc} */
    @Override
    public void setChar(Index i, char value) {
        setDouble(i, value);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(Index i) {
        double d = getDouble(i);
        if (d == 0) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(int index) {
        return get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setDouble(int index, double value) {
        set(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(int index) {
        return (float) get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setFloat(int index, float value) {
        set(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(int index) {
        return (long) get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setLong(int index, long value) {
        set(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(int index) {
        return (int) get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setInt(int index, int value) {
        set(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(int index) {
        return (short) get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setShort(int index, short value) {
        set(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(int index) {
        return (byte) get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setByte(int index, byte value) {
        set(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public char getChar(int index) {
        return (char) get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setChar(int index, char value) {
        set(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(int index) {
        double d = get(index);
        if (d == 0) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setBoolean(int index, boolean value) {
        if (value) {
            set(index, 1.0d);
        } else {
            set(index, 0.0d);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getStorage() {
        return indToVal.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public void setBoolean(Index index, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(Index index) {
        return getDouble(index);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(Index index, Object o) {
        setDouble(index, (double) o);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(int i) {
        return getDouble(i);
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int i, Object o) {
        setDouble(i, (double) o);
    }

    @Override
    protected Array createView(Index index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void copyFrom1DJavaArray(IndexIterator ii, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void copyTo1DJavaArray(IndexIterator ii, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /** {@inheritDoc} */
    @Override
    public Array copy() {
        return new Sparse(this);
    }
}
