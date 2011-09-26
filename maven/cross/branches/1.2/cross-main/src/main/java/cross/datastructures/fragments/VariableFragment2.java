/*
 * 
 *
 * $Id$
 */

package cross.datastructures.fragments;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jdom.Element;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import cross.datastructures.StatsMap;
import cross.datastructures.tools.ArrayTools;
import cross.exception.ResourceNotAvailableException;
import cross.io.misc.ArrayChunkIterator;
import cross.io.misc.Base64;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.ArrayInt;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * A class representing Variables. A Variable is a meta-info container for
 * existing data stored in an array for example. VariableFragment objects belong
 * to a parent FileFragment, which corresponds to a virtual file structure.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class VariableFragment2 implements IVariableFragment {

    private final Fragment fragment = new Fragment();
    private final String varname;
    private Dimension[] dims;
    private DataType dataType;
    private Range[] ranges;
    private String rep = null;
    private IVariableFragment index = null;
    private IFileFragment parent = null;
    private boolean useCachedList = false;
    private File tmpfile = null;

    private VariableFragment2(final IFileFragment ff,
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

    public VariableFragment2(final IFileFragment parent2, final String varname2) {
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
        VariableFragment2 nf = new VariableFragment2(ff, vf.getVarname());
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

    private VariableFragment2(final IFileFragment ff, final String varname1,
            final Dimension[] dims1, final DataType dt, final Range[] ranges1) {
        this(ff, null, varname1, dims1, dt, ranges1);
    }

    public VariableFragment2(final IFileFragment parent2, final String varname2,
            final IVariableFragment ifrg) {
        this(parent2, varname2, null, null, null);
        setIndex(ifrg);
    }

    protected void adjustConsistency(Array a) {
        if (a != null) {
            setDataType(DataType.getType(a.getElementType()));
            final Dimension[] d = getDimensions();
            if (d == null) {
                setDimensions(cross.datastructures.tools.ArrayTools.getDefaultDimensions(a));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#appendXML(org.jdom.Element
     * )
     */
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

        Array a = restore();
        if (a != null) {
            final Element data = new Element("data");
            final StringBuilder sb = new StringBuilder(
                    a.getShape()[0]);
            final IndexIterator ii = a.getIndexIterator();
            while (ii.hasNext()) {
                sb.append(ii.getObjectNext() + " ");
            }
            final String b64 = Base64.encodeObject(sb.toString(), Base64.GZIP);
            data.setText(b64);
            e.addContent(data);
        }
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
        if (o instanceof VariableFragment2) {
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
    public Array getArray() {
        this.log.debug("Ranges of {}={}", getVarname(), Arrays.deepToString(getRange()));
        return restore();
    }

    /**
     * @param a
     * @return
     * @see cross.datastructures.fragments.Fragment#getAttribute(ucar.nc2.Attribute)
     */
    public Attribute getAttribute(final Attribute a) {
        return this.fragment.getAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see cross.datastructures.fragments.Fragment#getAttribute(java.lang.String)
     */
    public Attribute getAttribute(final String name) {
        return this.fragment.getAttribute(name);
    }

    /**
     * @return
     * @see cross.datastructures.fragments.Fragment#getAttributes()
     */
    public List<Attribute> getAttributes() {
        return this.fragment.getAttributes();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getChunkIterator()
     */
    public ArrayChunkIterator getChunkIterator(final int chunksize) {
        return new ArrayChunkIterator(this, chunksize);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getDataType()
     */
    public DataType getDataType() {
        return this.dataType;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getDimensions()
     */
    public Dimension[] getDimensions() {
        return this.dims;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getIndex()
     */
    public IVariableFragment getIndex() {
        return this.index;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getIndexedArray()
     */
    public List<Array> getIndexedArray() {
        this.log.debug("{}", this.parent.toString());
        // get the associated Netcdf File
        NetcdfFile nd = null;
        List<Array> al = Collections.emptyList();
        try {
            nd = NetcdfFile.openInMemory(getTMPFile().getAbsolutePath());
            // Ensure there is an index
            EvalTools.notNull(getIndex(), this);// ,f.getIndex().getRange());
            final IVariableFragment index = getIndex();
            this.log.debug("Reading {} with index {}", getVarname(), index.getName());
            // This will be the range of Arrays in the returned ArrayList
            Range[] index_range = index.getRange();
            // Unset the range, so we can read in the full index_array at first
            index.setRange(null);

            // read in the full index_array
            log.debug("Reading index array {}", index);
            final ArrayInt.D1 index_array = (ArrayInt.D1) index.getArray();
            // how many scans are stored in the original array/ how many array
            // pointers are contained in index_array?
            final int num_arrays = index_array.getShape()[0];
            // use the default values of index_range
            int index_start = 0;
            if ((index_range != null) && (index_range[0] != null)) {
                index_start = index_range[0].first();
            }
            int index_end = index_array.getShape()[0] - 1;
            if ((index_range != null) && (index_range[0] != null)) {
                index_end = index_range[0].last();
            }
            int index_stride = 1;
            if ((index_range != null) && (index_range[0] != null)) {
                index_stride = index_range[0].stride();
            }

            this.log.debug("index_start {}, index_end {}, index_stride {}",
                    new Object[]{index_start, index_end, index_stride});

            // get information for length of compressed data
            final Variable data_var = nd.findVariable(getVarname());
            if (data_var == null) {
                nd.close();
                throw new ResourceNotAvailableException("Could not read "
                        + getVarname());
            }
            // get the (first) dimension for that (we expect 1D arrays)
            EvalTools.eqI(1, data_var.getDimensions().size(), this);
            final Dimension data_dim = data_var.getDimension(0);
            setDimensions(new Dimension[]{new Dimension(data_dim.getName(),
                        data_dim)});

            // absolute array start and end indices in data_array
            int data_start = index_array.get(index_start);
            int data_end = index_array.get(index_end);
            int data_stride = 1;

            // set stride, if exists
            if ((getRange() != null) && (getRange()[0] != null)) {
                data_stride = getRange()[0].stride();
            }

            this.log.debug("data_start {}, data_end {}, data_stride {}",
                    new Object[]{data_start, data_end, data_stride});

            // Create a new index array, which is zero based
            if (index_range == null) {
                index_range = new Range[1];
            }
            if (index_range[0] == null) {
                try {
                    index_range[0] = new Range(0, index_end - index_start,
                            index_stride);
                    this.log.debug("index_range[0] = {}", index_range[0]);
                } catch (final InvalidRangeException e) {
                    this.log.error(e.getLocalizedMessage());
                }
            }

            int index_offset = 0;

            // create the ArrayList, which will hold the individual arrays
            al = new ArrayList<Array>(num_arrays);

            // Iterate over all arrays in range, starting at relative index 0
            // this can be translated to absolute in index_array via
            // index_start+i, where index_start is the positive offset into
            // index_array
            for (int i = 0; i < index_range[0].length(); i += index_stride) {
                // first element of array index_start+i
                data_start = index_array.get((index_start + i));
                // if we have reached the last scan start contained in index_array
                // use the length of the data array -1 as absolute end of last array
                if ((i + index_start + 1) == num_arrays) {
                    data_end = data_dim.getLength() - 1;
                } else {
                    data_end = index_array.get((index_start + i + 1)) - 1;
                }
                try {
                    this.log.debug("Reading array {}, from {} to {}", new Object[]{
                                i, data_start, data_end});
                    // Read from data_start to data_end incl. with data_stride
                    final Array ai = data_var.read(data_start + ":" + data_end
                            + ":" + data_stride);
                    // Update new index to the changed value
                    // new_index.set(i, index_offset);
                    // Increase the next start offset by the length of array ai
                    // log.debug("new_index[i] = {}, Index offset =
                    // {}",i,index_offset);
                    index_offset += (data_end - data_start);
                    al.add(ai);
                } catch (IOException ex) {
                    this.log.error(ex.getLocalizedMessage());
                } catch (final InvalidRangeException e) {
                    this.log.error(e.getLocalizedMessage());
                }

            }
            index.setRange(index_range);
//            nd.close();
        } catch (IOException ioex) {
            this.log.error("Failed to open file: " + getTMPFile().getAbsolutePath() + ": " + ioex.getLocalizedMessage());
        } finally {
            if (nd != null) {
                try {
                    nd.close();
                } catch (IOException io) {
                    this.log.error("Failed to close file: " + getTMPFile().getAbsolutePath() + ": " + io.getLocalizedMessage());
                }
            }
        }
        return Collections.synchronizedList(al);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getParent()
     */
    public IFileFragment getParent() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getRange()
     */
    public Range[] getRange() {
        return this.ranges;
    }

    /**
     * @return
     * @see cross.datastructures.fragments.Fragment#getStats()
     */
    public StatsMap getStats() {
        return this.fragment.getStats();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#getVarname()
     */
    public String getVarname() {
        return this.varname;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IVariableFragment#hasArray()
     */
    public boolean hasArray() {
        return (getArray() == null) ? ((getIndexedArray() == null) ? false : true)
                : true;
    }

    /**
     * @param a
     * @return
     * @see cross.datastructures.fragments.Fragment#hasAttribute(ucar.nc2.Attribute)
     */
    public boolean hasAttribute(final Attribute a) {
        return this.fragment.hasAttribute(a);
    }

    /**
     * @param name
     * @return
     * @see cross.datastructures.fragments.Fragment#hasAttribute(java.lang.String)
     */
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
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setArray(ucar.ma2.Array)
     */
    public void setArray(final Array a1) {
        this.log.debug("Set array on VariableFragment {} as child of {}",
                toString(), getParent().getAbsolutePath());
        if (a1 == null) {
            clear();
            adjustConsistency(a1);
        } else {
            save(a1);
        }
    }

    protected void save(Array a) {
        File f = getTMPFile();
        NetcdfFileWriteable nfw = null;
        try {
            nfw = NetcdfFileWriteable.createNew(f.getAbsolutePath());
            nfw.addVariable(getName(), getDataType(), getDimensions());
            for (Attribute att : getAttributes()) {
                nfw.addVariableAttribute(getName(), att);
            }
            nfw.create();
            nfw.write(getName(), a);
        } catch (InvalidRangeException ex) {
            this.log.warn("Invalid range: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            this.log.warn("Caught exception while trying to create temporary file: " + ex.getLocalizedMessage());
        } finally {
            if (nfw != null) {
                try {
                    nfw.close();
                } catch (IOException ex) {
                    this.log.warn("Caught exception while trying to close temporary file: " + ex.getLocalizedMessage());
                }
            }
        }

    }

    protected File getTMPFile() {
        if (this.tmpfile == null) {
            File f = new File(getParent().getAbsolutePath()).getParentFile();
            tmpfile = new File(f, StringTools.removeFileExt(getParent().getName()) + "-" + getName() + ".cdf");
            tmpfile.deleteOnExit();
        }
        return this.tmpfile;
    }

    protected Array restore() {
        NetcdfFile nf = null;
        Array a = null;
        try {
            nf = NetcdfFile.openInMemory(getTMPFile().getAbsolutePath());
            clear();
            a = nf.findVariable(getName()).read();
        } catch (IOException ex) {
            this.log.warn("Caught exception while trying to open temporary file: " + ex.getLocalizedMessage());
        } finally {
            if (null != nf) {
                try {
                    nf.close();
                } catch (IOException ioex) {
                    this.log.warn("Caught exception while trying to close temporary file: " + ioex.getLocalizedMessage());
                }
            }
        }
        return a;
    }

    /**
     * @param a
     * @see cross.datastructures.fragments.Fragment#setAttributes(ucar.nc2.Attribute[])
     */
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
    public void setIndexedArray(final List<Array> al1) {
        this.log.debug(
                "Set indexed array on VariableFragment {} as child of {}",
                toString(), getParent().getAbsolutePath());
        synchronized (this) {
            setIndexedArrayInternal(al1);
        }
    }

    protected void setIndexedArrayInternal(final List<Array> al1) {
        if (al1 != null) {
            save(ArrayTools.glue(al1));
        } else {
            clear();
        }
    }

    /**
     * Allows to manually clear all associated array data.
     */
    public void clear() {
        getTMPFile().delete();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setIsModified(boolean)
     */
    @Override
    public void setIsModified(final boolean b) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IVariableFragment#setRange(ucar.ma2.Range
     * [])
     */
    public void setRange(final Range[] ranges1) {
        // EvalTools.notNull(range);
        this.ranges = ranges1;
    }

    /**
     * @param stats1
     * @see cross.datastructures.fragments.Fragment#setStats(cross.datastructures.StatsMap)
     */
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
