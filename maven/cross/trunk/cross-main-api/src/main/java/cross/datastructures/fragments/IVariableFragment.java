/*
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: IVariableFragment.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */
/**
 * 
 */
package cross.datastructures.fragments;

import cross.io.misc.IArrayChunkIterator;
import java.io.IOException;
import java.util.List;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Range;
import ucar.nc2.Dimension;

/**
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IVariableFragment extends IFragment {

    /**
     * Append structural information of this VariableFragment to Element e.
     */
    @Override
    public abstract void appendXML(Element e);

    @Override
    public abstract int compare(IFragment o1, IFragment o2);

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public abstract int compareTo(Object o);

    /**
     * Read the underlying array, cache it, then return. Or return null if
     * reading fails. Cached array is only updated, if it is set to null by
     * calling setArray(null).
     *
     * @return
     */
    public abstract Array getArray();

    public IArrayChunkIterator getChunkIterator(int chunksize);

    /**
     * Return the DataType assigned to the elements contained in the underlying
     * array.
     *
     * @return
     */
    public abstract DataType getDataType();

    /**
     * Return the Dimensions of this VariableFragment.
     *
     * @return
     */
    public abstract Dimension[] getDimensions();

    /**
     * Return the VariableFragment used to access the data of this
     * VariableFragment by index, can be null, if no index is assigned or
     * needed.
     *
     * @return
     */
    public abstract IVariableFragment getIndex();

    /**
     * Read an Array row by row in Compressed Row Storage by using indices
     * stored in a second array as offsets.
     *
     * @return
     * @throws IOException
     */
    public abstract List<Array> getIndexedArray();

    /**
     * Returns this Fragment's parent FileFragment. Must not be null!
     *
     * @return
     */
    public abstract IFileFragment getParent();

    /**
     * Returns the ranges set for this VariableFragment. When accessing the
     * underlying array data, the ranges are used to constrain the reading of
     * array data to portions defined in them.
     *
     * @return
     */
    public abstract Range[] getRange();

    /**
     * Return the name of this VariableFragment.
     *
     * Use <code>getName()</code> instead.
     *
     * @return
     */
    @Deprecated
    public abstract String getVarname();

    /**
     * Return the name of this VariableFragment.
     *
     * @return
     */
    public abstract String getName();

    /**
     * Call to see, whether this VariableFragment already has an array set.
     *
     * @return
     */
    public abstract boolean hasArray();

    @Override
    public abstract int hashCode();

    /**
     * Return, whether this IVariable fragment has been modified since the last
     * save. Modifications include setting the array, indexed array and
     * attributes.
     *
     * @return
     */
    public abstract boolean isModified();

    /**
     * Explicitly set an array, may be null. Will also clear indexed array.
     *
     * @param a1
     */
    public abstract void setArray(Array a1);

    /**
     * Set the DataType.
     *
     * @param dataType1
     */
    public abstract void setDataType(DataType dataType1);

    /**
     * Set the Dimensions of this VariableFragment. Automatically adds those
     * Dimensions to the parent.
     *
     * @param dims1
     */
    public abstract void setDimensions(Dimension[] dims1);

    /**
     * Every VariableFragment obtains a reference to an IndexFragment, to make
     * sure, that the associated Variable can be read at any time.
     *
     * @param index1
     */
    public abstract void setIndex(IVariableFragment index1);

    /**
     * Explicitly set an indexed array, may be null.
     *
     * @param al1
     */
    public abstract void setIndexedArray(List<Array> al1);

    /**
     * Sets this IVariableFragment's state to the value of parameter b. Used by
     * IFileFragment to indicate to VariableFragment, that its contents have
     * been saved.
     *
     * @param b
     */
    public abstract void setIsModified(boolean b);

    /**
     * Set ranges for this VariableFragment.
     *
     * @param ranges1
     */
    public abstract void setRange(Range[] ranges1);

    @Override
    public abstract String toString();
}
