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

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Area;
import java.io.Serializable;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayInt;

/**
 * Interface abstraction of 2-dimensional arrays storing double values.
 *
 * @author Nils Hoffmann
 *
 */
public interface IArrayD2Byte extends Serializable {

    /**
     *
     * @return the number of columns
     */
    public abstract int columns();

    /**
     * Return array in row compressed storage format.
     *
     * @return first array int tuple defines offsets of rows in second array
     */
    public abstract Tuple2D<ArrayInt.D1, ArrayByte.D1> flatten();

    /**
     *
     * @param row
     * @param col
     * @return element if row and col are in valid range, otherwise returns
     *         default value
     */
    public abstract byte get(int row, int col);

    /**
     * Use with caution, creates the dense array according to its bounds in
     * memory.
     *
     * @return a dense representation of this array
     */
    public abstract ArrayByte.D2 getArray();

    /**
     *
     * @param row
     * @return [lowest column in range, length of column]
     */
    public abstract int[] getColumnBounds(int row);

    /**
     *
     * @return the default value, e.g. 0, used for initialization
     */
    public abstract byte getDefaultValue();

    /**
     *
     * @return the number of elements stored in this array
     */
    public abstract int getNumberOfStoredElements();

    /**
     *
     * @return the Area enclosed by this Array
     */
    public abstract Area getShape();

    public abstract boolean inRange(int i, int j);

    /**
     *
     * @return the number of rows
     */
    public abstract int rows();

    /**
     *
     * @param row
     * @param col
     * @param b   the value to be set at row,col
     * @throws ArrayIndexOutOfBoundsException
     */
    public abstract void set(int row, int col, byte b)
        throws ArrayIndexOutOfBoundsException;

    /**
     *
     * @return a string representation of this array
     */
    public abstract String toString();
}
