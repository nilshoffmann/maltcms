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
 * $Id: ImmutableVariableFragment.java 110 2010-03-25 15:21:19Z nilshoffmann $
 */
/**
 *
 */
package cross.datastructures.fragments;

import cross.datastructures.StatsMap;
import cross.datastructures.tools.EvalTools;
import cross.io.misc.ArrayChunkIterator;
import java.util.Collections;
import java.util.List;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * Immutable Variant of a VariableFragment. All set operations will throw an
 * UnsupportedOperationException. All other operations delegate to an instance
 * of IVariableFragment, which is provided to the constructor.
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * @deprecated Use {@link cross.datastructures.fragments.ImmutableVariableFragment2}
 * instead
 */
@Deprecated
public class ImmutableVariableFragment implements IVariableFragment {

    private final IVariableFragment vf;

    public ImmutableVariableFragment(final IVariableFragment vf2) {
        this.vf = vf2;
        EvalTools.notNull(vf2, this);
    }

    /**
     * @param e
     * @see
     * cross.datastructures.fragments.IVariableFragment#appendXML(org.jdom.Element)
     */
    @Override
    public void appendXML(final Element e) {
        this.vf.appendXML(e);
    }

    /**
     * @param o1
     * @param o2
     * @return
     * @see
     * cross.datastructures.fragments.IVariableFragment#compare(cross.datastructures.fragments.Fragment,
     * cross.datastructures.fragments.Fragment)
     */
    @Override
    public int compare(final IFragment o1, final IFragment o2) {
        return this.vf.compare(o1, o2);
    }

    /**
     * @param o
     * @return
     * @see
     * cross.datastructures.fragments.IVariableFragment#compareTo(java.lang.Object)
     */
    @Override
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
     * @return @see cross.datastructures.fragments.IVariableFragment#getArray()
     */
    @Override
    public Array getArray() {
        return this.vf.getArray().copy();
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(ucar.nc2.Attribute)
     */
    @Override
    public Attribute getAttribute(final Attribute a) {
        return this.vf.getAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#getAttribute(java.lang.String)
     */
    @Override
    public Attribute getAttribute(final String name) {
        return this.vf.getAttribute(name);
    }

    /**
     * @return @see cross.datastructures.fragments.IFragment#getAttributes()
     */
    @Override
    public List<Attribute> getAttributes() {
        return this.vf.getAttributes();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#getChunkIterator(int)
     */
    @Override
    public ArrayChunkIterator getChunkIterator(final int chunksize) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return @see
     * cross.datastructures.fragments.IVariableFragment#getDataType()
     */
    @Override
    public DataType getDataType() {
        return this.vf.getDataType();
    }

    /**
     * @return @see
     * cross.datastructures.fragments.IVariableFragment#getDimensions()
     */
    @Override
    public Dimension[] getDimensions() {
        return this.vf.getDimensions();
    }

    /**
     * @return @see cross.datastructures.fragments.IVariableFragment#getIndex()
     */
    @Override
    public IVariableFragment getIndex() {
        return this.vf.getIndex();
    }

    /**
     * @return @see
     * cross.datastructures.fragments.IVariableFragment#getIndexedArray()
     */
    @Override
    public List<Array> getIndexedArray() {
        return Collections.unmodifiableList(this.vf.getIndexedArray());
    }

    /**
     * @return @see cross.datastructures.fragments.IVariableFragment#getParent()
     */
    @Override
    public IFileFragment getParent() {
        // if(this.vf.getParent() instanceof ImmutableFileFragment) {
        // return this.vf.getParent();
        // }
        // return new ImmutableFileFragment(this.vf.getParent());
        return this.vf.getParent();
    }

    /**
     * @return @see cross.datastructures.fragments.IVariableFragment#getRange()
     */
    @Override
    public Range[] getRange() {
        return this.vf.getRange();
    }

    /**
     * @return @see cross.datastructures.fragments.IFragment#getStats()
     */
    @Override
    public StatsMap getStats() {
        return this.vf.getStats();
    }

    /**
     * @return @see
     * cross.datastructures.fragments.IVariableFragment#getVarname()
     */
    @Override
    public String getVarname() {
        return this.vf.getVarname();
    }

    /**
     * @return @see cross.datastructures.fragments.IVariableFragment#hasArray()
     */
    @Override
    public boolean hasArray() {
        return this.vf.hasArray();
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(ucar.nc2.Attribute)
     */
    @Override
    public boolean hasAttribute(final Attribute a) {
        return this.vf.hasAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.IFragment#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(final String name) {
        return this.vf.hasAttribute(name);
    }

    /**
     * @return @see cross.datastructures.fragments.IVariableFragment#hashCode()
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
     * @see
     * cross.datastructures.fragments.IVariableFragment#setArray(ucar.ma2.Array)
     */
    @Override
    public void setArray(final Array a1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAttribute(Attribute a) {
        this.vf.addAttribute(a);
    }

    /**
     * @param a
     * @see
     * cross.datastructures.fragments.IFragment#setAttributes(ucar.nc2.Attribute[])
     */
    @Override
    public void setAttributes(final Attribute... a) {
        this.vf.setAttributes(a);
    }

    /**
     * @param dataType1
     * @see
     * cross.datastructures.fragments.IVariableFragment#setDataType(ucar.ma2.DataType)
     */
    @Override
    public void setDataType(final DataType dataType1) {
        this.vf.setDataType(dataType1);
    }

    /**
     * @param dims1
     * @see
     * cross.datastructures.fragments.IVariableFragment#setDimensions(ucar.nc2.Dimension[])
     */
    @Override
    public void setDimensions(final Dimension[] dims1) {
        this.vf.setDimensions(dims1);
    }

    /**
     * @param index1
     * @see
     * cross.datastructures.fragments.IVariableFragment#setIndex(cross.datastructures.fragments.IVariableFragment)
     */
    @Override
    public void setIndex(final IVariableFragment index1) {
        this.vf.setIndex(index1);
    }

    /**
     * @param al1
     * @see
     * cross.datastructures.fragments.IVariableFragment#setIndexedArray(java.util.ArrayList)
     */
    @Override
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
     * @see
     * cross.datastructures.fragments.IVariableFragment#setRange(ucar.ma2.Range[])
     */
    @Override
    public void setRange(final Range[] ranges1) {
        this.vf.setRange(ranges1);
    }

    /**
     * @param stats1
     * @see
     * cross.datastructures.fragments.IFragment#setStats(cross.datastructures.StatsMap)
     */
    @Override
    public void setStats(final StatsMap stats1) {
        this.vf.setStats(stats1);
    }

    /**
     * @return @see cross.datastructures.fragments.IVariableFragment#toString()
     */
    @Override
    public String toString() {
        return this.vf.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getName()
     */
    @Override
    public String getName() {
        return this.vf.getName();
    }
}
