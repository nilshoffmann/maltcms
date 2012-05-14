/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.io.andims;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.tools.StringTools;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of {@link cross.io.IDataSource} for Netcdf files, following
 * the ANDI-MS/AIA standard.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@ServiceProvider(service = IDataSource.class)
public class NetcdfDataSource implements IDataSource {

    @Deprecated
    private static int minCachedFiles = 20;
    @Deprecated
    private static int maxCachedFiles = 40;
    @Deprecated
    private static long secondsUntilCleanup = 360;
    private final Logger log = Logging.getLogger(this.getClass());
    private final String[] fileEnding = new String[]{"nc", "cdf"};
    private boolean updateAttributes = false;
    @Deprecated
    private boolean useNetcdfFileCache = false;
    private boolean saveNCML;
    private List<String> scanDimensionVars = Collections.emptyList();
    private String scanDimensionName = "scan_number";
    private List<String> pointDimensionVars = Collections.emptyList();
    private String pointDimensionName = "point_number";

    private Dimension addDimension(final NetcdfFileWriteable nfw,
            final HashMap<String, Dimension> dimensions,
            final IVariableFragment vf, final Dimension element) {

        String dimname = element.getName();

        if (this.pointDimensionVars.contains(vf.getVarname())) {
            dimname = this.pointDimensionName;
            this.log.debug("Renaming dimension {} to {} for variable {}",
                    new Object[]{element.getName(), dimname,
                        vf.getVarname()});
        }

        if (this.scanDimensionVars.contains(vf.getVarname())) {
            dimname = this.scanDimensionName;
            this.log.debug("Renaming dimension {} to {} for variable {}",
                    new Object[]{element.getName(), dimname,
                        vf.getVarname()});
        }
        Dimension d = addDimension(dimensions, dimname, nfw, element);
        return d;
    }

    private Dimension addDimension(final HashMap<String, Dimension> dimensions, String dimname, final NetcdfFileWriteable nfw, final Dimension element) {
        Dimension d;
        if (dimensions.containsKey(dimname)) {
            this.log.debug("Dimension {} already known!", dimensions.get(dimname));
            d = dimensions.get(dimname);
            if(!d.equals(element)) {
                //TODO improve dimension mapping
                log.debug("Dimensions with identical name are not equal: {}!={}",d,element);
            }
        } else {
            d = nfw.addDimension(dimname, element.getLength(), element.isShared(), element.isUnlimited(), element.isVariableLength());
            dimensions.put(dimname, d);
        }
        return d;
    }

    @Override
    public int canRead(final IFileFragment ff) {
        final int dotindex = ff.getName().lastIndexOf(".");
        if (dotindex == -1) {
            throw new RuntimeException("Could not determine File extension of "
                    + ff);
        }
        final String fileending = ff.getName().substring(dotindex + 1);
        // System.out.println("fileending: "+fileending);
        for (final String s : this.fileEnding) {
            if (s.equalsIgnoreCase(fileending)) {
                return 1;
            }
        }
        this.log.debug("no!");
        return 0;
    }

    protected boolean closeFile(final NetcdfFile nf) {
        try {
            nf.close();
            return true;
        } catch (final IOException ex) {
            this.log.warn("Failed to close file " + nf.getLocation());
        }
        return false;
    }

    @Override
    public void configurationChanged(final ConfigurationEvent arg0) {
        // System.out.println("Configuration changed for property");
        // System.out.println(arg0.getPropertyName()+" =
        // "+arg0.getPropertyValue());
        // int type = arg0.getType();
        // String typeS = "With event type ";
        // switch(type) {
        // case AbstractConfiguration.EVENT_ADD_PROPERTY:
        // typeS+="Add Property";
        // break;
        // case AbstractConfiguration.EVENT_CLEAR_PROPERTY:
        // typeS+="Clear Property";
        // break;
        // case AbstractConfiguration.EVENT_READ_PROPERTY:
        // typeS+="Read Property";
        // break;
        // case AbstractConfiguration.EVENT_SET_PROPERTY:
        // typeS+="Set Property";
        // break;
        //			
        // }
        // System.out.println(typeS);
    }

