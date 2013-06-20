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
package cross.datastructures.tools;

import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.io.IDataSourceFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.*;
import ucar.nc2.Dimension;

/**
 * Utility class providing methods for storing and retrieving of Arrays,
 * identified by VariableFragment objects.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
public class FragmentTools {

    /**
     * Creates a one-dimensional double array and adds it as to the
     * automatically created IVariableFragment with varname.
     *
     * @param parent
     * @param varname
     * @param size
     * @return
     */
    public static IVariableFragment createDoubleArrayD1(
            final IFileFragment parent, final String varname, final int size) {
        IVariableFragment vf = null;
        if (parent.hasChild(varname)) {
            vf = parent.getChild(varname);
        } else {
            vf = new VariableFragment(parent, varname);
        }
        final ArrayDouble.D1 a = new ArrayDouble.D1(size);
        vf.setArray(a);
        return vf;
    }

    /**
     * Creates an array of given shape and type as array of varname as child of
     * parent.
     *
     * @param parent
     * @param varname
     * @param type
     * @param shape
     * @return
     */
    public static IVariableFragment createArray(final IFileFragment parent,
            final String varname, final DataType type, final int... shape) {
        IVariableFragment vf = null;
        if (parent.hasChild(varname)) {
            vf = parent.getChild(varname);
        } else {
            vf = new VariableFragment(parent, varname);
        }
        final Array a = Array.factory(type, shape);
        vf.setArray(a);
        return vf;
    }

    /**
     * Creates a new Fragment with default name. Both original FileFragments
     * files are stored as variables below the newly created fragment.
     *
     * @param f1
     * @param f2
     * @return
     */
    public static IFileFragment createFragment(final IFileFragment f1,
            final IFileFragment f2, final File outputdir) {
        EvalTools.notNull(new Object[]{f1, f2}, FragmentTools.class);
        final IFileFragment ff = new FileFragment(outputdir, null);
        FragmentTools.setLHSFile(ff, f1);
        FragmentTools.setRHSFile(ff, f2);
        // ff.addSourceFile(f1,f2);
        return ff;
    }

    /**
     * Creates or retrieves a {
     *
     * @see IVariableFragment} with <code>parent</code> as parent and with name
     * <code>name</code> and <code>size</code> elements.
     *
     * @param parent
     * @param varname
     * @param size
     * @return
     */
    public static IVariableFragment createIntArrayD1(
            final IFileFragment parent, final String varname, final int size) {
        IVariableFragment vf = null;
        if (parent.hasChild(varname)) {
            vf = parent.getChild(varname);
        } else {
            vf = new VariableFragment(parent, varname);
        }
        final ArrayInt.D1 a = new ArrayInt.D1(size);
        vf.setArray(a);
        return vf;
    }

    /**
     * Create a {
     *
     * @see IVariableFragment} as child of <code>parent</code> with name
     * <code>varname</code> and contents as given in string <code>value</code>.
     *
     * @param parent
     * @param varname
     * @param value
     * @return
     */
    public static IVariableFragment createString(final IFileFragment parent,
            final String varname, final String value) {
        IVariableFragment vf = null;
        if (parent.hasChild(varname)) {
            vf = parent.getChild(varname);
        } else {
            vf = new VariableFragment(parent, varname);
        }
        final ArrayChar.D1 a = new ArrayChar.D1(value.length());
        a.setString(value);
        vf.setArray(a);
        return vf;
    }

    /**
     * Create a {
     *
     * @see IVariableFragment} as child of <code>parent</code> with name
     * <code>varname</code> and contents as given in string collection
     * <code>c</code>.
     *
     * @param parent
     * @param varname
     * @param c
     * @return
     */
    public static IVariableFragment createStringArray(
            final IFileFragment parent, final String varname,
            final Collection<String> c) {
        IVariableFragment vf = null;
        if (parent.hasChild(varname)) {
            vf = parent.getChild(varname);
        } else {
            vf = new VariableFragment(parent, varname);
        }
        Array a = Array.makeArray(DataType.STRING, new LinkedList<String>(c));
        vf.setArray(a);
        return vf;
    }

    /**
     * Create a {
     *
     * @see IVariableFragment} as a child of <code>parent</code>, with name
     * <code>varname</code> and index fragment <code>ifrg</code>.
     *
     * @param parent
     * @param varname
     * @param ifrg
     * @return
     */
    public static IVariableFragment createVariable(final IFileFragment parent,
            final String varname, final IVariableFragment ifrg) {
        EvalTools.notNull(new Object[]{parent, varname}, FragmentTools.class);
        try {
            final IVariableFragment vf = parent.getChild(varname);
            vf.setIndex(ifrg);
            return vf;
            // Catch the exception, since we are now sure, that varname
            // does not exist, so create new
        } catch (final ResourceNotAvailableException e) {
            log.debug("VariableFragment " + varname
                    + " not available as child of " + parent.getUri());
        }
        log.debug("Adding as new child!");
        final IVariableFragment vf = new VariableFragment(parent, varname, ifrg);
        return vf;
    }

    /**
     * Returns a list of variable names as defined in
     * <code>default.vars</code>.
     *
     * @return
     */
    public static ArrayList<String> getDefaultVars() {
        final List<?> l = Factory.getInstance().getConfiguration().getList("default.vars");
        final ArrayList<String> al = new ArrayList<String>();
        for (final Object o : l) {
            al.add(o.toString());
        }
        return al;
    }

    public static Array getIndexed(final IFileFragment iff, final String var,
            final String indexVar, final int i)
            throws ConstraintViolationException {
        final IVariableFragment si = iff.getChild(indexVar);
        final IVariableFragment variable = iff.getChild(var);
        variable.setIndex(si);
        return variable.getIndexedArray().get(i);
    }

    public static Array getIndexed(final IVariableFragment ivf, final int i) {
        EvalTools.notNull(
                new Object[]{ivf, ivf.getParent(), ivf.getIndex()},
                FragmentTools.class);
        return FragmentTools.getIndexed(ivf.getParent(), ivf.getName(), ivf.getIndex().getName(), i);
    }

    /**
     * Returns the left hand side of a pairwise alignment.
     *
     * @param ff
     * @return
     */
    public static IFileFragment getLHSFile(final IFileFragment ff) {
        final String s = FragmentTools.getStringVar(
                ff,
                Factory.getInstance().getConfiguration().getString("var.reference_file", "reference_file"));
        return new FileFragment(FileTools.resolveRelativeUri(ff.getUri(), URI.create(FileTools.escapeUri(s))));
    }

    /**
     * Returns the right hand side of a pairwise alignment.
     *
     * @param ff
     * @return
     */
    public static IFileFragment getRHSFile(final IFileFragment ff) {
        final String s = FragmentTools.getStringVar(ff, Factory.getInstance().getConfiguration().getString("var.query_file", "query_file"));
        return new FileFragment(FileTools.resolveRelativeUri(ff.getUri(), URI.create(FileTools.escapeUri(s))));
    }

    /**
     * Returns all immediate source files of a {
     *
     * @see IFileFragment}.
     *
     * @param ff
     * @return
     */
    public static Map<URI, IFileFragment> getSourceFiles(
            final IFileFragment ff) {
        final String sourceFilesVar = Factory.getInstance().getConfiguration().getString("var.source_files", "source_files");
        log.debug("Trying to load {} for {}", sourceFilesVar,
                ff.getUri());
        IVariableFragment tf = ff.hasChild(sourceFilesVar) ? ff.getChild(sourceFilesVar) : new VariableFragment(ff, sourceFilesVar);
        Array a = null;
        try {
            a = Factory.getInstance().getDataSourceFactory().getDataSourceFor(ff).readSingle(tf);
        } catch (ResourceNotAvailableException rnae) {
            //this is fine, it simply means, there is no source file variable available
        } catch (IOException ioex) {
            //throw new RuntimeException(ioex);
        }
        Collection<String> c = null;
        if (a != null) {
            c = ArrayTools.getStringsFromArray(a);
            if (c == null || c.isEmpty()) {
                log.warn("Could not find any source_files in " + ff);
                return Collections.emptyMap();
            }
        } else {
            log.warn("Could not retrieve source_files from " + ff);
            return Collections.emptyMap();
        }
        final Map<URI, IFileFragment> al = new LinkedHashMap<URI, IFileFragment>(
                c.size());
        log.info("Found the following source files:");
        URI baseUri = ff.getUri();
        StringBuilder uris = new StringBuilder();
        for (final String s : c) {
            log.info("Resource: {}", s);
            URI uri = URI.create(FileTools.escapeUri(s));
            IFileFragment fragment = null;
            URI resolved = FileTools.resolveRelativeUri(baseUri, uri);
//            fragment = FileFragment.getFragment(uri);                
//            if (fragment == null) {
            fragment = new FileFragment(resolved);
//            }
            log.info("Adding FileFragment for resolved relative uri from array: {}; resolved: {}", uri, fragment.getUri());
            al.put(fragment.getUri(), fragment);
            uris.append(fragment.getUri()).append("; ");

        }
        log.info("Restored source files from array: {}", uris.toString());
        return al;
    }

    /**
     * Returns true if the given parent file has no further source files.
     *
     * @param parentFile
     * @return
     */
    public static boolean isRootFile(IFileFragment parentFile) {
        try {
            Map<URI, IFileFragment> sourceFiles = FragmentTools.getSourceFiles(parentFile);
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

    /**
     * Returns true if
     * <code>childFile</code> is a direct child of
     * <code>parentFile</code>.
     *
     * @param parentFile
     * @param childFile
     * @return
     */
    public static boolean isChild(IFileFragment parentFile, IFileFragment childFile) {
        if (!parentFile.getUri().getScheme().equals(childFile.getUri().getScheme())) {
            log.warn("Trying to compare file fragments with different schemes: {} vs. {}", parentFile.getUri(), childFile.getUri());
            return false;
        }
        URI relativePath = FileTools.getRelativeUri(parentFile.getUri(), childFile.getUri());
        if (relativePath.getPath().contains("..")) {
            return false;
        }
        return true;
    }

    public static URI resolve(IFileFragment targetFile, IFileFragment baseFile) {
        boolean relativize = false;
        if (isChild(baseFile, targetFile)) {
            log.info("targetFile {} is below baseFile {}", targetFile.getUri(), baseFile.getUri());
            relativize = true;
        }
        if (isRootFile(targetFile) && !relativize) {
            log.debug("targetFile {} is a root file!", targetFile.getUri());
            return targetFile.getUri();
        }
        URI relativePath = FileTools.getRelativeUri(baseFile.getUri(), targetFile.getUri());
        log.debug("Relative path from {} to {} = {}", new Object[]{baseFile.getUri(), targetFile.getUri(), relativePath});
        EvalTools.eq(targetFile.getUri().toString(), baseFile.getUri().resolve(relativePath).toString());
        return relativePath;
    }

    public static ArrayChar.D2 createSourceFilesArray(final IFileFragment root, Collection<IFileFragment> files) {
        int ml = 128;
        List<String> names = new ArrayList<String>();
        for (final IFileFragment file : files) {
            String resolvedPath = resolve(file, root).toString();
            log.info("Adding resolved (relative) source file: {}", resolvedPath);
            names.add(resolvedPath);
            while(resolvedPath.length() > ml) {
                ml *= 2;
            }
        }
        final ArrayChar.D2 a = cross.datastructures.tools.ArrayTools.createStringArray(files.size(), ml);
        int i = 0;
        for (final String s : names) {
            log.debug("Setting source file {} on {}", s,
                    root);
            a.setString(i++, s);

        }
        return a;
    }

    /**
     * Returns a collection of strings from a char array.
     *
     * @param ff
     * @param variableName
     * @return
     */
    public static Collection<String> getStringArray(final IFileFragment ff,
            final String variableName) {
        IVariableFragment vf;
        try {
            vf = ff.getChild(variableName);
            log.info("Retrieved VariableFragment");
            final Array a = vf.getArray();
            log.info("Retrieved Array");
            EvalTools.notNull(a, FragmentTools.class);
            return ArrayTools.getStringsFromArray(a);
        } catch (ResourceNotAvailableException ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Could not retrieve variable ").append(variableName).append(" from ").append(ff.getUri());
            log.warn(sb.toString());
            return Collections.emptyList();
        }
    }

    /**
     *
     * @param ff
     * @param variableName
     * @return
     * @deprecated
     */
    @Deprecated
    public static String getStringVar(final IFileFragment ff,
            final String variableName) {
        IVariableFragment vf;
        vf = ff.getChild(variableName);
        final Array a = vf.getArray();
        if (a instanceof ArrayChar.D1) {
            return ((ArrayChar.D1) a).getString();
        } else {
            log.warn("Received array of type {}, expected {}", a.getClass().getName(), ArrayChar.D1.class.getName());
        }
        log.info("Type of received array {}", a.getClass().getName());
        return null;
    }

    /**
     *
     * @param parent
     * @param varname
     * @param indexname
     * @return
     * @throws ResourceNotAvailableException
     * @deprecated
     */
    @Deprecated
    /**
     * See {
     *
     * @see IFileFragment} and {
     * @see IVariableFragment}.
     */
    public static IVariableFragment getVariable(final IFileFragment parent,
            final String varname, final String indexname)
            throws ResourceNotAvailableException {
        EvalTools.notNull(parent, varname, indexname);
        IVariableFragment vf = null;
        IVariableFragment ifrg = null;
        vf = parent.getChild(varname);
        ifrg = parent.getChild(indexname);
        if (!vf.getIndex().equals(ifrg)) {
            vf.setIndex(ifrg);
        }
        return vf;
    }

    /**
     * Load additional variables from
     * <code>ff</code>, using
     * <code>additional.vars</code>.
     *
     * @param ff
     */
    public static void loadAdditionalVars(final IFileFragment ff) {
        FragmentTools.loadAdditionalVars(ff, null);
    }

    /**
     * Load additional variables from
     * <code>ff</code>, using the given
     * <code>configKey</code> or
     * <code>additional.vars</code> if
     * <code>configKey</code> is null.
     *
     * @param ff
     * @param configKey
     */
    public static void loadAdditionalVars(final IFileFragment ff,
            final String configKey) {
        final List<?> l = Factory.getInstance().getConfiguration().getList(configKey == null ? "additional.vars" : configKey);
        final Iterator<?> iter = l.iterator();
        log.debug("Trying to load additional vars for file {}",
                ff.getUri());
        while (iter.hasNext()) {
            final String var = iter.next().toString();
            if (var.equals("*")) { // load all available Variables
                log.debug("Loading all available vars!");
                try {
                    final ArrayList<IVariableFragment> al = Factory.getInstance().getDataSourceFactory().getDataSourceFor(ff).readStructure(ff);
                    for (final IVariableFragment vf : al) {
                        vf.getArray();
                    }
                } catch (final IOException e) {
                    log.warn(e.getLocalizedMessage());
                }
            } else if (!var.equals("") && !var.trim().isEmpty()) {
                log.debug("Loading var {}", var);
                try {
                    ff.getChild(var).getArray();
                    // In this case, we do not want to stop the whole app in
                    // case of an exception,
                    // so catch and log it
                } catch (final ResourceNotAvailableException e) {
                    log.warn(e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Load variables from
     * <code>ff</code>, using
     * <code>default.vars</code>.
     *
     * @param ff
     */
    public static void loadDefaultVars(final IFileFragment ff) {
        FragmentTools.loadDefaultVars(ff, null);
    }

    /**
     * Load variables from
     * <code>ff</code>, using the given
     * <code>configKey</code> or
     * <code>default.vars</code> if
     * <code>configKey</code> is null.
     *
     * @param ff
     * @param configKey
     */
    public static void loadDefaultVars(final IFileFragment ff,
            final String configKey) {
        final List<?> l = Factory.getInstance().getConfiguration().getList(configKey == null ? "default.vars" : configKey);
        final Iterator<?> iter = l.iterator();
        log.debug("Loading default vars for file {}",
                ff.getUri());
        while (iter.hasNext()) {
            final String var = iter.next().toString();
            if (!var.equals("") && !var.trim().isEmpty()) {
                log.debug("Loading var {}", var);
                Array a = ff.getChild(var).getArray();
                log.debug("{} ", a);
            }
        }
    }

    /**
     * Set variable
     * <code>var.reference_file</code>, left hand side (lhs) for pairwise
     * alignments.
     *
     * @param ff
     * @param lhs
     */
    public static void setLHSFile(final IFileFragment ff,
            final IFileFragment lhs) {
        FragmentTools.createString(ff, Factory.getInstance().getConfiguration().getString("var.reference_file", "reference_file"),
                FileTools.getRelativeUri(ff.getUri(), lhs.getUri()).toString());
    }

    /**
     * Set variable
     * <code>var.query_file</code>, right hand side (rhs) for pairwise
     * alignments.
     *
     * @param ff
     * @param rhs
     */
    public static void setRHSFile(final IFileFragment ff,
            final IFileFragment rhs) {
        FragmentTools.createString(ff, Factory.getInstance().getConfiguration().getString("var.query_file", "query_file"),
                FileTools.getRelativeUri(ff.getUri(), rhs.getUri()).toString());
    }

    /**
     * Returns those variables, which share <em>at least one</em> of the
     * dimensions given in the second argument.
     *
     * @param variables
     * @param dimensions
     * @return
     */
    public static List<IVariableFragment> getVariablesSharingAnyDimensions(
            List<IVariableFragment> variables, String... dimensions) {
        List<IVariableFragment> r = new ArrayList<IVariableFragment>();
        String[] dimnames = dimensions;
        Arrays.sort(dimnames);
        for (IVariableFragment ivf : variables) {
            Dimension[] dims = ivf.getDimensions();
            for (Dimension dim : dims) {
                int idx = Arrays.binarySearch(dimnames, dim.getName());
                if (idx >= 0) {
                    r.add(ivf);
                }
            }
        }
        return r;
    }

    /**
     * Returns those variables, which share <em>all</em> of the dimensions given
     * in the second argument.
     *
     * @param variables
     * @param dimensions
     * @return
     */
    public static List<IVariableFragment> getVariablesSharingAllDimensions(
            List<IVariableFragment> variables, String... dimensions) {
        List<IVariableFragment> r = new ArrayList<IVariableFragment>();
        String[] dimnames = dimensions;
        Arrays.sort(dimnames);
        for (IVariableFragment ivf : variables) {
            Dimension[] dims = ivf.getDimensions();
            if (dims.length == dimnames.length) {
                int matches = 0;
                for (Dimension dim : dims) {
                    int idx = Arrays.binarySearch(dimnames, dim.getName());
                    if (idx >= 0) {
                        matches++;
                    }
                }
                if (matches == dimnames.length) {
                    r.add(ivf);
                }
            }
        }
        return r;
    }

    /**
     * Retrieve all accessible
     *
     * @see IVariableFragment instances starting from the given
     * @see IFileFragment and traversing the ancestor tree in breadth-first
     * order, adding all immediate
     *
     * @see IVariableFragment instances to the list first, before exploring the
     * next ancestor as given by <em>source_files</em>.
     *
     * @param fragment
     * @return
     */
    public static List<IVariableFragment> getAggregatedVariables(
            IFileFragment fragment) {
        HashMap<String, IVariableFragment> names = new HashMap<String, IVariableFragment>();
        List<IVariableFragment> allVars = new ArrayList<IVariableFragment>();
        List<IFileFragment> parentsToExplore = new LinkedList<IFileFragment>();
        // System.out.println("Parent files " + parentsToExplore);
        parentsToExplore.add(fragment);
        while (!parentsToExplore.isEmpty()) {
            IFileFragment parent = parentsToExplore.remove(0);
            try {
                IVariableFragment sf = parent.getChild("source_files", true);
                Collection<String> c = ArrayTools.getStringsFromArray(sf.getArray());
                for (String s : c) {
                    log.debug("Processing file {}", s);
                    URI path = URI.create(FileTools.escapeUri(s));
                    if (path.getScheme() == null || !path.getPath().startsWith("/")) {
                        URI resolved = FileTools.resolveRelativeUri(fragment.getUri(), path);
                        log.debug("Adding resolved relative path: {} to {}", path, resolved);
                        parentsToExplore.add(new FileFragment(resolved));
                    } else {
                        log.debug("Adding absolute path: {}", path);
                        parentsToExplore.add(new FileFragment(path));
                    }

//                    File file = new File(s);
//                    if (file.isAbsolute()) {
//                        parentsToExplore.add(new FileFragment(file));
//                    } else {
//                        try {
//                            file = new File(
//                                    cross.datastructures.tools.FileTools.resolveRelativeFile(new File(
//                                    fragment.getAbsolutePath()).getParentFile(), new File(
//                                    s)));
//                            parentsToExplore.add(new FileFragment(file.getCanonicalFile()));
//                        } catch (IOException ex) {
//                            log.error("{}", ex);
//                        }
//                    }
                }
            } catch (ResourceNotAvailableException rnae) {
            }
            // System.out.println("Parent files " + parentsToExplore);
            try {
                for (IVariableFragment ivf : getVariablesFor(parent)) {
                    if (!ivf.getName().equals("source_files")) {
                        if (!names.containsKey(ivf.getName())) {
                            names.put(ivf.getName(), ivf);
                            allVars.add(ivf);
                        }
                    }
                }
            } catch (IOException ex) {
                log.warn("{}", ex);
            }
        }
        return allVars;
    }

    /**
     * Starting from a given IFileFragment, searches the linked source files, if
     * any until the deepest such source file is found. Multiple IFileFragments
     * on the same level are reported in no particular order.
     *
     * @param fragment
     * @return
     */
    public static List<IFileFragment> getDeepestAncestor(IFileFragment fragment) {
        List<IFileFragment> parentsToExplore = new LinkedList<IFileFragment>();
        // System.out.println("Parent files " + parentsToExplore);
        LinkedHashMap<Integer, LinkedHashSet<IFileFragment>> depthToFragment = new LinkedHashMap<Integer, LinkedHashSet<IFileFragment>>();
        parentsToExplore.add(fragment);
        int depth = 0;
        LinkedHashSet<IFileFragment> lhs = new LinkedHashSet<IFileFragment>();
        lhs.add(fragment);
        depthToFragment.put(Integer.valueOf(depth), lhs);
        int maxDepth = 0;
        while (!parentsToExplore.isEmpty()) {
            IFileFragment parent = parentsToExplore.remove(0);
            try {
                IVariableFragment sf = parent.getChild("source_files", true);
                Collection<String> c = ArrayTools.getStringsFromArray(sf.getArray());
                depth++;
                for (String s : c) {
                    log.debug("Processing file {}", s);
                    URI path = URI.create(FileTools.escapeUri(s));
                    IFileFragment frag = null;
                    if (path.getScheme() == null || !path.getPath().startsWith("/")) {
                        URI resolved = FileTools.resolveRelativeUri(fragment.getUri(), path);
                        log.debug("Adding resolved relative path: {} to {}", path, resolved);
                        frag = new FileFragment(resolved);
                    } else {
                        log.debug("Adding absolute path: {}", path);
                        frag = new FileFragment(path);
                    }
                    if (frag != null) {
                        Integer key = Integer.valueOf(depth);
                        if (depth > maxDepth) {
                            maxDepth = depth;
                        }
                        if (depthToFragment.containsKey(key)) {
                            depthToFragment.get(key).add(frag);
                        } else {
                            lhs = new LinkedHashSet<IFileFragment>();
                            lhs.add(frag);
                            depthToFragment.put(key, lhs);
                        }
                    }
                }
            } catch (ResourceNotAvailableException rnae) {
            }
        }
//        if (depthToFragment.containsKey(Integer.valueOf(depth))) {
//            new LinkedList<IFileFragment>(depthToFragment.get(Integer.valueOf(depth)));
//        }
        lhs = depthToFragment.get(Integer.valueOf(maxDepth));
        return new LinkedList<IFileFragment>(lhs);
    }

    /**
     * Returns a list of the variables provided by the {
     *
     * @see IDataSource} for the given file fragment. Any errors encountered by
     * the DataSource are thrown as an {
     * @see IOException}.
     *
     * @param fragment
     * @return
     * @throws IOException
     */
    public static List<IVariableFragment> getVariablesFor(IFileFragment fragment)
            throws IOException {
        IDataSourceFactory dsf = Factory.getInstance().getDataSourceFactory();
        return dsf.getDataSourceFor(fragment).readStructure(fragment);
    }

    /**
     * Returns a {
     *
     * @see TupleND} of {
     * @see ImmutableFileFragment}s from the given files.
     *
     * @param files
     * @return
     */
    public static TupleND<IFileFragment> immutable(File... files) {
        TupleND<IFileFragment> t = new TupleND<IFileFragment>();
        for (File f : files) {
            t.add(new ImmutableFileFragment(f));
        }
        return t;
    }

    /**
     * Returns a {
     *
     * @see TupleND} of {
     * @see ImmutableFileFragment}s from the given files.
     *
     * @param files
     * @return
     */
    public static TupleND<IFileFragment> immutable(Collection<File> files) {
        TupleND<IFileFragment> t = new TupleND<IFileFragment>();
        for (File f : files) {
            t.add(new ImmutableFileFragment(f));
        }
        return t;
    }

    /**
     * Returns a {
     *
     * @see TupleND} of {
     * @see FileFragment}s from the given files.
     *
     * @param files
     * @return
     */
    public static TupleND<IFileFragment> mutable(Collection<File> files) {
        TupleND<IFileFragment> t = new TupleND<IFileFragment>();
        for (File f : files) {
            t.add(new FileFragment(f));
        }
        return t;
    }
}
