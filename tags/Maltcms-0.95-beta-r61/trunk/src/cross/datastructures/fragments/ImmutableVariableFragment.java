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
package cross.datastructures.fragments;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import cross.datastructures.StatsMap;
import cross.io.misc.ArrayChunkIterator;
import cross.tools.EvalTools;

/**
 * Immutable Variant of a VariableFragment. All set operations will throw
 * UnsupportedOperationException. All other operations delegate to an instance
 * of IVariableFragment, which is provided to the constructor.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
public class ImmutableVariableFragment implements IVariableFragment {

	private IVariableFragment vf = null;

	public ImmutableVariableFragment(final IVariableFragment vf2) {
		EvalTools.notNull(vf2, this);
		this.vf = vf2;
	}

	/**
	 * @param e
	 * @see cross.datastructures.fragments.IVariableFragment#appendXML(org.jdom.Element)
	 */
	public void appendXML(final Element e) {
		this.vf.appendXML(e);
	}

	/**
	 * @param o1
	 * @param o2
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#compare(cross.datastructures.fragments.Fragment,
	 *      cross.datastructures.fragments.Fragment)
	 */
	public int compare(final IFragment o1, final IFragment o2) {
		return this.vf.compare(o1, o2);
	}

	/**
	 * @param o
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#compareTo(java.lang.Object)
	 */
	public int compareTo(final Object o) {
		return this.vf.compareTo(o);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.util.Comparator#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return this.vf.equals(obj);
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getArray()
	 */
	public Array getArray() {
		return this.vf.getArray().copy();
	}

	/**
	 * @param a
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getAttribute(ucar.nc2.Attribute)
	 */
	public Attribute getAttribute(final Attribute a) {
		return this.vf.getAttribute(a);
	}

	/**
	 * @param name
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getAttribute(java.lang.String)
	 */
	public Attribute getAttribute(final String name) {
		return this.vf.getAttribute(name);
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getAttributes()
	 */
	public List<Attribute> getAttributes() {
		return this.vf.getAttributes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.fragments.IVariableFragment#getChunkIterator(int)
	 */
	public ArrayChunkIterator getChunkIterator(final int chunksize) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getDataType()
	 */
	public DataType getDataType() {
		return this.vf.getDataType();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getDimensions()
	 */
	public Dimension[] getDimensions() {
		return this.vf.getDimensions();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getIndex()
	 */
	public IVariableFragment getIndex() {
		if (this.vf.getIndex() instanceof ImmutableVariableFragment) {
			return this.vf.getIndex();
		}
		return new ImmutableVariableFragment(this.vf.getIndex());
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getIndexedArray()
	 */
	public ArrayList<Array> getIndexedArray() {
		final ArrayList<Array> al = new ArrayList<Array>(this.vf
		        .getIndexedArray().size());
		final List<Array> indxd = this.vf.getIndexedArray();
		for (final Array a : indxd) {
			al.add(a.copy());
		}
		return al;
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getParent()
	 */
	public IFileFragment getParent() {
		// if(this.vf.getParent() instanceof ImmutableFileFragment) {
		// return this.vf.getParent();
		// }
		// return new ImmutableFileFragment(this.vf.getParent());
		return this.vf.getParent();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getRange()
	 */
	public Range[] getRange() {
		return this.vf.getRange();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IFragment#getStats()
	 */
	public StatsMap getStats() {
		return this.vf.getStats();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#getVarname()
	 */
	public String getVarname() {
		return this.vf.getVarname();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#hasArray()
	 */
	public boolean hasArray() {
		return this.vf.hasArray();
	}

	/**
	 * @param a
	 * @return
	 * @see cross.datastructures.fragments.IFragment#hasAttribute(ucar.nc2.Attribute)
	 */
	public boolean hasAttribute(final Attribute a) {
		return this.vf.hasAttribute(a);
	}

	/**
	 * @param name
	 * @return
	 * @see cross.datastructures.fragments.IFragment#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(final String name) {
		return this.vf.hasAttribute(name);
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.vf.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.fragments.IVariableFragment#isModified()
	 */
	@Override
	public boolean isModified() {
		return false;
	}

	/**
	 * @param a1
	 * @see cross.datastructures.fragments.IVariableFragment#setArray(ucar.ma2.Array)
	 */
	public void setArray(final Array a1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param a
	 * @see cross.datastructures.fragments.IFragment#setAttributes(ucar.nc2.Attribute[])
	 */
	public void setAttributes(final Attribute... a) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param dataType1
	 * @see cross.datastructures.fragments.IVariableFragment#setDataType(ucar.ma2.DataType)
	 */
	public void setDataType(final DataType dataType1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param dims1
	 * @see cross.datastructures.fragments.IVariableFragment#setDimensions(ucar.nc2.Dimension[])
	 */
	public void setDimensions(final Dimension[] dims1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param index1
	 * @see cross.datastructures.fragments.IVariableFragment#setIndex(cross.datastructures.fragments.IVariableFragment)
	 */
	public void setIndex(final IVariableFragment index1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param al1
	 * @see cross.datastructures.fragments.IVariableFragment#setIndexedArray(java.util.ArrayList)
	 */
	public void setIndexedArray(final List<Array> al1) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.datastructures.fragments.IVariableFragment#setIsModified(boolean)
	 */
	@Override
	public void setIsModified(final boolean b) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param ranges1
	 * @see cross.datastructures.fragments.IVariableFragment#setRange(ucar.ma2.Range[])
	 */
	public void setRange(final Range[] ranges1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param stats1
	 * @see cross.datastructures.fragments.IFragment#setStats(cross.datastructures.StatsMap)
	 */
	public void setStats(final StatsMap stats1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param varname1
	 * @see cross.datastructures.fragments.IVariableFragment#setVarname(java.lang.String)
	 */
	public void setVarname(final String varname1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return
	 * @see cross.datastructures.fragments.IVariableFragment#toString()
	 */
	@Override
	public String toString() {
		return this.vf.toString();
	}

}