    @Override
    public void configure(final Configuration configuration) {
        NetcdfDataSource.minCachedFiles = configuration.getInt(
                "ucar.nc2.NetcdfFileCache.minCachedFiles",
                NetcdfDataSource.minCachedFiles);
        NetcdfDataSource.maxCachedFiles = configuration.getInt(
                "ucar.nc2.NetcdfFileCache.maxCachedFiles",
                NetcdfDataSource.maxCachedFiles);
        NetcdfDataSource.secondsUntilCleanup = configuration.getLong(
                "ucar.nc2.NetcdfFileCache.secondsUntilCleanup",
                NetcdfDataSource.secondsUntilCleanup);
        this.updateAttributes = configuration.getBoolean(
                "cross.datastructures.fragments.Fragment.update.attributes",
                true);
        this.useNetcdfFileCache = configuration.getBoolean(
                "ucar.nc2.NetcdfFileCache.use", false);
        this.scanDimensionVars = StringTools.toStringList(configuration.getList(this.getClass().getName() + ".scanDimensionVars"));
        Collections.sort(this.scanDimensionVars);
        this.scanDimensionName = configuration.getString(this.getClass().getName()
                + ".scanDimensionName", "scan_number");
        this.pointDimensionVars = StringTools.toStringList(configuration.getList(this.getClass().getName() + ".pointDimensionVars"));
        Collections.sort(this.pointDimensionVars);
        this.pointDimensionName = configuration.getString(this.getClass().getName()
                + ".pointDimensionName", "point_number");
        this.saveNCML = configuration.getBoolean(
                "ucar.nc2.NetcdfFile.saveNCML", false);
    }

    public IVariableFragment convert(final IFileFragment ff, final Variable v,
            final IVariableFragment ivf) {
        final DataType dt = v.getDataType();
        final List<?> dimensions = v.getDimensions();
        final Dimension[] d = new Dimension[dimensions.size()];
        int i = 0;
        for (final Object o : dimensions) {
            if (o instanceof Dimension) {
                d[i++] = (Dimension) o;
            }
        }
        final String name = v.getName();
        final List<?> ranges = v.getRanges();

        IVariableFragment vf = null;
        if (ivf == null) {
            if (ff.hasChild(name)) {
                vf = ff.getChild(name);
            } else {
                vf = new VariableFragment(ff, name);
            }
        } else {
            vf = ivf;
        }
        updateIVariableFragment(v, dt, d, ranges, vf);
        return vf;
    }

    protected NetcdfFile get(final IVariableFragment vf)
            throws ResourceNotAvailableException {

        try {
            final NetcdfFile nf = locateFile(vf);
            if (nf == null) {
                return null;
            }
            return nf;
        } catch (final IOException e) {
            // this is only a problem, when we want to write to a file, but then
            // we create it anyway
            this.log.debug(e.getLocalizedMessage());
        }
        return null;
    }

    protected Dimension[] getDimensionArray(final NetcdfFile nf,
            final Variable v) {
        EvalTools.notNull(v, this);
        final List<?> l = v.getDimensions();
        final ArrayList<Dimension> al = new ArrayList<Dimension>();
        for (final Object o : l) {
            if (o instanceof String) {
                final Dimension d = nf.findDimension((String) o);
                al.add(d);
            } else if (o instanceof Dimension) {
                al.add((Dimension) o);
            }

        }
        return toDimensionArray(al);
    }

    protected ArrayList<Dimension> getDimensionList(final NetcdfFile nf,
            final Variable v) {
        EvalTools.notNull(v, this);
        final List<?> l = v.getDimensions();
        final ArrayList<Dimension> al = new ArrayList<Dimension>();
        for (final Object o : l) {
            if (o instanceof String) {
                final Dimension d = nf.findDimension((String) o);
                al.add(d);
            } else if (o instanceof Dimension) {
                al.add((Dimension) o);
            }

        }
        return al;
    }

