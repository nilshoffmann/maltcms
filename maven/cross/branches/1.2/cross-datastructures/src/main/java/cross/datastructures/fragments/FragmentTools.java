/**
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
 */
/*
 * 
 *
 * $Id$
 */
package cross.datastructures.fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;


import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;

import cross.tools.ArrayTools;
import cross.tools.EvalTools;
import cross.io.FileTools;
import cross.io.IDataSourceFactory;
import cross.lookup.GlobalContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class providing methods for storing and retrieving of Arrays,
 * identified by VariableFragment objects.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
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
     * Creates or retrieves a {@link IVariableFragment} with <code>parent</code>
     * as parent and with name <code>name</code> and <code>size</code> elements.
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
     * Create a {@link IVariableFragment} as child of <code>parent</code> with
     * name <code>varname</code> and contents as given in string
     * <code>value</code>.
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
     * Create a {@link IVariableFragment} as child of <code>parent</code> with
     * name <code>varname</code> and contents as given in string collection
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
     * Create a {@link IVariableFragment} as a child of <code>parent</code>,
     * with name <code>varname</code> and index fragment <code>ifrg</code>.
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
            FragmentTools.log.debug("VariableFragment " + varname
                    + " not available as child of " + parent.getAbsolutePath());
        }
        FragmentTools.log.debug("Adding as new child!");
        final IVariableFragment vf = new VariableFragment(parent, varname, ifrg);
        return vf;
    }

    /**
     * Returns a list of variable names as defined in <code>default.vars</code>.
     * 
     * @return
     */
    public static ArrayList<String> getDefaultVars() {
        final List<?> l = Factory.getInstance().getConfiguration().getList(
                "default.vars");
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
        return FragmentTools.getIndexed(ivf.getParent(), ivf.getVarname(), ivf.
                getIndex().getVarname(), i);
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
                Factory.getInstance().getConfiguration().getString(
                "var.reference_file", "reference_file"));
        return new FileFragment(new File(s));
    }

    /**
     * Returns the right hand side of a pairwise alignment.
     * 
     * @param ff
     * @return
     */
    public static IFileFragment getRHSFile(final IFileFragment ff) {
        final String s = FragmentTools.getStringVar(ff, Factory.getInstance().
                getConfiguration().getString("var.query_file", "query_file"));
        return new FileFragment(new File(s));
    }

    /**
     * Returns all immediate source files of a {@link IFileFragment}.
     * 
     * @param ff
     * @return
     */
    public static Collection<IFileFragment> getSourceFiles(
            final IFileFragment ff) {
        final String sourceFilesVar = Factory.getInstance().getConfiguration().
                getString("var.source_files", "source_files");
        FragmentTools.log.debug("Trying to load {} for {}", sourceFilesVar,
                ff.getAbsolutePath());
        final Collection<String> c = FragmentTools.getStringArray(ff,
                sourceFilesVar);
        if (c.isEmpty()) {
            FragmentTools.log.warn("Could not find any source_files in " + ff);
            return Collections.emptyList();
        }
        final ArrayList<IFileFragment> al = new ArrayList<IFileFragment>(
                c.size());
        FragmentTools.log.info("Found the following source files:");
        for (final String s : c) {
            if (new File(s).isAbsolute()) {
                al.add(Factory.getInstance().getFileFragmentFactory().create(s));
                FragmentTools.log.info("{}", al.get(al.size() - 1).
                        getAbsolutePath());
            } else {
                try {
                    String absoluteFile = FileTools.resolveRelativeFile(
                            new File(ff.getAbsolutePath()).getParentFile(),
                            new File(s));
                    al.add(Factory.getInstance().getFileFragmentFactory().create(
                            absoluteFile));
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(
                            FragmentTools.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }

        }
        return al;
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

        vf = ff.getChild(variableName);
        FragmentTools.log.info("Retrieved VariableFragment");
        final Array a = vf.getArray();
        FragmentTools.log.info("Retrieved Array");
        EvalTools.notNull(a, FragmentTools.class);
        return ArrayTools.getStringsFromArray(a);
    }

    @Deprecated
    public static String getStringVar(final IFileFragment ff,
            final String variableName) {
        IVariableFragment vf;
        vf = ff.getChild(variableName);
        final Array a = vf.getArray();
        if (a instanceof ArrayChar.D1) {
            return ((ArrayChar.D1) a).getString();
        } else {
            FragmentTools.log.warn("Received array of type {}, expected {}", a.
                    getClass().getName(), ArrayChar.D1.class.getName());
        }
        FragmentTools.log.info("Type of received array {}",
                a.getClass().getName());
        return null;
    }

    @Deprecated
    /**
     * See {@link IFileFragment} and {@link IVariableFragment}.
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
     * Load additional variables from <code>ff</code>, using
     * <code>additional.vars</code>.
     * 
     * @param ff
     */
    public static void loadAdditionalVars(final IFileFragment ff) {
        FragmentTools.loadAdditionalVars(ff, null);
    }

    /**
     * Load additional variables from <code>ff</code>, using the given
     * <code>configKey</code> or <code>additional.vars</code> if
     * <code>configKey</code> is null.
     * 
     * @param ff
     * @param configKey
     */
    public static void loadAdditionalVars(final IFileFragment ff,
            final String configKey) {
        final List<?> l = Factory.getInstance().getConfiguration().getList(
                configKey == null ? "additional.vars" : configKey);
        final Iterator<?> iter = l.iterator();
        FragmentTools.log.debug("Trying to load additional vars for file {}",
                ff.getAbsolutePath());
        while (iter.hasNext()) {
            final String var = iter.next().toString();
            if (var.equals("*")) { // load all available Variables
                FragmentTools.log.debug("Loading all available vars!");
                try {
                    final ArrayList<IVariableFragment> al = Factory.getInstance().
                            getDataSourceFactory().getDataSourceFor(ff).
                            readStructure(ff);
                    for (final IVariableFragment vf : al) {
                        vf.getArray();
                    }
                } catch (final IOException e) {
                    FragmentTools.log.warn(e.getLocalizedMessage());
                }
            } else if (!var.equals("") && !var.trim().isEmpty()) {
                FragmentTools.log.debug("Loading var {}", var);
                try {
                    ff.getChild(var).getArray();
                    // In this case, we do not want to stop the whole app in
                    // case of an exception,
                    // so catch and log it
                } catch (final ResourceNotAvailableException e) {
                    FragmentTools.log.warn(e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Load variables from <code>ff</code>, using <code>default.vars</code>.
     * 
     * @param ff
     */
    public static void loadDefaultVars(final IFileFragment ff) {
        FragmentTools.loadDefaultVars(ff, null);
    }

    /**
     * Load variables from <code>ff</code>, using the given
     * <code>configKey</code> or <code>default.vars</code> if
     * <code>configKey</code> is null.
     * 
     * @param ff
     * @param configKey
     */
    public static void loadDefaultVars(final IFileFragment ff,
            final String configKey) {
        final List<?> l = Factory.getInstance().getConfiguration().getList(
                configKey == null ? "default.vars" : configKey);
        final Iterator<?> iter = l.iterator();
        FragmentTools.log.debug("Loading default vars for file {}",
                ff.getAbsolutePath());
        while (iter.hasNext()) {
            final String var = iter.next().toString();
            if (!var.equals("") && !var.trim().isEmpty()) {
                FragmentTools.log.debug("Loading var {}", var);
                ff.getChild(var).getArray();
            }
        }
    }

    /**
     * Set variable <code>var.reference_file</code>, left hand side (lhs) for
     * pairwise alignments.
     * 
     * @param ff
     * @param lhs
     */
    public static void setLHSFile(final IFileFragment ff,
            final IFileFragment lhs) {
        FragmentTools.createString(ff, Factory.getInstance().getConfiguration().
                getString("var.reference_file", "reference_file"),
                lhs.getAbsolutePath());
    }

    /**
     * Set variable <code>var.query_file</code>, right hand side (rhs) for
     * pairwise alignments.
     * 
     * @param ff
     * @param rhs
     */
    public static void setRHSFile(final IFileFragment ff,
            final IFileFragment rhs) {
        FragmentTools.createString(ff, Factory.getInstance().getConfiguration().
                getString("var.query_file", "query_file"),
                rhs.getAbsolutePath());
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
     * Retrieve all accessible {@link IVariableFragment} instances starting from
     * the given {@link IFileFragment} and traversing the ancestor tree in
     * breadth-first order, adding all immediate @ link IVariableFragment}
     * instances to the list first, before exploring the next ancestor
     * (source_files).
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
                Collection<String> c = ArrayTools.getStringsFromArray(sf.
                        getArray());
                for (String s : c) {
                    log.debug("Processing file {}", s);
                    File file = new File(s);
                    if (file.isAbsolute()) {
                        parentsToExplore.add(new FileFragment(file));
                    } else {
                        try {
                            file = new File(
                                    cross.tools.FileTools.resolveRelativeFile(new File(
                                    fragment.getAbsolutePath()).getParentFile(),
                                    new File(
                                    s)));
                            parentsToExplore.add(new FileFragment(file.
                                    getCanonicalFile()));
                        } catch (IOException ex) {
                            log.error("{}", ex);
                        }
                    }
                }
            } catch (ResourceNotAvailableException rnae) {
            }
            // System.out.println("Parent files " + parentsToExplore);
            try {
//				List<IVariableFragment> l = dsf.getDataSourceFor(parent)
//				        .readStructure(parent);
                for (IVariableFragment ivf : getVariablesFor(parent)) {
                    if (!ivf.getName().equals("source_files")) {
                        if (!names.containsKey(ivf.getName())) {
                            names.put(ivf.getName(), ivf);
                            allVars.add(ivf);
                        }

                    }
                }

                // allVars.addAll(l);
            } catch (IOException ex) {
                log.error("{}", ex);
            }
        }
        return allVars;
    }

    /**
     * Starting from a given IFileFragment, searches the linked source files, if any 
     * until the deepest such source file is found. Multiple IFileFragments on the same level 
     * are reported in no particular order.
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
                Collection<String> c = ArrayTools.getStringsFromArray(sf.
                        getArray());
                depth++;
                for (String s : c) {
                    log.debug("Processing file {}", s);
                    File file = new File(s);
                    IFileFragment frag = null;
                    if (file.isAbsolute()) {
                        frag = new FileFragment(file);
                        parentsToExplore.add(frag);
                    } else {
                        try {
                            file = new File(
                                    cross.tools.FileTools.resolveRelativeFile(new File(
                                    fragment.getAbsolutePath()).getParentFile(),
                                    new File(
                                    s)));
                            frag = new FileFragment(file.getCanonicalFile());
                            parentsToExplore.add(frag);
                        } catch (IOException ex) {
                            log.error("{}", ex);
                        }
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
        if (depthToFragment.containsKey(Integer.valueOf(depth))) {
            new LinkedList<IFileFragment>(depthToFragment.get(Integer.valueOf(
                    depth)));
        }
        lhs = depthToFragment.get(Integer.valueOf(maxDepth));
        return new LinkedList<IFileFragment>(lhs);
    }

    /**
     * Returns a list of the variables provided by the {@link IDataSource} for
     * the given file fragment. Any errors encountered by the DataSource are
     * thrown as an IOException.
     * 
     * @param fragment
     * @return
     * @throws IOException
     */
    public static List<IVariableFragment> getVariablesFor(IFileFragment fragment)
            throws IOException {
        IDataSourceFactory dsf = GlobalContext.getContext().getBean(IDataSourceFactory.class);
        return dsf.getDataSourceFor(fragment).readStructure(fragment);
    }
}
