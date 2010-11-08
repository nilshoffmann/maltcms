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
 * $Id: ArrayFactory.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */

/**
 * 
 */
package maltcms.datastructures.array;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.constraint.ConstraintFactory;
import ucar.ma2.Array;

/**
 * A factory abstraction for creation of IArrayD2Double implementations.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ArrayFactory {

	/**
	 * Wraps an Array within a DenseArray. Data of a is copied.
	 * 
	 * @param a
	 * @return
	 */
	public IArrayD2Double create(final Array a) {
		return new DenseArray(a);
	}

	/**
	 * Creates a PartitionedArray if number of passed in anchors is greater than
	 * 2. If a band constraint > 0 is also given, the resulting layout will be
	 * the intersection of both constraints' shapes. If no anchor pair set is
	 * given, but a band constraint has been set >0, than this will return a
	 * PartitionedArray with the corresponding band constraint. Otherwise, a
	 * DenseArray is returned.
	 * 
	 * @param rows
	 * @param cols
	 * @param aps
	 * @param neighborhood
	 * @param band
	 * @param default_value
	 * @param globalBand
	 * @return
	 */
	public IArrayD2Double create(final int rows, final int cols,
	        final AnchorPairSet aps, final int neighborhood, final double band,
	        final double default_value, final boolean globalBand) {
		// More than start and end are present
		if (aps.getSize() > 2) {
			return PartitionedArray.create(rows, cols, aps, neighborhood, band,
			        default_value, globalBand);
		} else if (band > 0.0d) {
			return PartitionedArray.create(rows, cols, default_value,
			        ConstraintFactory.getInstance().createBandConstraint(0, 0,
			                rows, cols, band));
		}
		return create(rows, cols, default_value);
	}

	/**
	 * Creates a DenseArray with given number of rows, columns and default_value
	 * as initial value of elements.
	 * 
	 * @param rows
	 * @param cols
	 * @param default_value
	 * @return
	 */
	public IArrayD2Double create(final int rows, final int cols,
	        final double default_value) {
		return new DenseArray(rows, cols, default_value);
	}

	/**
	 * Create an optimized array storing only those elements contained within
	 * the area bounds.
	 * 
	 * @param rows
	 * @param cols
	 * @param default_value
	 * @param bounds
	 * @return
	 */
	public IArrayD2Double create(final int rows, final int cols,
	        final double default_value, final Area bounds) {
		return PartitionedArray.create(rows, cols, default_value, bounds);
	}

	/**
	 * Creates a copied layout. Underlying column start, length and offset
	 * arrays are copied. Default value, number of virtual rows and number of
	 * virtual columns are copied.
	 * 
	 * @param ia
	 *            the array used as blueprint for layout
	 * @return
	 */
	public IArrayD2Double createCopiedLayout(final IArrayD2Double ia) {
		if (ia instanceof PartitionedArray) {
			return PartitionedArray.copyLayout((PartitionedArray) ia);
		}
		return create(ia.rows(), ia.columns(), ia.getDefaultValue());
	}

	public BufferedImage createLayoutImage(final IArrayD2Double ia) {
		if (ia instanceof DenseArray) {
			return DenseArray.createLayoutImage((DenseArray) ia,
			        Color.lightGray, Color.darkGray);
		} else if (ia instanceof PartitionedArray) {
			return PartitionedArray.createLayoutImage((PartitionedArray) ia,
			        Color.lightGray, Color.darkGray);
		}
		throw new IllegalArgumentException("Unsupported IArrayD2Double class!");
	}

	/**
	 * Creating a shared layout is recommended, if you want to use a layout for
	 * multiple arrays. Underlying column start, length and offset arrays are
	 * shared by reference. Default value, number of virtual rows and number of
	 * virtual columns are copied.
	 * 
	 * @param ia
	 *            the array used as blueprint for layout
	 * @return either a PartitionedArray if ia is a PartitionedArray, otherwise
	 *         a DenseArray
	 */
	public IArrayD2Double createSharedLayout(final IArrayD2Double ia) {
		if (ia instanceof PartitionedArray) {
			return PartitionedArray.shareLayout((PartitionedArray) ia);
		}
		return create(ia.rows(), ia.columns(), ia.getDefaultValue());
	}

	/**
	 * Create a two-dimensional array, which is backed by some sparse
	 * implementation (rcs or hashing). Virtual layout size is given by rows and
	 * columns. Default value is fixed to 0.
	 * 
	 * @param rows
	 * @param columns
	 * @return
	 */
	public IArrayD2Double createSparseArray(final int rows, final int columns) {
		return new SparseArray(rows, columns);
	}

}