    /**
     * Attributes are small metadata.
     *
     * @param ff
     * @param nf
     */
    protected void loadAttributes(final IFileFragment ff, final NetcdfFile nf) {
        final List<?> attrs = nf.getGlobalAttributes();
        if (!ff.getAttributes().isEmpty()) {
            this.log.debug(
                    "IFileFragment {} already has Attributes, updating!", ff.getAbsolutePath());
        }
        if (!attrs.isEmpty()) {
            this.log.debug("Loading Attributes for {}", ff.getAbsolutePath());
        }
        for (final Object o : attrs) {
            if (ff.hasAttribute((Attribute) o)) {
                if (this.updateAttributes) {
                    this.log.debug("Updating Attribute {}", ((Attribute) o).getName());
                    ff.setAttributes((Attribute) o);
                } else {
                    this.log.debug("Attribute {} already exists, ",
                            ((Attribute) o).getName());
                }
            } else {
                this.log.debug("Setting Attribute {}", ((Attribute) o).getName());
                ff.setAttributes((Attribute) o);
            }
        }
    }

    protected NetcdfFile locateFile(final IFileFragment ff) throws IOException {
        return NetcdfFile.open(FileTools.getFile(ff).getAbsolutePath());
    }

    protected NetcdfFile locateFile(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        NetcdfFile nf = null;
        this.log.debug("Trying to find {} in {}", f.getVarname(), f.getParent().getAbsolutePath());
        String file = "";
        try {// Prefer to find in parent of f, if it exists as a file
            file = FileTools.getFile(f.getParent()).getAbsolutePath();
            nf = NetcdfFile.open(file);
            this.log.debug("Searching for IVariableFragment {} in parent {}",
                    f, f.getParent().getAbsolutePath());
            if (nf.findVariable(f.getVarname()) != null) {
                this.log.debug("Found IVariableFragment {} in parent {}", f, f.getParent().getAbsolutePath());
                return nf;
            } else {
                nf.close();
            }
        } catch (final IOException ioex) {
            throw ioex;
        }
        // try to locate variable in source files of parent of f, first hit
        // wins
        // so be careful with equal names
        if (f.getParent().getSourceFiles().isEmpty()) {
            this.log.debug("{} has no source files!", f.getParent().getAbsolutePath());
            // try to load the source_files from file
            // FragmentTools.getSourceFiles(f.getParent());
        }
        for (final IFileFragment ff : f.getParent().getSourceFiles()) {
            try {
                file = FileTools.getFile(ff).getAbsolutePath();
                nf = NetcdfFile.open(file);
                this.log.debug(
                        "Searching for IVariableFragment {} in source file {}",
                        f, ff);
                if (nf.findVariable(f.getVarname()) != null) {
                    this.log.debug(
                            "Found IVariableFragment {} in source file {}", f,
                            ff);
                    return nf;
                } else {
                    nf.close();
                }
            } catch (final IOException ioex2) {
                throw ioex2;
            }
        }
        throw new ResourceNotAvailableException("Could not find Variable "
                + f.getVarname() + " in any associated file!");
    }

    @Override
    public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
            FileNotFoundException, ResourceNotAvailableException {
        final ArrayList<IVariableFragment> al = readStructure(f);
        final ArrayList<Array> ral = new ArrayList<Array>(al.size());
        for (final IVariableFragment vf : al) {
            final Array a = readSingle(vf);
            ral.add(a);
        }
        return ral;
    }

    @Override
    public ArrayList<Array> readIndexed(final IVariableFragment f)
            throws IOException, FileNotFoundException,
            ResourceNotAvailableException {
        this.log.debug("Reading indexed of {}, child of {}", f.toString(), f.getParent().toString());
        final ArrayList<Array> al = readIndexed2(f);
        return al;
    }

