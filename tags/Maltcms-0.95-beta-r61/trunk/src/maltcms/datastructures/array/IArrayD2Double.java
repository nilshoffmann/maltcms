/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

/**
 * 
 */
package maltcms.datastructures.array;

import java.awt.geom.Area;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import cross.datastructures.tuple.Tuple2D;

/**
 * Interface abstraction of 2-dimensional arrays storing double values.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IArrayD2Double {

	/**
	 * 
	 * @return the number of columns
	 */
	public abstract int columns();

	/**
	 * 
	 * @param row
	 * @param col
	 * @return element if row and col are in valid range, otherwise returns
	 *         default value
	 */
	public abstract double get(int row, int col);

	/**
	 * Use with caution, creates the dense array according to its bounds in
	 * memory.
	 * 
	 * @return a dense representation of this array
	 */
	public abstract ArrayDouble.D2 getArray();

	/**
	 * Return array in row compressed storage format.
	 * 
	 * @return first array int tuple defines offsets of rows in second array
	 */
	public abstract Tuple2D<ArrayInt.D1, ArrayDouble.D1> flatten();

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
	public abstract double getDefaultValue();

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
	 * @param d
	 *            the value to be set at row,col
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public abstract void set(int row, int col, double d)
	        throws ArrayIndexOutOfBoundsException;

	/**
	 * 
	 * @return a string representation of this array
	 */
	public abstract String toString();

}
