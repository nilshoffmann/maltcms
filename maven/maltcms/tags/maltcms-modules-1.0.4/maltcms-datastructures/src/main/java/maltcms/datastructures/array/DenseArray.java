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
 * $Id: DenseArray.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */

/**
 * 
 */
package maltcms.datastructures.array;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayDouble.D2;
import ucar.ma2.ArrayInt.D1;
import cross.datastructures.tuple.Tuple2D;

/**
 * A simple dense array encapsulation class.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class DenseArray implements IArrayD2Double {

	public static BufferedImage createLayoutImage(final DenseArray pa,
	        final Color bg, final Color fg) {
		final BufferedImage bi = new BufferedImage(pa.rows(), pa.columns(),
		        BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g = (Graphics2D) bi.getGraphics();
		final Color b = bg;
		g.setColor(b);
		g.fill(new Rectangle(0, 0, pa.rows(), pa.columns()));
		final Color c = fg;
		g.setColor(c);
		g.fill(new Rectangle(0, 0, pa.rows(), pa.columns()));
		return bi;
	}

	private ArrayDouble.D2 data = null;

	private double defaultValue = 0;

	/**
	 * Copy a and save it in internal array
	 * 
	 * @param a
	 */
	public DenseArray(final Array a) {
		if (a instanceof ArrayDouble.D2) {
			this.data = (ArrayDouble.D2) a.copy();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Create new Array with specified number of rows and columns. Initialize
	 * with defaultValue1.
	 * 
	 * @param rows
	 * @param columns
	 * @param defaultValue1
	 */
	public DenseArray(final int rows, final int columns,
	        final double defaultValue1) {
		this.data = new ArrayDouble.D2(rows, columns);
		this.defaultValue = defaultValue1;
		ArrayTools.fill(this.data, defaultValue1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.alignment.IArrayD2Double#columns()
	 */
	@Override
	public int columns() {
		return this.data.getShape()[1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#flatten()
	 */
	@Override
	public Tuple2D<D1, ucar.ma2.ArrayDouble.D1> flatten() {
		final ArrayDouble.D1 arr = new ArrayDouble.D1(
		        getNumberOfStoredElements());
		final ArrayInt.D1 si = new ArrayInt.D1(rows());
		int offset = 0;
		for (int i = 0; i < rows(); i++) {
			si.set(i, 0 + offset);
			for (int j = 0; j < columns(); j++) {
				arr.set(offset + j, get(i, j));
			}
			offset += columns();
		}
		return new Tuple2D<D1, ucar.ma2.ArrayDouble.D1>(si, arr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.alignment.IArrayD2Double#get(int, int)
	 */
	@Override
	public double get(final int row, final int col) {
		return this.data.get(row, col);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.alignment.IArrayD2Double#getArray()
	 */
	@Override
	public D2 getArray() {
		return (ArrayDouble.D2) this.data.copy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#getColumnBounds(int)
	 */
	@Override
	public int[] getColumnBounds(final int row) {
		return new int[] { 0, columns() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.alignment.IArrayD2Double#getDefaultValue()
	 */
	@Override
	public double getDefaultValue() {
		return this.defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.datastructures.alignment.IArrayD2Double#getNumberOfStoredElements
	 * ()
	 */
	@Override
	public int getNumberOfStoredElements() {
		return (columns() * rows());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.array.IArrayD2Double#getShape()
	 */
	@Override
	public Area getShape() {
		return new Area(new Rectangle(0, 0, columns(), rows()));
	}

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
	 * @see maltcms.datastructures.alignment.IArrayD2Double#rows()
	 */
	@Override
	public int rows() {
		return this.data.getShape()[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.datastructures.alignment.IArrayD2Double#set(int, int,
	 * double)
	 */
	@Override
	public void set(final int row, final int col, final double d)
	        throws ArrayIndexOutOfBoundsException {
		this.data.set(row, col, d);
	}

}
