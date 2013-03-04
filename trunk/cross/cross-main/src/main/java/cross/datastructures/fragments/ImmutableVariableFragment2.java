/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.fragments;

import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.tools.EvalTools;
import cross.exception.ResourceNotAvailableException;
import cross.io.misc.ArrayChunkIterator;
import cross.io.misc.Base64;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
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
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class ImmutableVariableFragment2 implements IVariableFragment {

    public static boolean useCachedIndexedAccess = false;
    
    private final Fragment fragment = new Fragment();
    private final String varname;
    private Dimension[] dims;
    private DataType dataType;
    private Range[] ranges;
    private String rep = null;
    private IVariableFragment index = null;
    private IFileFragment parent = null;
    private boolean isModified = false;
    private boolean useCachedList = useCachedIndexedAccess;

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

    /**
     *
     * @param parent2
     * @param varname2
     */
    public ImmutableVariableFragment2(final IFileFragment parent2,
            final String varname2) {
        this(parent2, varname2, null, null, null);
    }
    
    /**
     * Creates a VariableFragment compatible in type, name and dimensions to vf.
     * Does not copy Range or array data!
     *
     * @param ff
     * @param vf
     */
    public static IVariableFragment createCompatible(IFileFragment ff,
            IVariableFragment vf) {
        ImmutableVariableFragment2 nf = new ImmutableVariableFragment2(ff, vf.getName());
        Dimension[] d = vf.getDimensions();
        Dimension[] nd = new Dimension[d.length];
        int i = 0;
        for (Dimension dim : d) {
            nd[i++] = new Dimension(dim.getName(), dim);
        }
        nf.dims = nd;
        nf.dataType = vf.getDataType();
        return nf;
    }

    private ImmutableVariableFragment2(final IFileFragment ff,
            final String varname1, final Dimension[] dims1, final DataType dt,
            final Range[] ranges1) {
        this(ff, null, varname1, dims1, dt, ranges1);
    }

    /**
     *
     * @param parent2
     * @param varname2
     * @param ifrg
     */
    public ImmutableVariableFragment2(final IFileFragment parent2,
            final String varname2, final IVariableFragment ifrg) {
        this(parent2, varname2, null, null, null);
        setIndex(ifrg);
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
        log.debug("Appending xml for variable " + getName());
        final String vname = "variable";
        final Element var = new Element(vname);
        this.fragment.appendXML(var);
        var.setAttribute("name", getName());
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
            var.setAttribute("indexVariable", this.index.getName());
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

    @Override
    public void clear() {
//        this.parent = null;
//        this.volatileCache.put(this, null);
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
     * @see
     * cross.datastructures.fragments.IVariableFragment#compareTo(java.lang.
     * Object)
     */
    @Override
    public int compareTo(final Object o) {
        if (o instanceof ImmutableVariableFragment2) {
            final String lhs = getParent().getName() + ">" + getName();
            final String rhs = ((IVariableFragment) o).getParent().getName()
                    + ">" + ((IVariableFragment) o).getName();
            return lhs.compareTo(rhs);
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getArray()
     */
    @Override
    public Array getArray() {
        log.debug("Ranges of {}={}", getName(), Arrays.deepToString(getRange()));
        if (this.getIndex() == null) {
            try {
                return Factory.getInstance().getDataSourceFactory().getDataSourceFor(
                        getParent()).readSingle(this);
            } catch (IOException ex) {
                log.warn("", ex);
            } catch (ResourceNotAvailableException ex) {
                log.warn("", ex);
            }
            return null;
        } else {
            try {
                final List<Array> l = getIndexedArray();
                if (l == null || l.isEmpty()) {
                    return null;
                }
                log.warn("Requested single array from indexed variable: will glue indexed arrays!");
                final Array a = cross.datastructures.tools.ArrayTools.glue(l);
                return a;
            } catch (final ResourceNotAvailableException e) {
                log.error(e.getLocalizedMessage());
            }
            return null;
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
    /**
     *
     * @param chunksize
     * @return
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
                log.info("Using cached list");
                return CachedList.getList(this);
            } else {
                try {
                    final List<Array> l = Factory.getInstance().getDataSourceFactory().getDataSourceFor(
                            getParent()).readIndexed(this);
                    return l;
                } catch (final IOException e) {
                    throw new RuntimeException(e);
//                    log.error(e.getLocalizedMessage());
//                    return Collections.emptyList();
                } catch (final ResourceNotAvailableException e) {
//                    log.error(e.getLocalizedMessage());
//                    return Collections.emptyList();
                    throw new RuntimeException(e);
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

    /**
     * This method will always return true!
     *
     * @return
     */
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
     * @see cross.datastructures.fragments.IVariableFragment#isModified()
     */
    @Override
    public boolean isModified() {
        return this.isModified;
    }

    /**
     *
     * @return
     */
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
            log.debug("Setting index from {} to {}", this.index, index1);
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

    /**
     *
     * @param b
     */
    public void setUseCachedList(final boolean b) {
        this.useCachedList = b;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#toString()
     */
    /**
     *
     * @return
     */
    @Override
    public String toString() {
        // if (this.rep == null) {
        final StringBuilder sb = new StringBuilder();
        // sb.append(getParent().getAbsolutePath() + ">");
        sb.append(getName());
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

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.fragment != null ? this.fragment.hashCode() : 0);
        hash = 41 * hash + (this.varname != null ? this.varname.hashCode() : 0);
        hash = 41 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        return hash;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImmutableVariableFragment2 other = (ImmutableVariableFragment2) obj;
        if (this.fragment != other.fragment && (this.fragment == null || !this.fragment.equals(other.fragment))) {
            return false;
        }
        if ((this.varname == null) ? (other.varname != null) : !this.varname.equals(other.varname)) {
            return false;
        }
        if (this.parent != other.parent && (this.parent == null || !this.parent.equals(other.parent))) {
            return false;
        }
        return true;
    }
}
