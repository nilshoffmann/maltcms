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

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Area;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.PriorityQueue;
import ucar.ma2.ArrayDouble.D2;
import ucar.ma2.ArrayInt.D1;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class TiledArray implements IArrayD2Double {

    /**
     *
     */
    private static final long serialVersionUID = -3193556606479661979L;
    private IArrayD2Double[] arrays;
    private int tilesPerRow = 0;
    private int tileSizeRows, tileSizeCols;
    private int cols, rows;
    private double defaultValue = 0;
    private File[] filenames;
    private PriorityQueue<IArrayD2Double> pq;
    private long[] arrayATime;

    public TiledArray(int tilesRows, int tilesCols, int rows, int cols,
        double defaultValue) {
        this.tileSizeRows = (int) Math.ceil((float) rows / (float) tilesRows);
        this.tileSizeCols = (int) Math.ceil((float) cols / (float) tilesCols);
        this.tilesPerRow = tilesCols;
        this.rows = rows;
        this.cols = cols;
        arrays = new IArrayD2Double[tilesRows * tilesCols];
        filenames = createFileNames(arrays.length);
        this.defaultValue = defaultValue;
        this.arrayATime = new long[arrays.length];
        this.pq = new PriorityQueue<IArrayD2Double>();
    }

    private int idx(int x, int y) {
        return (tilesPerRow * y) + x;
    }

    // private int tileIndex(int x, int y) {
    // (x/tilesRows)*tileSizeCols
    // }
    private File[] createFileNames(int length) {
        File[] f = new File[length];
        for (int i = 0; i < length; i++) {
            try {
                f[i] = File.createTempFile(super.hashCode() + "_" + i, null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return f;
    }

    private void writeToDisk(int i, IArrayD2Double arr) {
        File f = filenames[i];
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(
                new FileOutputStream(f)));
            oos.writeObject(arr);
            oos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private IArrayD2Double acquireFromDisk(int i) {
        File f = filenames[i];
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(f)));
            Object o = ois.readObject();
            ois.close();
            if (o instanceof IArrayD2Double) {
                return (IArrayD2Double) o;
            } else {
                throw new ClassCastException();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public IArrayD2Double getTile(int x, int y) {
        final int index = idx(x, y);
        if (this.arrays[idx(x, y)] == null) {
            IArrayD2Double arr = acquireFromDisk(index);
            if (arr == null) {
                this.arrays[index] = new DenseArray(tileSizeRows, tileSizeCols,
                    this.defaultValue);
            } else {
                this.arrays[index] = arr;
            }
        }
        return this.arrays[index];
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#columns()
     */
    @Override
    public int columns() {
        return this.cols;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#flatten()
     */
    @Override
    public Tuple2D<D1, ucar.ma2.ArrayDouble.D1> flatten() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#get(int, int)
     */
    @Override
    public double get(int row, int col) {
        int index = idx(row, col);
        return index;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getArray()
     */
    @Override
    public D2 getArray() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getColumnBounds(int)
     */
    @Override
    public int[] getColumnBounds(int row) {
        return new int[]{0, columns()};
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getDefaultValue()
     */
    @Override
    public double getDefaultValue() {
        return this.defaultValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.datastructures.array.IArrayD2Double#getNumberOfStoredElements()
     */
    @Override
    public int getNumberOfStoredElements() {
        int cnt = 0;
        for (int i = 0; i < this.arrays.length; i++) {
            if (this.arrays[i] != null) {
                cnt += arrays[i].getNumberOfStoredElements();
            }
        }
        return cnt;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#getShape()
     */
    @Override
    public Area getShape() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#inRange(int, int)
     */
    @Override
    public boolean inRange(int i, int j) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#rows()
     */
    @Override
    public int rows() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.datastructures.array.IArrayD2Double#set(int, int, double)
     */
    @Override
    public void set(int row, int col, double d)
        throws ArrayIndexOutOfBoundsException {
        // TODO Auto-generated method stub
    }
}
