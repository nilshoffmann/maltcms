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

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.constraint.ConstraintFactory;
import ucar.ma2.Array;

/**
 * A factory abstraction for creation of IArrayD2Double implementations.
 *
 * @author Nils Hoffmann
 * 
 */
public class ArrayFactory {

    /**
     * Wraps an Array within a DenseArray. Data of a is copied.
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @return a {@link maltcms.datastructures.array.IArrayD2Double} object.
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
     * @param rows a int.
     * @param cols a int.
     * @param aps a {@link maltcms.datastructures.alignment.AnchorPairSet} object.
     * @param neighborhood a int.
     * @param band a double.
     * @param default_value a double.
     * @param globalBand a boolean.
     * @return a {@link maltcms.datastructures.array.IArrayD2Double} object.
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
     * @param rows a int.
     * @param cols a int.
     * @param default_value a double.
     * @return a {@link maltcms.datastructures.array.IArrayD2Double} object.
     */
    public IArrayD2Double create(final int rows, final int cols,
            final double default_value) {
        return new DenseArray(rows, cols, default_value);
    }

    /**
     * Create an optimized array storing only those elements contained within
     * the area bounds.
     *
     * @param rows a int.
     * @param cols a int.
     * @param default_value a double.
     * @param bounds a {@link java.awt.geom.Area} object.
     * @return a {@link maltcms.datastructures.array.IArrayD2Double} object.
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
     * @param ia the array used as blueprint for layout
     * @return a {@link maltcms.datastructures.array.IArrayD2Double} object.
     */
    public IArrayD2Double createCopiedLayout(final IArrayD2Double ia) {
        if (ia instanceof PartitionedArray) {
            return PartitionedArray.copyLayout((PartitionedArray) ia);
        }
        return create(ia.rows(), ia.columns(), ia.getDefaultValue());
    }

    /**
     * <p>createLayoutImage.</p>
     *
     * @param ia a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @return a {@link java.awt.image.BufferedImage} object.
     */
    public BufferedImage createLayoutImage(final IArrayD2Double ia) {
        if (ia instanceof DenseArray) {
            return DenseArray.createLayoutImage((DenseArray) ia,
                    Color.lightGray, Color.darkGray);
        } else if (ia instanceof PartitionedArray) {
            return PartitionedArray.createLayoutImage((PartitionedArray) ia,
                    Color.lightGray, Color.darkGray);
        }
        throw new IllegalArgumentException("Unsupported IArrayD2Double object of class: " + ia.getClass().getName());
    }

    /**
     * Creating a shared layout is recommended, if you want to use a layout for
     * multiple arrays. Underlying column start, length and offset arrays are
     * shared by reference. Default value, number of virtual rows and number of
     * virtual columns are copied.
     *
     * @param ia the array used as blueprint for layout
     * @return either a PartitionedArray if ia is a PartitionedArray, otherwise
     * a DenseArray
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
     * @param rows a int.
     * @param columns a int.
     * @return a {@link maltcms.datastructures.array.IArrayD2Double} object.
     */
    public IArrayD2Double createSparseArray(final int rows, final int columns) {
        return new SparseArray(rows, columns);
    }
}