    /**
     * ReadIndexed: Read a number of (sparse) arrays written in
     * row-compressed-storage (1D-array of values >0)
     *
     * Row-Compressed-Array: DATA Index array: INDEX with length k=|INDEX| =>
     * Number of arrays
     *
     * NOTATION: DATA[FROM,TO,STRIDE], NOTE: TO is inclusive, not up to
     *
     * Require: Index containing offsets into DATA, such that INDEX[0]=DATA[0]
     * and INDEX[i]=start index of array i with 0<=i<k
     *
     * Reading an arbitrary array i: IF i+1>=k INDEX[i+1] = |DATA|-1 ELSE a[i] =
     * DATA[INDEX[i],INDEX[i+1]-1] ENDIF
     *
     *
     * If we only want to read a subsection of DATA, say j scans starting from
     * scan i, we use Require: NEW_INDEX with length j FOR a=0;a<j;a++ IF a+1 >=
     * k SCAN[a] = DATA[INDEX[a],|DATA|-1] ELSE SCAN[a] =
     * DATA[INDEX[a],INDEX[a+1]-1] ENDIF NEW_INDEX[a] = DATA[INDEX[a]]; ENDFOR
     */
    public ArrayList<Array> readIndexed2(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException,
            FileNotFoundException {
        this.log.debug("{}", f.getParent().toString());
        // get the associated Netcdf File
        final NetcdfFile nd = get(f);
        if (nd == null) {
            throw new FileNotFoundException("File "
                    + f.getParent().getAbsolutePath()
                    + " appears not to be a valid cdf file!");
        }
        // Ensure there is an index
        EvalTools.notNull(f.getIndex(), this);// ,f.getIndex().getRange());
        final IVariableFragment index = f.getIndex();
        this.log.debug("Reading {} with index {}", f.getVarname(), index.getVarname());
        // This will be the range of Arrays in the returned ArrayList
        Range[] index_range = index.getRange();
        // Unset the range, so we can read in the full index_array at first
        index.setRange(null);

        // read in the full index_array
        log.debug("Reading index array {}", index);
        final ArrayInt.D1 index_array = (ArrayInt.D1) readSingle(index);
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
        final Variable data_var = nd.findVariable(f.getVarname());
        if (data_var == null) {
            nd.close();
            throw new ResourceNotAvailableException("Could not read "
                    + f.getVarname());
        }
        // get the (first) dimension for that (we expect 1D arrays)
        EvalTools.eqI(1, data_var.getDimensions().size(), this);
        final Dimension data_dim = data_var.getDimension(0);
        f.setDimensions(new Dimension[]{new Dimension(data_dim.getName(),
                    data_dim)});

        // absolute array start and end indices in data_array
        int data_start = index_array.get(index_start);
        int data_end = index_array.get(index_end);
        int data_stride = 1;

        // set stride, if exists
        if ((f.getRange() != null) && (f.getRange()[0] != null)) {
            data_stride = f.getRange()[0].stride();
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
        final ArrayList<Array> al = new ArrayList<Array>(num_arrays);

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
            } catch (final InvalidRangeException e) {
                this.log.error(e.getLocalizedMessage());
            }

        }
        EvalTools.notNull(al, this);
        index.setRange(index_range);
        nd.close();
        return al;
    }

