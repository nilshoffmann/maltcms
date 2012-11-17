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
import cross.datastructures.cache.CacheFactory;
import cross.datastructures.cache.ICacheDelegate;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tools.FragmentTools;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

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
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class FileFragment implements IFileFragment {

    // public static final String SOURCE_FILES = "source_files";
    /**
     *
     */
    public static final Map<String, FileFragment> fileMap = new WeakHashMap<String, FileFragment>();
    /**
     *
     */
    public static final String NUMBERFORMAT = "%010d";

    public static void clearFragments() {
        FileFragment.fileMap.clear();
    }

    public static FileFragment getFragment(final File filename) {
        return getFragment(filename.getAbsoluteFile().toURI());
    }

    public static FileFragment getFragment(final URI location) {
        return FileFragment.fileMap.get(location.toASCIIString());
    }

    public static FileFragment getFragment(final String filename) {
        URI uri = URI.create(filename);
        if (uri.getScheme() == null) {
            return getFragment(new File(filename));
        }
        return getFragment(uri);
    }

    public static boolean hasFragment(final IFileFragment ff) {
        return FileFragment.hasFragment(ff.getUri().toASCIIString());
    }

    public static boolean hasFragment(final String filename) {
        return FileFragment.fileMap.containsKey(filename);
    }

    public static String printFragment(final IFileFragment ff) {
        final StringBuffer sb = new StringBuffer();
        final List<Attribute> attrs = ff.getAttributes();
        sb.append("Contents of File " + ff.getUri() + "\n");
        sb.append("Attributes:\n");
        for (final Attribute a : attrs) {
            sb.append("\t" + a.toString() + "\n");
        }
        sb.append("Variables and Groups: \n");
        synchronized (ff) {
            for (final IVariableFragment vf : ff) {
                sb.append(vf.toString() + "(DataType = " + vf.getDataType() + ")" + ":\n");
                sb.append("\tDimensions: ");
                final StringBuffer dims = new StringBuffer();
                dims.append("(");
                final Dimension[] dimA = vf.getDimensions();
                if (dimA != null) {
                    for (final Dimension d : dimA) {
                        sb.append(d.getName() + ",");
                        dims.append(d.getLength() + " x ");
                    }
                    sb.replace(sb.length() - 1, sb.length(), "");
                    dims.replace(dims.length() - 3, dims.length(), "");
                    dims.append(")");
                }
                sb.append(" " + dims + "\n");
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
    /**
     *
     */
    static long FID = 0;
    private String rep = "";
    private long fID = 0;
    private long nextGID = 0;
    private long gID = 0;
    private LinkedHashMap<String, Dimension> dims = null;
    private Map<String, IVariableFragment> children = null;
    private Set<IFileFragment> sourcefiles = new LinkedHashSet<IFileFragment>();
    private final String fileExtension = ".cdf";
    private final Fragment fragment = new Fragment();
    private ICacheDelegate<IVariableFragment, List<Array>> persistentCache = null;
    private URI u;

    /**
     * Create a FileFragment
     */
    public FileFragment() {
        this.sourcefiles = new LinkedHashSet<IFileFragment>();
        this.fID = FileFragment.FID++;
        this.children = Collections.synchronizedMap(new LinkedHashMap<String, IVariableFragment>());
        this.dims = new LinkedHashMap<String, Dimension>();
//        setFile(new File(getDefaultFilename()).toURI());
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
     * Create a FileFragment connected to URI u.
     *
     * @param u
     */
    public FileFragment(final URI u) {
        this();
        setFile(u);
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
        setFile(new File(basedir, filename).toURI());
    }

    /**
     * Sets the array cache of this FileFragment as specified if the current
     * cache has not yet been initialized (is null). Throws an
     *
     * @see IllegalStateException otherwise to prevent loss of cached data.
     *
     * @throws IllegalStateException
     */
    @Override
    public void setCache(ICacheDelegate<IVariableFragment, List<Array>> persistentCache) {
        if (this.persistentCache != null) {
            throw new IllegalStateException("Cache already initialized!");
        }
        this.persistentCache = persistentCache;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#addChildren(cross.datastructures
     * .fragments.VariableFragment)
     */
    @Override
    public synchronized void addChildren(final IVariableFragment... fragments) {
        for (final IVariableFragment vf : fragments) {
            if (this.children.containsKey(vf.getName())) {
                log.debug("VariableFragment " + vf.getName()
                        + " already known!");
                throw new IllegalArgumentException(
                        "Can not add a child more than once, call getImmediateChild() to obtain a reference!");
            }
            // else {
            // IGroupFragment gf = vf.getGroup();
            log.debug("Adding {} {} as child of {} to {}",
                    new Object[]{vf.getClass().getSimpleName(), vf.getName(),
                        vf.getParent().getUri(),
                        getUri()});
            this.children.put(vf.getName(), vf);
            if (vf.getParent().getUri().equals(getUri())) {
                log.debug("Parent FileFragment is this!");
            } else {
                log.debug("Parent FileFragment is {}", vf.getParent().getUri());
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
    @Override
    public void addDimensions(final Dimension... dims1) {
        for (final Dimension d : dims1) {
            if (!this.dims.containsKey(d.getName())) {
                this.dims.put(d.getName(), d);
            } else {
                Dimension known = this.dims.get(d.getName());
                if (known.getLength() != d.getLength()) {
                    log.warn("Replacing dimension {} with {}", known, d);
                    this.dims.remove(d.getName());
                    this.dims.put(d.getName(), d);
                }
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
    @Override
    public void addSourceFile(final Collection<IFileFragment> c) {
        if (c != null) {
            for (final IFileFragment f1 : c) {
                if (f1.getUri().equals(this.getUri())) {
                    throw new IllegalArgumentException(
                            "Cannot reference self as source file!");
                } else {
                    if (this.sourcefiles.contains(f1)) {
                        log.debug(
                                "Sourcefile {} already set, not overwriting!",
                                f1.getName());
                    } else {
                        log.debug(
                                "Adding sourcefile {} to FileFragment {}", f1.getUri(), this.getUri());
                        this.sourcefiles.add(f1);
                    }
                }
            }
            setSourceFiles(this.sourcefiles);
        }
    }

    private void setSourceFiles(final Collection<IFileFragment> files) {
        if (files.isEmpty()) {
            log.debug(
                    "setSourceFiles called for empty source files list on FileFragment {}",
                    this);
            return;
        }
        final List<IFileFragment> c = new ArrayList<IFileFragment>();
        for (IFileFragment f : files) {
            if (f != null) {
                c.add(f);
            }
        }
        int ml = 128;
        List<String> names = new ArrayList<String>();
        for (final IFileFragment file : c) {
            String resolvedPath = resolve(file, this).toASCIIString();
            log.info("Adding resolved (relative) source file: {}", resolvedPath);
            names.add(resolvedPath);
            if (resolvedPath.length() > ml) {
                ml *= 2;
            }
        }
        final ArrayChar.D2 a = cross.datastructures.tools.ArrayTools.createStringArray(c.size(), ml);
        final Dimension d1 = new Dimension("source_file_number", a.getShape()[0], true);
        final Dimension d2 = new Dimension("source_file_max_chars", a.getShape()[1], true);
        int i = 0;
        for (final String s : names) {
            log.debug("Setting source file {} on {}", s,
                    this);

            a.setString(i++, s);

        }
        final String sfvar = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
        IVariableFragment vf = null;
        if (hasChild(sfvar)) {
            log.debug("Source files exist on {}", this);
            vf = getChild(sfvar);
            vf.setArray(a);
        } else {
            log.debug("Setting new source files on {}",
                    this);
            vf = new VariableFragment(this, sfvar);
            vf.setArray(a);
        }
        vf.setDimensions(new Dimension[]{d1, d2});
    }

    private URI resolve(IFileFragment targetFile, IFileFragment baseFile) {
        if (isRootFile(targetFile)) {
            log.debug("targetFile {} is a root file!", targetFile.getUri());
            return targetFile.getUri();
        }
        URI relativePath = FileTools.getRelativeUri(baseFile.getUri(), targetFile.getUri());
        log.debug("Relative path from {} to {} = {}", new Object[]{baseFile.getUri(), targetFile.getUri(), relativePath});
        EvalTools.eq(targetFile.getUri().toASCIIString(), baseFile.getUri().resolve(relativePath).toASCIIString());
        return relativePath;
    }

    private boolean isRootFile(IFileFragment parentFile) {
        try {
            Collection<IFileFragment> sourceFiles = FragmentTools.getSourceFiles(parentFile);
            if (sourceFiles.isEmpty()) {
                log.info("File {} is a root file!", parentFile.getUri());
                return true;
            }
            log.info("File {} is NOT root file!", parentFile.getUri());
            return false;
        } catch (ResourceNotAvailableException rnae) {
            log.info("File {} is a root file!", parentFile.getUri());
            return true;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.fragments.IFileFragment#addSourceFile(cross.
     * datastructures.fragments.FileFragment)
     */
    @Override
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
    @Override
    public void appendXML(final Element e) {
        log.debug("Appending xml for fileFragment " + getName());
        final Element fileFragment = new Element("file");
        this.fragment.appendXML(fileFragment);
        final Element sourceFiles = new Element("sourceFiles");
        for (final IFileFragment frag : getSourceFiles()) {
            final Element sfile = new Element("file");
            sfile.setAttribute("filename", frag.getUri().toASCIIString());
            sourceFiles.addContent(sfile);
        }
        final Element dimensions = new Element("dimensions");
        int id = 0;
        for (final Dimension d : this.dims.values()) {
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
        fileFragment.setAttribute("filename", getUri().toASCIIString());
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
        List<IVariableFragment> toRemove = new LinkedList<IVariableFragment>();
        for (final IVariableFragment ivf : this.getImmediateChildren()) {
            if (ivf.isModified()) {
                log.warn(
                        "Can not clear arrays for {} on {}, {} was modified!",
                        new Object[]{ivf.getParent().getUri(), ivf.getName(), ivf.getClass().getSimpleName()});
            } else {
                toRemove.add(ivf);
            }
        }
        for (IVariableFragment v : toRemove) {
            log.debug("Removing child {}", v.getName());
            v.clear();
            if (persistentCache != null) {
                persistentCache.put(v, null);
            }
            removeChild(v);
        }
    }

    @Override
    public void clearDimensions() {
        this.dims.clear();
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see
     * cross.datastructures.fragments.Fragment#compare(cross.datastructures.fragments.IFragment,
     * cross.datastructures.fragments.IFragment)
     */
    @Override
    public int compare(final IFragment arg0, final IFragment arg1) {
        return this.fragment.compare(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @see cross.datastructures.fragments.Fragment#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final Object arg0) {
        return this.fragment.compareTo(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath() {
        if (this.u.getScheme() == null || this.u.getScheme().equals("file")) {
            return new File(this.u).getAbsolutePath();
        } else {
            return this.u.toASCIIString();
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

    /**
     * @return @see cross.datastructures.fragments.Fragment#getCache()
     */
    @Override
    public ICacheDelegate<IVariableFragment, List<Array>> getCache() {
        if (this.persistentCache == null) {
            String cacheLocation = UUID.nameUUIDFromBytes(getUri().toASCIIString().getBytes()).toString();
            this.persistentCache = CacheFactory.createFragmentCache(cacheLocation);
        }
        return this.persistentCache;
    }

    /**
     * @param varname
     * @see cross.datastructures.fragments.IFileFragment#getChild(String)
     */
    @Override
    public IVariableFragment getChild(final String varname)
            throws ResourceNotAvailableException {
        return getChild(varname, false);
    }

    /**
     * @param varnem
     * @param loadStructureOnly
     * @see cross.datastructures.fragments.IFileFragment#getChild(String,
     * boolean)
     */
    @Override
    public synchronized IVariableFragment getChild(final String varname,
            final boolean loadStructureOnly)
            throws ResourceNotAvailableException {
        // return child if already in memory
        if (this.children.containsKey(varname)) {
            log.debug("Found {} as direct child of {} in memory.", varname,
                    this.getUri());
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
                log.debug("Found {} as direct child of {} in file",
                        varname, this.getUri());
                return vf;
            } catch (final FileNotFoundException fnf) {
                log.info(fnf.getLocalizedMessage());
            } catch (final ResourceNotAvailableException rna) {
                log.info(rna.getLocalizedMessage());
            } catch (final IOException e) {
                log.info(e.getLocalizedMessage());
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
                log.info("Trying to load source files from file: {}", this.getUri());
                final HashSet<IFileFragment> hs = new HashSet<IFileFragment>(
                        FragmentTools.getSourceFiles(this));
                this.sourcefiles.addAll(hs);
            }
            // loop over all active source_files
            for (final IFileFragment ff : getSourceFiles()) {
                log.debug("Checking source file {} for variable {}", ff,
                        varname);
                // call getChild recursively
                // FIXME check all source files and report multiples
                try {
                    vf = ff.getChild(varname, true);
                    log.debug("Variable {} found in {}", vf.getName(),
                            ff.getUri());
                    return vf;
                } catch (final ResourceNotAvailableException iex) {
                    // throw new ResourceNotAvailableException(
                    // "Failed to find var " + varname + " in fragment "
                    // + ff.toString(), iex);
                } catch (final Exception e) {
                    log.warn("Caught an unexpected exception while looking for variable "+varname+" in source file "+ff.getUri());
                    throw new RuntimeException(e);
                }
            }
            // if all fails, throw ResourceNotAvailableException
            throw new ResourceNotAvailableException("Failed to find var "
                    + varname + " in fragment " + getName()
                    + " and source files " + this.sourcefiles);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getImmediateChildren()
     */
    @Override
    public List<IVariableFragment> getImmediateChildren() {
        return Collections.unmodifiableList(new ArrayList<IVariableFragment>(this.children.values()));
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getID()
     */
    @Override
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
    @Override
    public String getName() {
        //return this.f.getName();
        return u.getPath().substring(u.getPath().lastIndexOf("/") + 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getParent()
     */
    @Override
    public IGroupFragment getParent() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#getSize()
     */
    @Override
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
     * <code>tools.FragmentTools.getSourceFiles(IFileFragment f)</code>.
     */
    @Override
    public Collection<IFileFragment> getSourceFiles() {
        return this.sourcefiles;
    }

    /**
     * @return @see cross.datastructures.fragments.Fragment#getStats()
     */
    @Override
    public StatsMap getStats() {
        return this.fragment.getStats();
    }

    /**
     * Get the uniform resource identifier for this FileFragment.
     *
     * @return u
     */
    @Override
    public URI getUri() {
        return u;
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
    @Override
    public synchronized boolean hasChild(final IVariableFragment vf) {
        return hasChild(vf.getName());
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
    @Override
    public synchronized boolean hasChild(final String varname) {
        if (this.children.containsKey(varname)) {
            log.debug("Variable {} already contained as child of {}",
                    varname, this.getUri());
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
    @Override
    public boolean hasChildren(final IVariableFragment... vf) {
        for (final IVariableFragment frag : vf) {
            if (!hasChild(frag)) {
                log.warn("Requested variable {} not contained in {}", frag.getName(), getUri());
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
    @Override
    public boolean hasChildren(final String... s) {
        for (final String name : s) {
            if (!hasChild(name)) {
                log.warn("Requested variable {} not contained in {}",
                        name, getUri());
                return false;
            }
        }
        return true;
    }

    private String getDefaultFilename() {
        final StringBuilder sb = new StringBuilder();
        final Formatter formatter = new Formatter(sb);
        formatter.format(FileFragment.NUMBERFORMAT, (this.fID));
        return sb.toString() + this.fileExtension;
    }

    /**
     *
     * @return
     */
    @Override
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
    @Override
    public Iterator<IVariableFragment> iterator() {
        final ArrayList<IVariableFragment> al = new ArrayList<IVariableFragment>(
                this.children.size());
        if (this.children.isEmpty()) {
            try {
                Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).readStructure(this);
            } catch (IOException ex) {
                Logger.getLogger(FileFragment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
    /**
     *
     * @return
     */
    @Override
    public long nextGID() {
        final long id = this.nextGID++;
        return id;
    }

    @Override
    public void readStructure() {
        try {
            Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).readStructure(this);
        } catch (IOException ex) {
            log.warn(ex.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    /**
     *
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
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
        this.dims = new LinkedHashMap<String, Dimension>();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#removeChild(cross.datastructures
     * .fragments.IVariableFragment)
     */
    @Override
    public synchronized void removeChild(
            final IVariableFragment variableFragment) {
        if (this.children.containsKey(variableFragment.getName())) {
            log.debug("Removing child " + variableFragment.getName());
            this.children.remove(variableFragment.getName());
        } else {
            log.warn("Could not remove {}, no child of {}",
                    variableFragment.getName(), this.getUri());
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
            log.debug("Removing {} from {}", sfvar, this);
        } else {
            log.warn(
                    "Can not remove {}, no such child in {}!", sfvar, this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#save()
     */
    /**
     *
     * @return
     */
    @Override
    public boolean save() {
        if (this.u == null) {
            log.warn("URI for FileFragment was null, using default!");
            setFile(new File(getDefaultFilename()).toURI());
            log.warn("URI now set to " + getUri());
        }

        // FIXME all output currently redirected to netcdf
        final String ext = StringTools.getFileExtension(getName()).toLowerCase();
        final String filename = StringTools.removeFileExt(getName());
        final String basepath = u.getPath().substring(0, u.getPath().lastIndexOf("/") + 1);
        log.debug("extension: " + ext);
        log.debug("filename: " + filename);
        log.debug("basepath: " + basepath);
        log.debug("uri: " + u.toASCIIString());
        //FIXME this should be configured more centrally
        final String[] netcdfExts = new String[]{"nc", "nc.gz", "nc.z", "nc.zip", "nc.gzip", "nc.bz2", "cdf", "cdf.gz", "cdf.z", "cdf.zip", "cdf.gzip", "cdf.bz2"};
        log.debug("Looking for file extension: {} in {}", ext, Arrays.toString(netcdfExts));
        boolean cdfFile = false;
        for (String key : netcdfExts) {
            if (key.equals(ext)) {
                cdfFile = true;
            }
        }
        if (!cdfFile) {
            try {
                log.debug("Did not find extension!");
                URI newLocation = new URI(this.u.getScheme(), this.u.getUserInfo(), this.u.getHost(), this.u.getPort(), basepath + filename + ".cdf", this.u.getQuery(), this.u.getFragment());
                setFile(newLocation);
            } catch (URISyntaxException ex) {
                log.warn("Failed to set new location: ", ex);
                return false;
            }
        } else {
            log.debug("Found extension!");
        }
        if (Factory.getInstance().getDataSourceFactory().getDataSourceFor(this).write(this)) {
            log.debug("Save of {} succeeded, clearing arrays!", getName());
            for (IVariableFragment frag : getImmediateChildren()) {
                frag.setIsModified(false);
            }
            clearArrays();
            getCache().close();
            this.persistentCache = null;
//            FileFragment.fileMap.remove(u.toASCIIString());
            return true;
        }
        return false;
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

    @Override
    public void addAttribute(Attribute a) {
        this.fragment.addAttribute(a);
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IFileFragment#setFile(java.io.File)
     */
    @Override
    public void setFile(final File f1) {
        if (!f1.isAbsolute()) {
            log.warn("File must be absolute! Was: " + f1);
        }
        setFile(f1.toURI());
    }

    protected void setFile(final URI uri) {
        setFile(uri.toASCIIString());
    }

    protected boolean isFile(final String path) {
        File f = new File(path);
        return f.isFile();
    }

    protected boolean isURI(final String path) {
        URI uri = URI.create(path);
        if (uri.getScheme() == null || uri.getScheme().isEmpty()) {
            return false;
        }
        return uri.isAbsolute();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.fragments.IFileFragment#setFile(java.lang.String)
     */
    @Override
    public void setFile(final String file) {
        EvalTools.notNull(file, this);
//        URI u = null;
        log.info("Setting resource location to: {}", file);
//        if (isURI(file)) {
//            u = URI.create(file);
//            log.info("Resolving path of {} to {}", file, u);
//        } else {
//            File f = new File(file).getAbsoluteFile();
//            log.info("Resolving path of {} to {}", file, f.toURI().toASCIIString());
//            u = f.toURI();
//        }
        this.u = URI.create(file);
        if (this.u.getScheme() == null) {
            throw new ConstraintViolationException("URI scheme must not be null for " + this.u.toASCIIString());
        }
        if (this.u.getPath().contains("file:")) {
            throw new ConstraintViolationException("Illegal URI: scheme must not occur in path for " + this.u.toASCIIString());
        }
//        if (FileFragment.fileMap.containsKey(u.toASCIIString())) {
//            if (FileFragment.fileMap.get(u.toASCIIString()).equals(this)) {
//                log.debug("It is just me!");
//            } else {
//                throw new IllegalArgumentException("FileFragment " + u.toASCIIString() + " already contained in FileFragment.fileMap!");
//            }
//        } else {
//            if (this.u != null) {
//                log.debug("Removing binding to file {}", getUri());
//                FileFragment.fileMap.remove(getUri());
//            }
//            this.u = u;
//            log.debug("Setting file to {}", getUri());
//            FileFragment.fileMap.put(getUri(), this);
//        }
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.fragments.IGroupFragment#setID(long)
     */
    /**
     *
     * @param id
     */
    @Override
    public void setID(final long id) {
        this.gID = id;
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
     * Returns a string containing all VariableNames and Ranges.
     */
    protected String structureToString() {
        final StringBuilder sb = new StringBuilder();
        if (this.u == null) {
            sb.append(getID());
        } else {
            sb.append(this.u.toASCIIString());
        }
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
    /**
     *
     * @return
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
    /**
     *
     * @param out
     * @throws IOException
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
        out.writeObject(this.u.toASCIIString());
        out.flush();
        out.close();
    }

    /**
     * The registered dimensions of this FileFragment as an unmodifiable Set.
     *
     * @return
     */
    @Override
    public Set<Dimension> getDimensions() {
        return Collections.unmodifiableSet(new LinkedHashSet<Dimension>(this.dims.values()));
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (this.fID ^ (this.fID >>> 32));
        hash = 79 * hash + (this.fragment != null ? this.fragment.hashCode() : 0);
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
        final FileFragment other = (FileFragment) obj;
        if (this.fID != other.fID) {
            return false;
        }
        if (this.fragment != other.fragment && (this.fragment == null || !this.fragment.equals(other.fragment))) {
            return false;
        }
        return true;
    }
}
