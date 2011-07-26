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
 * $Id: FileFragment.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package cross.datastructures.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jdom.Element;
import org.slf4j.Logger;

import ucar.ma2.ArrayChar;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.Logging;
import cross.datastructures.StatsMap;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tools.FragmentTools;
import cross.tools.StringTools;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * FileFragment can be considered the root element of a Tree of
 * VariableFragments, where each VariableFragment represents one Variable within
 * the (possibly not yet existing) File F.
 * 
 * FileFragments should be created and registered by calling
 * <i>FragmentTools.create(String filename)</i> or appropriate <i>get(...)</i>
 * methods, which create and return a new FileFragment, if not previously
 * existent, or return the Fragment matching the given filename.
 * 
 * Filenames can be local or global, since the IO-mechanism decides, whether a
 * file given by filename already exists in input or output location. Files
 * already existing in output location are overwritten by default, but this
 * behavior can be changed by setting the configuration option output.overwrite
 * to false. Then, files are created in the default location for temporary
 * files.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FileFragment implements IFileFragment {

    // public static final String SOURCE_FILES = "source_files";
    public static final Map<String, FileFragment> fileMap = new WeakHashMap<String, FileFragment>();
    public static final String NUMBERFORMAT = "%010d";

    public static void clearFragments() {
        FileFragment.fileMap.clear();
    }

    public static FileFragment getFragment(final String filename) {
        return FileFragment.fileMap.get(filename);
    }

    public static boolean hasFragment(final IFileFragment ff) {
        return FileFragment.hasFragment(ff.getAbsolutePath());
    }

    public static boolean hasFragment(final String filename) {
        return FileFragment.fileMap.containsKey(filename);
    }

    public static String printFragment(final IFileFragment ff) {
        final StringBuffer sb = new StringBuffer();
        final List<Attribute> attrs = ff.getAttributes();
        sb.append("Contents of File " + ff.getAbsolutePath() + "\n");
        sb.append("Attributes:\n");
        for (final Attribute a : attrs) {
            sb.append(a.toString() + "\n");
        }
        sb.append("Variables and Groups: \n");
        synchronized (ff) {
            for (final IVariableFragment vf : ff) {
                final StringBuffer dims = new StringBuffer();
                final Dimension[] dimA = vf.getDimensions();
                if (dimA != null) {
                    for (final Dimension d : dimA) {
                        dims.append(d + " x ");
                    }
                    dims.replace(dims.length() - 3, dims.length(), "");
                }
                sb.append(vf.toString() + "; Dimensions = " + dims
                        + " DataType = " + vf.getDataType() + "\n");
            }
        }
        return sb.toString();
    }

    public static String printFragments() {
        final StringBuilder sb = new StringBuilder();
        for (final String s : FileFragment.fileMap.keySet()) {
            final IFileFragment ff = FileFragment.fileMap.get(s);
            sb.append(ff.toString() + "\n");
        }
        return sb.toString();
    }
    public IDataSource ds = null;
    static long FID = 0;
    private final Logger log = Logging.getLogger(this.getClass());
    private File f = null;
    private String rep = "";
    private long fID = 0;
    private long nextGID = 0;
    private long gID = 0;
    private HashSet<Dimension> dims = null;
    private Map<String, IVariableFragment> children = null;
    private HashSet<IFileFragment> sourcefiles = new HashSet<IFileFragment>();
    private final String fileExtension = ".cdf";
    private String filename = "";
    private final Fragment fragment = new Fragment();

    /**
     * Create a FileFragment
     */
    public FileFragment() {
        this.sourcefiles = new LinkedHashSet<IFileFragment>();
        this.fID = FileFragment.FID++;
        this.children = Collections.synchronizedMap(new LinkedHashMap<String, IVariableFragment>());
        this.dims = new LinkedHashSet<Dimension>();
        setFile(getDefaultFilename());
    }

    /**
     * Create a FileFragment connected to File f.
     *
     * @param f
     */
    public FileFragment(final File f) {
        this();
        setFile(f);
    }

    /**
     * Create a plain FileFragment at basedir with name. If name is null, uses a
     * default filename.
     *
     * @param basedir
     * @param name
     */
    public FileFragment(final File basedir, final String name) {
        this();
        String filename = (name == null ? getDefaultFilename() : name);
        setFile(new File(basedir, filename));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#addChildren(cross.datastructures
     * .fragments.VariableFragment)
     */
    public synchronized void addChildren(final IVariableFragment... fragments) {
        for (final IVariableFragment vf : fragments) {
            if (this.children.containsKey(vf.getVarname())) {
                this.log.debug("VariableFragment " + vf.getVarname()
                        + " already known!");
                throw new IllegalArgumentException(
                        "Can not add a child more than once, call getImmediateChild() to obtain a reference!");
            }
            // else {
            // IGroupFragment gf = vf.getGroup();
            this.log.debug("Adding VariableFragment {} as child of {} to {}",
                    new Object[]{vf.getVarname(),
                        vf.getParent().getAbsolutePath(),
                        getAbsolutePath()});
            this.children.put(vf.getVarname(), vf);
            if (vf.getParent().getAbsolutePath().equals(getAbsolutePath())) {
                this.log.debug("Parent FileFragment is this!");
            } else {
                this.log.debug("Parent FileFragment is {}", vf.getParent().getAbsolutePath());
            }
            // if(!gf.hasChild(vf)) {
            // gf.addChildren(vf);
            // }
            if (vf.getDimensions() != null) {
                addDimensions(vf.getDimensions());
            }
            // }
            // put(vf.getGroup(), vf);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#addDimensions(ucar.nc2.Dimension
     * )
     */
    public void addDimensions(final Dimension... dims1) {
        for (final Dimension d : dims1) {
            if (!this.dims.contains(d)) {
                this.dims.add(d);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#addSourceFile(java.util.
     * Collection)
     */
    public void addSourceFile(final Collection<IFileFragment> c) {
        if (c != null) {
            for (final IFileFragment f1 : c) {
                if (f1.getAbsolutePath().equals(this.getAbsolutePath())) {
                    throw new IllegalArgumentException(
                            "Cannot reference self as source file!");
                } else {
                    if (this.sourcefiles.contains(f1)) {
                        this.log.debug(
                                "Sourcefile {} already set, not overwriting!",
                                f1.getName());
                    } else {
                        this.log.debug(
                                "Adding sourcefile {} to FileFragment {}", f1.getAbsolutePath(), this.getAbsolutePath());
                        this.sourcefiles.add(f1);
                    }
                }
            }

            // this.sourcefiles.addAll(c);
            setSourceFiles(this.sourcefiles);
            // FragmentTools.setSourceFiles(this,c);
        }
    }

    private void setSourceFiles(final Collection<IFileFragment> files) {
        if (files.isEmpty()) {
            Logging.getLogger(this).debug(
                    "setSourceFiles called for empty source files list on FileFragment {}",
                    this);
            return;
        }
        final List<IFileFragment> c = new ArrayList<IFileFragment>();
        c.addAll(files);
        int ml = 128;
        for (final IFileFragment f : c) {
            if (f.getAbsolutePath().length() > ml) {
                ml *= 2;
            }
        }
        final ArrayChar.D2 a = cross.datastructures.tools.ArrayTools.createStringArray(c.size(), ml);
        final Dimension d1 = new Dimension("source_file_number", c.size(), true);
        final Dimension d2 = new Dimension("source_file_max_chars", ml, true);
        int i = 0;
        for (final IFileFragment f : c) {
            //Logging.getLogger(this).info("Resolved path to source file is: " + resolveURI(f));
            Logging.getLogger(this).debug("Setting source file {} on {}", f,
                    this);
            //a.setString(i++, f.getAbsolutePath());
            
            a.setString(i++, resolve(f,this));
            
        }
        final String sfvar = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
        IVariableFragment vf = null;
        if (hasChild(sfvar)) {
            Logging.getLogger(this).debug("Source files exist on {}", this);
            vf = getChild(sfvar);
            vf.setArray(a);
        } else {
            Logging.getLogger(this).debug("Setting new source files on {}",
                    this);
            vf = new VariableFragment(this, sfvar);
            vf.setArray(a);
        }
        vf.setDimensions(new Dimension[]{d1, d2});
    }

    private String resolve(IFileFragment targetFile, IFileFragment baseFile) {
        if(isRootFile(targetFile)) {
            return targetFile.getAbsolutePath();
        }
        try {
            String relativePath = FileTools.getRelativeFile(targetFile, baseFile).getPath();
            return relativePath;
        } catch (IOException ex) {
            Logging.getLogger(this).error("Failed to resolve relative path for {}!",targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath();
        }
    }

    private boolean isRootFile(IFileFragment parentFile) {
        try{
            FragmentTools.getSourceFiles(parentFile);
            Logging.getLogger(this).info("File {} is NOT root file!",parentFile.getAbsolutePath());
            return false;
        }catch(ResourceNotAvailableException rnae) {
            Logging.getLogger(this).info("File {} is a root file!",parentFile.getAbsolutePath());
            return true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.fragments.IFileFragment#addSourceFile(cross.
     * datastructures.fragments.FileFragment)
     */
    public void addSourceFile(final IFileFragment... ff) {
        final List<IFileFragment> l = Arrays.asList(ff);
        addSourceFile(l);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#appendXML(org.jdom.Element)
     */
    public void appendXML(final Element e) {
        this.log.debug("Appending xml for fileFragment " + getName());
        final Element fileFragment = new Element("file");
        this.fragment.appendXML(fileFragment);
        final Element sourceFiles = new Element("sourceFiles");
        for (final IFileFragment frag : getSourceFiles()) {
            final Element sfile = new Element("file");
            sfile.setAttribute("filename", frag.getAbsolutePath());
            sourceFiles.addContent(sfile);
        }
        final Element dimensions = new Element("dimensions");
        int id = 0;
        for (final Dimension d : this.dims) {
            final int length = d.getLength();
            final String name = d.getName();
            final Element dim = new Element("dimension");
            dim.setAttribute("name", name);
            dim.setAttribute("length", "" + length);
            dim.setAttribute("id", "" + id++);
            dim.setAttribute("shared", "" + d.isShared());
            dim.setAttribute("unlimited", "" + d.isUnlimited());
            dim.setAttribute("variableLength", "" + d.isVariableLength());
            dimensions.addContent(dim);
        }
        fileFragment.addContent(dimensions);
        // fileFragment.setAttribute("resourceLocation", );
        fileFragment.setAttribute("filename", getAbsolutePath());
        // fileFragment.setAttribute("dirname", getDirname());
        fileFragment.setAttribute("size", "" + getSize());
        // fileFragment.setAttribute("id",""+this.fID);
        e.addContent(fileFragment);
        // for(IGroupFragment gf:this.children.values()) {
        // gf.appendXML(fileFragment);
        // }
        for (final IVariableFragment vf : this.children.values()) {
            vf.appendXML(fileFragment);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#clearArrays()
     */
    @Override
    public void clearArrays() throws IllegalStateException {
        // Collection<IFileFragment> sf = getSourceFiles();
        for (final IVariableFragment ivf : this) {
            if (ivf.isModified()) {
                this.log.debug(
                        "Can not clear arrays on {}, Variable was modified!",
                        ivf.getVarname());
            } else {
                try {
                    ivf.setArray(null);
                    ivf.setIndexedArray(null);
                } catch (final UnsupportedOperationException uoe) {
                    this.log.debug("IVariable");
                }
            }
        }
        // FragmentTools.setSourceFiles(this, sf);
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see cross.datastructures.fragments.Fragment#compare(cross.datastructures.fragments.IFragment,
     *      cross.datastructures.fragments.IFragment)
     */
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.fragment.compare(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @see cross.datastructures.fragments.Fragment#compareTo(java.lang.Object)
     */
    public int compareTo(final Object arg0) {
        return this.fragment.compareTo(arg0);
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        return this.fragment.equals(obj);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getAbsolutePath()
     */
    public String getAbsolutePath() {
        return this.f.getAbsolutePath();
    }

    /**
     * @param a
     * @return
     * @see cross.datastructures.fragments.Fragment#getAttribute(ucar.nc2.Attribute)
     */
    public Attribute getAttribute(final Attribute a) {
        return this.fragment.getAttribute(a);
    }

    // /**
    // * Set the directory name of this FileFragment
    // *
    // * @param dir
    // */
    // public void setDirname(String dir) {
    // System.out.println("Setting dirname of "+this.getFilename()+" to "+dir);
    // if (!dir.endsWith(System.getProperty("file.separator"))) {
    // dir += System.getProperty("file.separator");
    // }
    // this.dirname = dir;
    // }
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

    /**
     * @param varname
     * @see cross.datastructures.fragments.IFileFragment#getChild(String)
     */
    public IVariableFragment getChild(final String varname)
            throws ResourceNotAvailableException {
        return getChild(varname, false);
    }

    /**
     * @param varnem
     * @param loadStructureOnly
     * @see cross.datastructures.fragments.IFileFragment#getChild(String,
     *      boolean)
     */
    public synchronized IVariableFragment getChild(final String varname,
            final boolean loadStructureOnly)
            throws ResourceNotAvailableException {
        // return child if already in memory
        if (this.children.containsKey(varname)) {
            this.log.debug("Found {} as direct child of {} in memory", varname,
                    this.getAbsolutePath());
            return getImmediateChild(varname);
        } else {
            String sourceFileVarName = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");

            IVariableFragment vf = new ImmutableVariableFragment2(this, varname);
            ((ImmutableVariableFragment2) vf).setUseCachedList(Factory.getInstance().getConfiguration().getBoolean(this.getClass().getName() + ".useCachedList",
                    false));

            EvalTools.notNull(vf, this);
            try {
                if (loadStructureOnly) {
                    Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).readStructure(vf);
                } else {
                    Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).readSingle(vf);
                }
                EvalTools.notNull(vf, this);
                this.log.debug("Found {} as direct child of {} in file",
                        varname, this.getAbsolutePath());
                return vf;
            } catch (final FileNotFoundException fnf) {
                this.log.info(fnf.getLocalizedMessage());
            } catch (final ResourceNotAvailableException rna) {
                this.log.info(rna.getLocalizedMessage());
            } catch (final IOException e) {
                this.log.info(e.getLocalizedMessage());
            }
            this.children.remove(varname);
            vf = null;
            // if we are not looking for variable source_files, which must be
            // available
            // in the immediately referenced file, we need to check those
            // FileFragments
            // referenced in source_files.
            // if source_files has not been initialized yet, load it
            if (!varname.equals(sourceFileVarName)
                    && this.sourcefiles.isEmpty()) {
                this.log.info("Trying to load source files from file: {}", this);
                final HashSet<IFileFragment> hs = new HashSet<IFileFragment>(
                        FragmentTools.getSourceFiles(this));
                this.sourcefiles.addAll(hs);
            }
            // loop over all active source_files
            for (final IFileFragment ff : getSourceFiles()) {
                this.log.debug("Checking source file {} for variable {}", ff,
                        varname);
                // call getChild recursively
                try {
                    // vf = ff.getChild(varname);
                    vf = ff.getChild(varname, true);
                    this.log.debug("Variable {} found in {}", vf.getVarname(),
                            ff.getAbsolutePath());
                    // add as child
                    addChildren(vf);
                    return vf;
                } catch (final ResourceNotAvailableException iex) {
                    // throw new ResourceNotAvailableException(
                    // "Failed to find var " + varname + " in fragment "
                    // + ff.toString(), iex);
                }
            }
            // if all fails, throw ResourceNotAvailableException
            throw new ResourceNotAvailableException("Failed to find var "
                    + varname + " in fragment " + getName()
                    + " and source files.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getID()
     */
    public long getID() {
        return this.fID;
    }

    private IVariableFragment getImmediateChild(final String varname) {
        return this.children.get(varname);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getName()
     */
    public String getName() {
        return this.f.getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getParent()
     */
    public IGroupFragment getParent() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getSize()
     */
    public int getSize() {
        // int size = 0;
        // for(String key:this.children.keySet()) {
        // size+=this.children.get(key).getSize();
        // }
        return this.children.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getSourceFiles()
     */
    /**
     * Use this method with caution! It will only return the list of source
     * files, if either an array from a parent file has been loaded, or if the
     * sourcefiles have been loaded explicitly. If you want to obtain the list
     * of source files, call:
     * <code>tools.FragmentTools.getSourcefiles(IFileFragment f)</code>.
     */
    public Collection<IFileFragment> getSourceFiles() {
        return this.sourcefiles;
    }

    /**
     * @return
     * @see cross.datastructures.fragments.Fragment#getStats()
     */
    public StatsMap getStats() {
        return this.fragment.getStats();
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
     * @see
     * cross.datastructures.fragments.IGroupFragment#hasChild(cross.datastructures
     * .fragments.VariableFragment)
     */
    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChild(cross.datastructures
     * .fragments.IVariableFragment)
     */
    public synchronized boolean hasChild(final IVariableFragment vf) {
        return hasChild(vf.getVarname());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IGroupFragment#hasChild(java.lang.String)
     */
    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChild(java.lang.String)
     */
    public synchronized boolean hasChild(final String varname) {
        if (this.children.containsKey(varname)) {
            this.log.debug("Variable {} already contained as child of {}",
                    varname, this.getAbsolutePath());
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChildren(cross.datastructures
     * .fragments.IVariableFragment)
     */
    public boolean hasChildren(final IVariableFragment... vf) {
        for (final IVariableFragment frag : vf) {
            if (!hasChild(frag)) {
                this.log.warn("Requested variable {} not contained in {}", frag.getVarname(), getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#hasChildren(java.lang.String
     * )
     */
    public boolean hasChildren(final String... s) {
        for (final String name : s) {
            if (!hasChild(name)) {
                this.log.warn("Requested variable {} not contained in {}",
                        name, getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.fragment.hashCode();
    }

    // /**
    // * Initialize FileFragment with a given File f. If f is null, a default
    // file
    // * name is created, otherwise, the file's name is used.
    // *
    // */
    // private void init() {
    // if (this.f == null) {
    // final String filename = getDefaultFilename();
    // setFile(new File(filename).getAbsolutePath());
    // this.log.debug("Created FileFragment {}", this.f.getAbsoluteFile());
    // }
    //
    // }
    private String getDefaultFilename() {
        final StringBuilder sb = new StringBuilder();
        final Formatter formatter = new Formatter(sb);
        formatter.format(FileFragment.NUMBERFORMAT, (this.fID));
        return sb.toString() + this.fileExtension;
    }

    public boolean isModified() {
        for (final IVariableFragment ivf : this) {
            if (ivf.isModified()) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#iterator()
     */
    public Iterator<IVariableFragment> iterator() {
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>(
                this.children.size());
        // for(IGroupFragment vf:this.children.values()) {
        final Iterator<String> iter = this.children.keySet().iterator(); // Must
        // be in
        // block
        while (iter.hasNext()) {
            final IVariableFragment ivf = this.children.get(iter.next());
            al.add(ivf);
        }
        // }
        // return this.children.values().iterator();
        return al.iterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IGroupFragment#nextGID()
     */
    @Override
    public long nextGID() {
        final long id = this.nextGID++;
        return id;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        Object o = in.readObject();
        if (o instanceof Long) {
            this.fID = (Long) o;
        }
        o = in.readObject();
        if (o instanceof String) {
            setFile((String) o);
        }
        in.close();
        this.sourcefiles = new HashSet<IFileFragment>();
        this.children = Collections.synchronizedMap(new HashMap<String, IVariableFragment>());
        this.dims = new HashSet<Dimension>();

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#removeChild(cross.datastructures
     * .fragments.IVariableFragment)
     */
    public synchronized void removeChild(
            final IVariableFragment variableFragment) {
        if (this.children.containsKey(variableFragment.getVarname())) {
            this.children.remove(variableFragment.getVarname());
        } else {
            this.log.warn("Could not remove {}, no child of {}",
                    variableFragment.getVarname(), this.getAbsolutePath());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.fragments.IFileFragment#removeSourceFile(cross.
     * datastructures.fragments.IFileFragment)
     */
    @Override
    public void removeSourceFile(final IFileFragment ff) {
        getSourceFiles().remove(ff);
        setSourceFiles(getSourceFiles());
        if (getSourceFiles().isEmpty()) {
            removeChild(getChild("source_files"));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#removeSourceFiles()
     */
    @Override
    public void removeSourceFiles() {
        getSourceFiles().clear();
        removeSourceFilesVariableFragment();
    }

    private void removeSourceFilesVariableFragment() {
        final String sfvar = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
        if (hasChild(sfvar)) {
            final IVariableFragment ivf = getChild(sfvar);
            removeChild(ivf);
            Logging.getLogger(this).debug("Removing {} from {}", sfvar, this);
        } else {
            Logging.getLogger(this).warn(
                    "Can not remove {}, no such child in {}!", sfvar, this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#save()
     */
    public boolean save() {
        EvalTools.notNull(this.f, this);
        // FIXME all output currently redirected to netcdf
        final String ext = StringTools.getFileExtension(this.f.getName());
        final String filename = StringTools.removeFileExt(this.f.getName());
        final String path = this.f.getParent();
        if (!ext.equals("nc") && !ext.equals("cdf")) {
            final String source = this.f.getAbsolutePath();
            final File f1 = new File(path, filename + ".cdf");
            setFile(f1);
            // addSourceFile(Factory.getInstance().getFileFragmentFactory().getFragment(source));
            // addSourceFile(new FileFragment(this.f, null, null));
        }
        if (Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).write(this)) {
            clearArrays();
            return true;
        }
        return false;
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
     * @see cross.datastructures.fragments.IFileFragment#setFile(java.io.File)
     */
    public void setFile(final File f1) {
        if ((this.f != null) && f1.getAbsolutePath().equals(getAbsolutePath())) {
            // Nothing to be done
            this.log.debug("File equals current path!");
        } else {
            if ((this.f != null)
                    && FileFragment.fileMap.containsKey(getAbsolutePath())) {
                this.log.debug("Removing binding to file {}", getAbsolutePath());
                FileFragment.fileMap.remove(this.f.getAbsolutePath());
            }
            this.f = f1;
            this.filename = this.f.getName();
            this.log.debug("Setting file to {}", this.f.getAbsolutePath());
            FileFragment.fileMap.put(getAbsolutePath(), this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#setFile(java.lang.String)
     */
    public void setFile(final String file) {
        setFile(new File(file));
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IGroupFragment#setID(long)
     */
    @Override
    public void setID(final long id) {
        this.gID = id;
    }

    /**
     * @param stats1
     * @see cross.datastructures.fragments.Fragment#setStats(cross.datastructures.StatsMap)
     */
    public void setStats(final StatsMap stats1) {
        this.fragment.setStats(stats1);
    }

    /**
     * Returns a string containing all VariableNames and Ranges.
     */
    protected String structureToString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.f.getAbsolutePath());
        sb.append(">");
        int i = 0;
        // for(IGroupFragment vf:this.children.values()) {
        synchronized (this.children) {
            final Iterator<String> iter = this.children.keySet().iterator(); // Must
            // be in
            // block
            while (iter.hasNext()) {
                final IVariableFragment ivf = this.children.get(iter.next());
                final String v = ivf.toString();
                sb.append(v);

                if (i < this.children.size() - 1) {
                    sb.append("&");
                }
                i++;
            }
        }
        // }
        // Iterator<String> iter = this.children.keySet().iterator();
        // for (int i = 0; i < this.children.size(); i++) {
        // String v = this.children.get(iter.next()).toString();
        // int lim = v.indexOf(">");
        // sb.append(v.substring(lim + 1, v.length()));
        // if (i < this.children.size() - 1) {// only append, if this is
        // // not the last child
        // sb.append("&");
        // }
        // }
        this.rep = sb.toString();
        // }
        return this.rep;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#toString()
     */
    @Override
    /*
     * Returns a String representation of this Fragment, containing all it's
     * children with indices and ranges. Can directly be used as input string to
     * the command-line-interface.
     */
    public String toString() {
        return structureToString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        if (isModified()) {
            // bring memory state into sync with storage representation
            save();
        }
        // store id
        out.writeObject(Long.valueOf(this.fID));
        // store path to storage
        out.writeObject(this.f.getAbsolutePath());
        out.flush();
        out.close();
    }
}