    @Override
    public Array readSingle(final IVariableFragment f) throws IOException,
            ResourceNotAvailableException, FileNotFoundException {
        this.log.debug("Reading single of {}, child of {}", f.toString(), f.getParent().toString());
        this.log.debug("{}", f.getParent().toString());
        final NetcdfFile nd = get(f);
        if (nd == null) {
            throw new FileNotFoundException("Could not find physical file for "
                    + f.getParent().getAbsolutePath());
        }
        EvalTools.notNull(nd, this);
        if (f.getIndex() != null) {

            this.log.debug("IVariableFragment "
                    + f.getVarname()
                    + " has IndexFragment "
                    + f.getIndex().getVarname()
                    + " set. Ignore if you didn't want to read this Variable indexed!");
        }
        final Variable v = nd.findVariable(f.getVarname());
        if (v == null) {
            throw new ResourceNotAvailableException("Could not read "
                    + f.getVarname());
        }

        f.setDataType(v.getDataType());
        // if this
        // fails, look at the commented code below!
        final List<Dimension> dims = v.getDimensions();
        log.debug("Dimensions for variable: {}:{}", f.getVarname(), dims);
        // final Iterator<Dimension> iter = dims.iterator();
        // final Dimension[] dimensions = new Dimension[dims.size()];
        // int cnt = 0;
        // while (iter.hasNext()) {
        // final Dimension dim = iter.next();
        // dimensions[cnt] = dim;
        // cnt++;
        // }
        f.setDimensions(dims.toArray(new Dimension[]{}));
        Array a = null;
        try {
            final Range[] r = f.getRange();
            if (r != null) {
                final List<Range> l = Arrays.asList(r);
                try {
                    this.log.debug("Using specified range to read partially");
                    a = v.read(l);
                    // if read with ranges is valid: keep ranges as before
                    f.setRange(r);
                } catch (final InvalidRangeException e) {
                    this.log.error(e.getLocalizedMessage());
                    a = v.read();
                    // replace ranges with valid ranges from file
                    f.setRange(v.getRanges().toArray(new Range[]{}));
                }
            } else {
                this.log.debug("No range set, reading completely");
                a = v.read();
                // update ranges to those from file
                f.setRange(v.getRanges().toArray(new Range[]{}));
            }
            nd.close();
            if (a == null) {
                this.log.debug("a is null", a);
            }
            return a;
        } catch (final IOException e) {
            this.log.error(e.getLocalizedMessage());
            throw e;
        }
    }

    @Override
    public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
            throws IOException, FileNotFoundException {
        this.log.debug("Reading structure of {}", f.toString());
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>();
        final NetcdfFile nd = locateFile(f);
        if (nd == null) {
            throw new FileNotFoundException("Could not find physical file for "
                    + f.getAbsolutePath());
        }
        loadAttributes(f, nd);
        try {
            final List<?> l = nd.getVariables();
            for (final Object o : l) {
                final Variable v = (Variable) o;
                final IVariableFragment vf = convert(f, v, null);
                final List<?> att = v.getAttributes();
                for (final Object attr : att) {
                    vf.setAttributes((ucar.nc2.Attribute) attr);
                }
                al.add(vf);
            }
            this.log.debug(f.toString());
        } finally {
            nd.close();
        }
        return al;
    }

    @Override
    public IVariableFragment readStructure(final IVariableFragment f)
            throws IOException, FileNotFoundException,
            ResourceNotAvailableException {
        this.log.debug("Reading structure of {}", f.toString());
        final NetcdfFile nd = locateFile(f);
        if (nd == null) {
            throw new FileNotFoundException("Could not find physical file for "
                    + f.getParent().getAbsolutePath());
        }
        Variable v = nd.findVariable(f.getVarname());
        if (v == null) {
            nd.close();
            throw new ResourceNotAvailableException("Could not read "
                    + f.getVarname());
        }
        final IVariableFragment vf = convert(f.getParent(), v, f);
        final List<?> att = v.getAttributes();
        for (final Object attr : att) {
            vf.setAttributes((ucar.nc2.Attribute) attr);
        }

        loadAttributes(f.getParent(), nd);
        this.log.debug(f.toString());
        nd.close();
        return f;
    }

