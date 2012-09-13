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
package maltcms.datastructures.array;

import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.Index1D;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
import ucar.ma2.ArrayDouble.D1;
import ucar.ma2.MAMath.MinMax;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cross.datastructures.tuple.Tuple2D;

/**
 * Sparse array implementation, mimicking a ucar.ma2.ArrayDouble.D1. Needs two
 * ArrayDouble.D1 arrays for construction, one containing the indices, one
 * containing a value for each index. Note that currently indices are mapped to
 * the nearest integer to mimick default array behaviour and to allow using of
 * sparse arrays like dense arrays.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class Sparse extends D1 {

    public static Sparse create(final Array indices, final Array values,
            final double massBinResolution) {
        final MinMax mm = MAMath.getMinMax(indices);
        final int nbins = MaltcmsTools.getNumberOfIntegerMassBins(mm.min,
                mm.max, massBinResolution);
        final double min = mm.min;// MaltcmsTools.binMZ(mm.min,mm.min,mm.max,
        // massBinResolution);
        final double max = mm.max;// MaltcmsTools.binMZ(mm.max,mm.min,mm.max,
        // massBinResolution);
        // System.out.println("Min: "+min+" Max: "+max+" bins: "+nbins);
        return Sparse.create(indices, values, (int) Math.floor(min), (int) Math
                .ceil(max), nbins, massBinResolution);
    }

    public static Sparse create(final Array indices, final Array values,
            final int minindex, final int maxindex, final int nbins,
            final double massBinResolution) {
        return new Sparse(indices, values, minindex, maxindex, nbins,
                massBinResolution);
    }
    /**
     * LinkedHashMap keeps a doubly linked list of all keys, in order of
     * insertion
     */
    // private LinkedHashMap<Integer, Double> indToVal = null;
    private SparseDoubleMatrix1D indToVal = null;
    private int minindex = 0;
    private int maxindex = 0;
    private int bins = 0;

    public Sparse(final Array indices, final Array values, final int minindex1,
            final int maxindex1, final int nbins, final double massBinResolution) {
        super(1);// maxindex-minindex);
        if (indices.getShape()[0] != values.getShape()[0]) {
            throw new IllegalArgumentException(
                    "Length of input arrays differs!");
        }
        this.bins = nbins;
        this.indToVal = new SparseDoubleMatrix1D(nbins);
        this.minindex = minindex1;
        this.maxindex = maxindex1;
        final IndexIterator ii1 = indices.getIndexIterator();
        final IndexIterator ii2 = values.getIndexIterator();
        while (ii1.hasNext() && ii2.hasNext()) {
            final int index = MaltcmsTools.binMZ(ii1.getDoubleNext(),
                    minindex1, maxindex1, massBinResolution);
            final double val = ii2.getDoubleNext();
            // System.out.println("Idx: "+index+" = "+val);
            if (this.indToVal.get(index) != 0) {
                this.indToVal.set(index, this.indToVal.get(index) + val);
            } else {
                this.indToVal.set(index, val);
            }
            // this.minindex = Math.min(this.minindex, index);
            // this.maxindex = Math.max(this.maxindex, index);
        }
    }

    public Sparse(final int arg0, final int minindex1, final int maxindex1) {
        super(1);// maxindex-minindex);
        this.bins = arg0;
        this.indToVal = new SparseDoubleMatrix1D(this.bins);
        this.minindex = minindex1;
        this.maxindex = maxindex1;
    }

    @Override
    public double get(final int arg0) {
        if (arg0 >= this.indToVal.cardinality()) {
            return 0.0;
        }
        // if (this.indToVal.containsKey(arg0)) {
        return this.indToVal.get(arg0);
        // } else {
        // return this.mv;
        // }
    }

    @Override
    public double getDouble(final Index arg0) {
        return get(arg0.currentElement());
    }

    @Override
    public Class<?> getElementType() {
        return DataType.DOUBLE.getClass();
    }

    @Override
    public float getFloat(final Index arg0) {
        return (float) getDouble(arg0);
    }

    @Override
    public Index getIndex() {
        return new Index1D(getShape());
    }

    @Override
    public IndexIterator getIndexIterator() {
        return new IndexIterator() {
            // Set<Integer> s = indToVal.keySet();
            // Iterator<Integer> iter = s.iterator();
            int counter = 0;

            public boolean getBooleanCurrent() {
                if (get(this.counter) == 0.0d) {
                    return false;
                }
                return true;
            }

            public boolean getBooleanNext() {
                if (get(this.counter++) == 0.0d) {
                    return false;
                }
                return true;
            }

            public byte getByteCurrent() {
                return (byte) get(this.counter);
            }

            public byte getByteNext() {
                return (byte) get(this.counter++);
            }

            public char getCharCurrent() {
                return (char) get(this.counter);
            }

            public char getCharNext() {
                return (char) get(this.counter++);
            }

            public int[] getCurrentCounter() {
                return new int[]{this.counter};
            }

            public double getDoubleCurrent() {
                return get(this.counter);
            }

            public double getDoubleNext() {
                return get(this.counter++);
            }

            public float getFloatCurrent() {
                return (float) get(this.counter);
            }

            public float getFloatNext() {
                return (float) get(this.counter++);
            }

            public int getIntCurrent() {
                return (int) get(this.counter);
            }

            public int getIntNext() {
                return (int) get(this.counter++);
            }

            public long getLongCurrent() {
                return (long) get(this.counter);
            }

            public long getLongNext() {
                return (long) get(this.counter++);
            }

            public Object getObjectCurrent() {
                return get(this.counter);
            }

            public Object getObjectNext() {
                return get(this.counter++);
            }

            public short getShortCurrent() {
                return (short) get(this.counter);
            }

            public short getShortNext() {

                return (short) (get(this.counter++));
            }

            public boolean hasNext() {
                if (this.counter < getNumKeys()) {
                    return true;
                }
                return false;
            }

            public Object next() {
                return get(this.counter++);
            }

            public void setBooleanCurrent(final boolean arg0) {
                if (arg0) {
                    set(this.counter, 1.0d);
                } else {
                    set(this.counter, 0.0d);
                }
            }

            public void setBooleanNext(final boolean arg0) {
                if (arg0) {
                    set(this.counter++, 1.0d);
                } else {
                    set(this.counter++, 0.0d);
                }

            }

            public void setByteCurrent(final byte arg0) {
                set(this.counter, arg0);

            }

            public void setByteNext(final byte arg0) {
                set(this.counter++, arg0);

            }

            public void setCharCurrent(final char arg0) {
                set(this.counter, arg0);

            }

            public void setCharNext(final char arg0) {
                set(this.counter++, arg0);

            }

            public void setDoubleCurrent(final double arg0) {
                set(this.counter, arg0);
            }

            public void setDoubleNext(final double arg0) {
                set(this.counter++, arg0);
            }

            public void setFloatCurrent(final float arg0) {
                set(this.counter, arg0);

            }

            public void setFloatNext(final float arg0) {
                set(this.counter++, arg0);
            }

            public void setIntCurrent(final int arg0) {
                set(this.counter, arg0);

            }

            public void setIntNext(final int arg0) {
                set(this.counter++, arg0);
            }

            public void setLongCurrent(final long arg0) {
                set(this.counter, arg0);

            }

            public void setLongNext(final long arg0) {
                set(this.counter++, arg0);
            }

            public void setObjectCurrent(final Object arg0) {
                if (arg0 instanceof Double) {
                    set(this.counter, (Double) arg0);
                }

            }

            public void setObjectNext(final Object arg0) {
                if (arg0 instanceof Double) {
                    set(this.counter++, (Double) arg0);
                }
            }

            public void setShortCurrent(final short arg0) {
                set(this.counter, arg0);
            }

            public void setShortNext(final short arg0) {
                set(this.counter++, arg0);
            }
        };

    }

    @Override
    public IndexIterator getIndexIteratorFast() {
        return getIndexIterator();
    }

    @Override
    public int getInt(final Index arg0) {
        return (int) getDouble(arg0);
    }

    @Override
    public long getLong(final Index arg0) {
        return (long) getDouble(arg0);
    }

    public int getMaxIndex() {
        return this.maxindex;
    }

    public int getMinIndex() {
        return this.minindex;
    }

    public int getNumKeys() {
        return this.indToVal.cardinality();
    }

    @Override
    public int[] getShape() {
        // return new int[] { this.maxindex - this.minindex };
        return new int[]{this.indToVal.size()};// this.maxindex -
        // this.minindex + 1};
    }

    @Override
    public long getSize() {
        return getShape()[0];
    }

    @Override
    public void set(final int arg0, final double arg1) {
        // if (arg0 < this.minindex) {
        // this.minindex = arg0;
        // }
        // if (arg0 > this.maxindex) {
        // this.maxindex = arg0;
        // }
        // this.map_minindex = Math.min(this.map_minindex, arg0);
        // this.map_maxindex = Math.max(this.map_maxindex, arg0);
        this.indToVal.set(arg0, arg1);
    }

    @Override
    public void setDouble(final Index arg0, final double arg1) {
        set(arg0.currentElement(), arg1);
    }

    @Override
    public void setFloat(final Index arg0, final float arg1) {
        setDouble(arg0, arg1);
    }

    @Override
    public void setInt(final Index arg0, final int arg1) {
        setDouble(arg0, arg1);
    }

    @Override
    public void setLong(final Index arg0, final long arg1) {
        setDouble(arg0, arg1);
    }

    public Tuple2D<ArrayDouble.D1, ArrayDouble.D1> toArrays() {
        final ArrayDouble.D1 indices = new ArrayDouble.D1(this.indToVal
                .cardinality());
        final ArrayDouble.D1 values = new ArrayDouble.D1(this.indToVal
                .cardinality());
        int i = 0;
        final IndexIterator indI = indices.getIndexIterator();
        final IndexIterator valI = values.getIndexIterator();
        while ((i < this.indToVal.size()) && indI.hasNext() && valI.hasNext()) {
            final Double value = this.indToVal.get(i);
            if (value != 0.0d) {
                indI.setIntNext(i + getMinIndex());
                valI.setDoubleNext(value);
            }
            i++;
        }
        return new Tuple2D<ArrayDouble.D1, ArrayDouble.D1>(indices, values);
    }
}
