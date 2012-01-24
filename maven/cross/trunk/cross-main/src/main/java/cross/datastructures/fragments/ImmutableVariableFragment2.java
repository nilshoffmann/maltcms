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
 * $Id: ImmutableVariableFragment2.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
/**
 * Created by hoffmann at 12.02.2007
 */
package cross.datastructures.fragments;

import cross.Factory;
import cross.Logging;
import cross.datastructures.StatsMap;
import cross.datastructures.tools.EvalTools;
import cross.exception.ResourceNotAvailableException;
import cross.io.misc.ArrayChunkIterator;
import cross.io.misc.Base64;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jdom.Element;
import org.slf4j.Logger;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * A class representing immutable Variables. A Variable is a meta-info container
 * for existing data stored in an array for example. VariableFragment objects
 * belong to a parent FileFragment, which corresponds to a virtual file
 * structure.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public class ImmutableVariableFragment2 implements IVariableFragment {

    private final Logger log = Logging.getLogger(this.getClass());
    private final Fragment fragment = new Fragment();
    private final String varname;
    private Dimension[] dims;
    private DataType dataType;
    private Range[] ranges;
    private String rep = null;
    private IVariableFragment index = null;
    private IFileFragment parent = null;
    private boolean isModified = false;
    private boolean useCachedList = false;

    private ImmutableVariableFragment2(final IFileFragment ff,
            final IGroupFragment group, final String varname1,
            final Dimension[] dims1, final DataType dt, final Range[] r) {
        EvalTools.notNull(varname1, "String varname was null", this);
        this.varname = varname1;
        this.ranges = r;
        this.parent = ff;
        // this.gf = (group==null)?new
        // NamedGroupFragment(this.parent,null):group;
        // if ((dims == null) && (r != null) && r[0]!=null) {
        // dims = new Dimension[1];
        // dims[0] = new Dimension("default", r[0].length(), true, false,
        // false);
        // }
        if ((dims1 != null) && (r != null)) {
            int i = 0;
            for (Dimension d : dims1) {
                if (d == null) {
                    if ((i < r.length) && (r[i] != null)) {
                        d = new Dimension("default" + i, r[i].length(), true,
                                false, false);
                    }
                } else {
                    if ((i < r.length) && (r[i] != null)) {
                        d.setLength(r[i].length());
                    }
                }
                i++;
            }
        }
        this.dims = dims1;
        this.dataType = dt == null ? DataType.DOUBLE : dt;
        toString();
        this.parent.addChildren(this);
        // this.gf.addChildren(this);
        // System.out.println("Created Info: "+this.toString());
    }

    public ImmutableVariableFragment2(final IFileFragment parent2,
            final String varname2) {
        this(parent2, varname2, null, null, null);
    }

    // private IGroupFragment gf = null;
    // private boolean protect = false;
    /**
     * Creates a VariableFragment compatible in type, name and dimensions to vf.
     * Does not copy Range or array data!
     *
     * @param ff
     * @param vf
     */
    public static IVariableFragment createCompatible(IFileFragment ff,
            IVariableFragment vf) {
        ImmutableVariableFragment2 nf = new ImmutableVariableFragment2(ff, vf.getVarname());
        vf.getParent().getChild(vf.getVarname(), true);
        Dimension[] d = vf.getDimensions();
        Dimension[] nd = new Dimension[d.length];
        int i = 0;
        for (Dimension dim : d) {
            nd[i++] = new Dimension(dim.getName(), dim);
        }
        nf.setDimensions(nd);
        nf.setDataType(vf.getDataType());
        return nf;
    }

    private ImmutableVariableFragment2(final IFileFragment ff,
            final String varname1, final Dimension[] dims1, final DataType dt,
            final Range[] ranges1) {
        this(ff, null, varname1, dims1, dt, ranges1);
    }

    public ImmutableVariableFragment2(final IFileFragment parent2,
            final String varname2, final IVariableFragment ifrg) {
        this(parent2, varname2, null, null, null);
        setIndex(ifrg);
    }

    protected void adjustConsistency(Array a) {
        setDataType(DataType.getType(a.getElementType()));
        final Dimension[] d = getDimensions();
        // if (d != null) {// adjust dimension size to that of the array
        // final int[] shape = a.getShape();
        // EvalTools.eqI(d.length, shape.length, this);// check for equal
        // // number of elements
        // int i = 0;
        // for (final Dimension dim : d) {
        // dim.setLength(shape[i]);
        // i++;
        // }
        // } else {
        if (d != null) {
            setDimensions(cross.datastructures.tools.ArrayTools.getDefaultDimensions(a));
        }
        // }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#appendXML(org.jdom.Element
     * )
     */
    @Override
    public void appendXML(final Element e) {
        this.log.debug("Appending xml for variable " + getVarname());
        final String vname = "variable";
        final Element var = new Element(vname);
        this.fragment.appendXML(var);
        var.setAttribute("name", getVarname());
        var.setAttribute("dataType", getDataType().getClassType().getName());

        final Dimension[] dims1 = getDimensions();
        if ((dims1 != null) && (dims1.length > 0)) {
            final Element dimensions = new Element("dimensions");
            getParent().addDimensions(dims1);
            for (final Dimension d : dims1) {
                final String name = d.getName();
                final Element dim = new Element("dimension");
                dim.setAttribute("refname", name);
                dimensions.addContent(dim);
            }
            var.addContent(dimensions);
        }
        if (getRange() != null) {
            final Element ranges1 = new Element("ranges");
            for (final Range r : getRange()) {
                if (r != null) {
                    final Element range = new Element("range");
                    if (r.getName() != null) {
                        range.setAttribute("name", r.getName());
                    }
                    range.setAttribute("first", "" + r.first());
                    range.setAttribute("stride", "" + r.stride());
                    range.setAttribute("last", "" + r.last());
                    ranges1.addContent(range);
                }
            }
            var.addContent(ranges1);
        }
        if (this.index != null) {
            var.setAttribute("indexVariable", this.index.getVarname());
        }
        e.addContent(var);

        final Array a = getArray();
        final Element data = new Element("data");
        final StringBuilder sb = new StringBuilder(a.getShape()[0]);
        final IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            sb.append(ii.getObjectNext() + " ");
        }
        final String b64 = Base64.encodeObject(sb.toString(), Base64.GZIP);
        data.setText(b64);
        e.addContent(data);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#compare(cross.datastructures
     * .fragments.Fragment, cross.datastructures.fragments.Fragment)
     */
    @Override
    public int compare(final IFragment o1, final IFragment o2) {
        return o1.toString().compareTo(o2.toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#compareTo(java.lang.
     * Object)
     */
    @Override
    public int compareTo(final Object o) {
        if (o instanceof ImmutableVariableFragment2) {
            final String lhs = getParent().getName() + ">" + getVarname();
            final String rhs = ((IVariableFragment) o).getParent().getName()
                    + ">" + ((IVariableFragment) o).getVarname();
            // return
            // this.toString().compareTo(((VariableFragment)o).toString());
            return lhs.compareTo(rhs);
        }
        return -1;
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IVariableFragment) {
            return this.fragment.equals(obj);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getArray()
     */
    @Override
    public Array getArray() {
        this.log.debug("Ranges of {}={}", getVarname(), Arrays.deepToString(getRange()));
        if (this.getIndex() == null) {
            try {
                final Array a = Factory.getInstance().getDataSourceFactory().getDataSourceFor(getParent()).readSingle(this);
                adjustConsistency(a);
                return a;
            } catch (final IOException io) {
                this.log.error("Could not load Array for variable {}",
                        getVarname());
                this.log.error(io.getLocalizedMessage());
            } catch (final ResourceNotAvailableException e) {
                this.log.error(e.getLocalizedMessage());
            }
            return null;
        } else {
            final List<Array> l = getIndexedArray();
            final Array a = cross.datastructures.tools.ArrayTools.glue(l);
            adjustConsistency(a);
            return a;
        }
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.Fragment#getAttribute(ucar.nc2.Attribute)
     */
    @Override
    public Attribute getAttribute(final Attribute a) {
        return this.fragment.getAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.Fragment#getAttribute(java.lang.String)
     */
    @Override
    public Attribute getAttribute(final String name) {
        return this.fragment.getAttribute(name);
    }

    /**
     * @return @see cross.datastructures.fragments.Fragment#getAttributes()
     */
    @Override
    public List<Attribute> getAttributes() {
        return this.fragment.getAttributes();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getChunkIterator()
     */
    @Override
    public ArrayChunkIterator getChunkIterator(final int chunksize) {
        return new ArrayChunkIterator(this, chunksize);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getDataType()
     */
    @Override
    public DataType getDataType() {
        return this.dataType;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getDimensions()
     */
    @Override
    public Dimension[] getDimensions() {
        return this.dims;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getIndex()
     */
    @Override
    public IVariableFragment getIndex() {
        return this.index;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getIndexedArray()
     */
    @Override
    public List<Array> getIndexedArray() {
        if (getIndex() == null) {
            return Collections.emptyList();
        } else {
            if (this.useCachedList) {
                log.error("Using cached scan line list");
                return CachedList.getList(this);
            } else {
                try {
                    final List<Array> l = Factory.getInstance().getDataSourceFactory().getDataSourceFor(
                            getParent()).readIndexed(this);
                    return l;
                } catch (final IOException e) {
                    this.log.error(e.getLocalizedMessage());
                    return Collections.emptyList();
                } catch (final ResourceNotAvailableException e) {
                    this.log.error(e.getLocalizedMessage());
                    return Collections.emptyList();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getParent()
     */
    @Override
    public IFileFragment getParent() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getRange()
     */
    @Override
    public Range[] getRange() {
        return this.ranges;
    }

    /**
     * @return @see cross.datastructures.fragments.Fragment#getStats()
     */
    @Override
    public StatsMap getStats() {
        return this.fragment.getStats();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getVarname()
     */
    @Override
    public String getVarname() {
        return this.varname;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#hasArray()
     */
    @Override
    public boolean hasArray() {
        return true;
    }

    /**
     * @param a
     * @return
     * @see
     * cross.datastructures.fragments.Fragment#hasAttribute(ucar.nc2.Attribute)
     */
    @Override
    public boolean hasAttribute(final Attribute a) {
        return this.fragment.hasAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see
     * cross.datastructures.fragments.Fragment#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(final String name) {
        return this.fragment.hasAttribute(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#hashCode()
     */
    @Override
    public int hashCode() {
        final String name = getParent().getName() + ">" + getVarname();
        final int code = (name).hashCode();
        // System.out.println("HashCode for "+name+" = "+code);
        return code;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#isModified()
     */
    @Override
    public boolean isModified() {
        return this.isModified;
    }

    public boolean isUseCachedList() {
        return this.useCachedList;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setArray(ucar.ma2.Array)
     */
    @Override
    public void setArray(final Array a1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAttribute(Attribute a) {
        this.fragment.addAttribute(a);
    }

    /**
     * @param a
     * @see
     * cross.datastructures.fragments.Fragment#setAttributes(ucar.nc2.Attribute[])
     */
    @Override
    public void setAttributes(final Attribute... a) {
        this.fragment.setAttributes(a);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setDataType(ucar.ma2
     * .DataType)
     */
    @Override
    public void setDataType(final DataType dataType1) {
        EvalTools.notNull(dataType1, this);
        this.dataType = dataType1;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setDimensions(ucar.nc2
     * .Dimension[])
     */
    @Override
    public void setDimensions(final Dimension[] dims1) {
        EvalTools.notNull(dims1, this);
        this.dims = dims1;
        getParent().addDimensions(dims1);
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.fragments.IVariableFragment#setIndex(cross.
     * datastructures.fragments.IVariableFragment)
     */
    @Override
    public void setIndex(final IVariableFragment index1) {
        if ((index1 != null) && (this.index != null)) {
            this.log.debug("Setting index from {} to {}", this.index, index1);
        }
        this.index = index1;
        // this.al = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setIndexedArray(java
     * .util.ArrayList)
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

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setRange(ucar.ma2.Range
     * [])
     */
    @Override
    public void setRange(final Range[] ranges1) {
        // EvalTools.notNull(range);
        this.ranges = ranges1;
    }

    /**
     * @param stats1
     * @see
     * cross.datastructures.fragments.Fragment#setStats(cross.datastructures.StatsMap)
     */
    @Override
    public void setStats(final StatsMap stats1) {
        this.fragment.setStats(stats1);
    }

    public void setUseCachedList(final boolean b) {
        this.useCachedList = b;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#toString()
     */
    @Override
    public String toString() {
        // if (this.rep == null) {
        final StringBuilder sb = new StringBuilder();
        // sb.append(getParent().getAbsolutePath() + ">");
        sb.append(getVarname());
        if (getRange() != null) {
            for (final Range r : getRange()) {
                if (r != null) {
                    sb.append("[" + r.first() + ":" + r.last() + ":"
                            + r.stride() + "]");
                }
            }
        }
        sb.append(getIndex() == null ? "" : "#" + getIndex().toString());
        // String range = "";
        // sb.append(range);
        this.rep = sb.toString();
        // }
        return this.rep;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getName()
     */
    @Override
    public String getName() {
        return this.varname;
    }
}