    protected NetcdfFileWriteable structureWrite(final IFileFragment parent) {
        try {
            File f = null;
            if (parent.getSize() == 0) {
                this.log.warn("IFileFragment {} has no children!", parent.getAbsolutePath());
                return null;
            }
            f = FileTools.prepareOutput(parent);
            final String filename = f.getAbsolutePath();
            this.log.debug("Trying to create NetcdfFileWritable {}", filename);
            final NetcdfFileWriteable nfw = NetcdfFileWriteable.createNew(
                    filename, false);

            final List<Attribute> globalAttrs = parent.getAttributes();
            for (final Attribute attr : globalAttrs) {
                this.log.debug("Adding attribute {} to file {}", attr, parent.getAbsolutePath());
                nfw.addGlobalAttribute(attr);
            }

            final HashMap<String, Dimension> dimensions = new HashMap<String, Dimension>();
            for (Dimension dim : parent.getDimensions()) {
                addDimension(dimensions, dim.getName(), nfw, dim);
            }
            boolean skipVarForMissingData = false;
            for (final IVariableFragment vf : parent) {
                if (vf.getVarname().equals("scan_index")) {
                    this.log.debug("{}", vf.getArray());
                }
                if (vf.getParent().getAbsolutePath().equals(
                        parent.getAbsolutePath())) {
                    // HANDLE DIMENSIONS, SHOULD BE DEFINED GLOBALLY
                    Dimension[] dim = null;
                    if (vf.getDimensions() == null) {
                        if (!vf.hasArray()) {
                            this.log.warn(
                                    "IVariableFragment {} has no array set, skipping!",
                                    vf);
                            skipVarForMissingData = true;
                        } else {
                            this.log.warn(
                                    "IVariableFragment {} has no Dimension info, adding defaults!",
                                    vf.getVarname());
                            dim = cross.datastructures.tools.ArrayTools.getDefaultDimensions(vf.getArray());
                            final Dimension[] dimC = new Dimension[dim.length];
                            int i = 0;
                            for (final Dimension element : dim) {
                                this.log.debug("Checking Dimension {}", element);
                                dimC[i++] = addDimension(nfw, dimensions, vf,
                                        element);
                            }
                            dim = dimC;
                        }
                    } else {
                        this.log.debug("Using Dimensions given by IVariableFragment!");

                        dim = vf.getDimensions();
                        final Dimension[] dimC = new Dimension[dim.length];
                        int i = 0;
                        for (final Dimension element : dim) {
                            this.log.debug("Checking Dimension {}", element);
                            dimC[i++] = addDimension(nfw, dimensions, vf,
                                    element);
                        }
                        dim = dimC;
                    }
                    if (!skipVarForMissingData) {
                        // HANDLE GROUPS
                        final Group rootGroup = nfw.getRootGroup();
                        // TODO Group handling loses data!!!
                        // IGroupFragment parentGroup =
                        // t.getFirst().getGroup().getParent();
                        // IGroupFragment group = t.getFirst().getGroup();
                        final Group variableGroup = rootGroup;
                        // if(parentGroup==null) {
                        // variableGroup = new
                        // Group(nfw,rootGroup,group.getName());
                        // nfw.addGroup(rootGroup, variableGroup);
                        // } else {
                        // Stack<IGroupFragment> s = new
                        // Stack<IGroupFragment>();
                        // IGroupFragment gf = group;
                        // log.debug("Processing group {}",group.getName());
                        // while(true) {
                        // s.push(parentGroup);
                        // log.debug("Current group {}",gf);
                        // parentGroup = gf.getParent();
                        // if(parentGroup==null) {
                        // break;
                        // }
                        // log.debug("Parent group {}",parentGroup.getName());
                        // gf = parentGroup;
                        // }
                        // while(!s.isEmpty()) {
                        // gf = s.pop();
                        // variableGroup = new
                        // Group(nfw,rootGroup,gf.getName());
                        // rootGroup = variableGroup;
                        // }
                        // }

                        // CREATE VARIABLE
                        final Variable v = new Variable(nfw, variableGroup,
                                null, vf.getVarname());
                        nfw.addVariable(variableGroup, v);

                        // SET DIMENSIONS
                        final List<Dimension> l = Arrays.asList(dim);
                        v.setDimensions(l);
                        vf.setDimensions(dim);

                        // SET DATA TYPE
                        final DataType dt = vf.getDataType() == null ? DataType.DOUBLE
                                : vf.getDataType();
                        v.setDataType(dt);

                        // SET ATTRIBUTES
                        final List<Attribute> attrs = vf.getAttributes();
                        for (final Attribute att : attrs) {
                            v.addAttribute(att);
                        }
                        this.log.debug("Adding variable {}", v.toString());
                    }
                    skipVarForMissingData = false;
                }
            }
            this.log.debug("Dimensions: {}", Arrays.deepToString(dimensions.values().toArray(new Dimension[]{})));
            nfw.create();
            return nfw;
        } catch (final IOException e) {
            this.log.error(e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public List<String> supportedFormats() {
        return Arrays.asList(this.fileEnding);
    }

    protected Dimension[] toDimensionArray(final ArrayList<Dimension> al) {
        Dimension[] dim = new Dimension[al.size()];
        dim = al.toArray(dim);
        return dim;
    }

    private void updateIVariableFragment(final Variable v, final DataType dt,
            final Dimension[] d, final List<?> ranges,
            final IVariableFragment vf) {
        vf.setDimensions(d);
        vf.setDataType(dt);
        final Range[] r = new Range[ranges.size()];
        final int i = 0;
        for (final Object o : ranges) {
            r[i] = (Range) o;
        }
        vf.setRange(r);
        final List<?> attrs = v.getAttributes();
        final ucar.nc2.Attribute[] attributes = new ucar.nc2.Attribute[attrs.size()];
        int cnt = 0;
        for (final Object o : attributes) {
            attributes[cnt++] = (ucar.nc2.Attribute) o;
        }
        vf.setAttributes(attributes);
    }

    @Override
    public boolean write(final IFileFragment f) {
        this.log.debug("{}", f.toString());
        this.log.debug("Saving {} with NetcdfDataSource", f.getAbsolutePath());
        EvalTools.notNull(f, this);
        final NetcdfFileWriteable nfw = structureWrite(f);
        EvalTools.notNull(nfw, this);
        int cnt = 0;
        String varname = "";
        for (final IVariableFragment vf : f) {
            if (vf.getParent().getAbsolutePath().equals(f.getAbsolutePath())) {
                try {
                    varname = vf.getVarname();
                    EvalTools.notNull(varname, this);
                    if (vf.hasArray()) {
                        final Variable v = nfw.findVariable(varname);
                        if (v != null) {
                            this.log.debug("Saving variable "
                                    + v.getNameAndDimensions());
                            nfw.write(varname, vf.getArray());
                            nfw.flush();
                        }
                    }
                    vf.setArray(null);
                    vf.setIndexedArray(null);
                } catch (final IOException e) {
                    log.error("IOException while writing variable '{}'",varname);
                    e.printStackTrace();
                } catch (final InvalidRangeException e) {
                    log.warn("InvalidRangeException writing variable '{}' with dimensions '{}'",varname,Arrays.toString(vf.getDimensions()));
                    e.printStackTrace();
                }
                cnt++;
            }
        }
        nfw.finish();
        this.log.info("Wrote " + cnt + " records to " + nfw.getLocation());
        if (this.saveNCML) {
            final String ncmlFile = StringTools.removeFileExt(nfw.getLocation())
                    + ".ncml";
            try {
                nfw.writeNcML(new BufferedOutputStream(new FileOutputStream(
                        ncmlFile)), null);
            } catch (final FileNotFoundException e1) {
                this.log.error(e1.getLocalizedMessage());
            } catch (final IOException e1) {
                this.log.error(e1.getLocalizedMessage());
            }
        }
        f.setFile(nfw.getLocation());
        try {
            nfw.flush();
            nfw.close();
            return true;
        } catch (final IOException e) {
            this.log.error(e.getLocalizedMessage());
            return false;
        }

    }
}
