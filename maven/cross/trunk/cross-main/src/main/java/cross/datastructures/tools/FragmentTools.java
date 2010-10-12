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
 * $Id: FragmentTools.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */

package cross.datastructures.tools;

import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.ArrayTools;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;

/**
 * Utility class providing methods for storing and retrieving of Arrays,
 * identified by VariableFragment objects.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FragmentTools {

	static final Logger log = Logging.getLogger(FragmentTools.class);

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
	 * Creates a new Fragment with default name. Both original FileFragments
	 * files are stored as variables below the newly created fragment.
	 * 
	 * @param f1
	 * @param f2
	 * @return
	 */

	public static IFileFragment createFragment(final IFileFragment f1,
	        final IFileFragment f2, final File outputdir) {
		EvalTools.notNull(new Object[] { f1, f2 }, FragmentTools.class);
		final IFileFragment ff = new FileFragment(outputdir, null);
		FragmentTools.setLHSFile(ff, f1);
		FragmentTools.setRHSFile(ff, f2);
		// ff.addSourceFile(f1,f2);
		return ff;
	}

	// public static synchronized VariableFragment getVariable(
	// FileFragment parent, String varname) throws ResourceNotAvailableException
	// {
	// return FragmentTools.createVariable(parent, varname);
	// }

	// public static IFileFragment getFragment(String filename)
	// throws IllegalArgumentException {
	// return FragmentTools.getFragment(filename, null);
	// }
	//
	// public static synchronized IFileFragment getFragment(String filename,
	// Class<?> creator) throws IllegalArgumentException {
	// if (filename.contains(Factory.getConfiguration().getString(
	// "file.separator", "/"))) {
	// return FragmentTools.create(filename, creator);
	// } else {
	// return FragmentTools.create(null, filename, null, creator);
	// }
	// }

	// public static synchronized FileFragment create(Class<?> creator) {
	// FileFragment ff = new FileFragment(creator);
	// return ff;
	// }

	// public static synchronized IFileFragment create(File f, Class<?> creator)
	// {
	// return FragmentTools.create(f.getParent(), f.getName(), null, creator);
	// }
	//
	// public static synchronized IFileFragment create(String filename,
	// Class<?> creator) {
	// return FragmentTools.create(FileTools.getDirname(filename), FileTools
	// .getFilename(filename), null, creator);
	// }
	//
	// public static IFileFragment create(String dirname, String filename,
	// Collection<IFileFragment> resourceFiles)
	// throws IllegalArgumentException {
	// return FragmentTools.create(dirname, filename, resourceFiles, null);
	// }

	// public static synchronized IFileFragment create(String dirname,
	// String filename, Collection<IFileFragment> resourceFiles,
	// Class<?> creator) throws IllegalArgumentException {
	// if (StringTools.getFileExtension(filename).equals("nc")
	// || StringTools.getFileExtension(filename).equals("cdf")) {
	// log.debug("IFileFragment references a netcdf file");
	// } else {
	// log.debug("IFileFragment references a {} file", StringTools
	// .getFileExtension(filename));
	// }
	// if (StringTools.getFileExtension(filename).equals("nc")
	// || StringTools.getFileExtension(filename).equals("cdf")) {
	// log.debug("IFileFragment references a netcdf file");
	// } else {
	// log.debug("IFileFragment references a {} file", StringTools
	// .getFileExtension(filename));
	// }
	// if (creator == null) {
	// return FragmentTools.createDirect(dirname, filename, resourceFiles);
	// }
	// File f = null;
	// IFileFragment ff = null;
	// if (filename == null) {
	// ff = new FileFragment(f, resourceFiles, creator);
	// } else {
	// f = new File(dirname, filename);// ,resourceFiles);
	// if (FileFragment.hasFragment(f.getAbsolutePath())) {
	// ff = FileFragment.getFragment(f.getAbsolutePath());
	// } else {
	// ff = new FileFragment(f, resourceFiles, null);
	// }
	// }
	// return ff;
	// }

	// public static synchronized IFileFragment createDirect(String dirname,
	// String filename, Collection<IFileFragment> resourceFiles) {
	// EvalTools.notNull(filename, FragmentTools.class);
	// String dir = dirname;
	// if (dir == null) {
	// dir = Factory.getConfiguration().getString("output.basedir");
	// }
	// File f = new File(dir, filename);
	// if (FileFragment.hasFragment(f.getAbsolutePath())) {
	// return FileFragment.getFragment(f.getAbsolutePath());
	// }
	// IFileFragment ff = new FileFragment(f, resourceFiles, null);
	// return ff;
	// }

	// public static synchronized VariableFragment createVariable(
	// FileFragment parent, String varname) {
	// //if (parent.hasChild(varname)) {
	// // return parent.getChild(varname);
	// //}
	// return new VariableFragment(parent, varname);
	// // return FragmentTools.createVariable(parent, varname, null);
	// }

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

	// public static IFileFragment createFragment(String s, Class<?> creator) {
	// String filename = "";
	// String dirname = "";
	// File f = new File(s);
	// // unqualified filename, without path information
	// if (!f.isAbsolute() &&
	// !s.contains(Factory.getConfiguration().getString("file.separator"))) {
	// dirname = Factory.getConfiguration().getString("input.basedir");
	// filename = s;
	// } else {// qualified file, with at least some path information
	// dirname = FileTools.getDirname(s);
	// filename = FileTools.getFilename(s);
	// }
	// IFileFragment ff = FragmentTools.create(dirname, filename, null,
	// creator);
	// return ff;
	// }

	// /**
	// * Create a IFileFragment and possibly associated VariableFragments.
	// *
	// * @param dataInfo
	// * @return
	// */
	// public static IFileFragment fromString(String dataInfo) {
	// if (FragmentTools.fsp == null) {
	// FragmentTools.fsp = new FragmentStringParser();
	// }
	// return FragmentTools.fsp.parse(dataInfo);
	// }

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

	public static IVariableFragment createStringArray(
	        final IFileFragment parent, final String varname,
	        final Collection<String> c) {
		IVariableFragment vf = null;
		if (parent.hasChild(varname)) {
			vf = parent.getChild(varname);
		} else {
			vf = new VariableFragment(parent, varname);
		}
		int maxlength = 1024;
		for (final String s : c) {
			if (s.length() > maxlength) {
				final int nmaxlength = (int) Math.ceil(Math.log(s.length())
				        / Math.log(2.0d));
				FragmentTools.log
				        .info(
				                "String {} exceeds maxlength of {}, increasing maxlength to {}!",
				                new Object[] { s, maxlength, nmaxlength });
				maxlength = nmaxlength;
			}
		}
		final ArrayChar.D2 a = new ArrayChar.D2(c.size(), maxlength);
		int i = 0;
		for (final String s : c) {
			a.setString(i, s);
			i++;
		}
		vf.setArray(a);
		return vf;
	}

	public static IVariableFragment createVariable(final IFileFragment parent,
	        final String varname, final IVariableFragment ifrg) {
		EvalTools
		        .notNull(new Object[] { parent, varname }, FragmentTools.class);
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
		        new Object[] { ivf, ivf.getParent(), ivf.getIndex() },
		        FragmentTools.class);
		return FragmentTools.getIndexed(ivf.getParent(), ivf.getVarname(), ivf
		        .getIndex().getVarname(), i);
	}

	public static IFileFragment getLHSFile(final IFileFragment ff) {
		final String s = FragmentTools.getStringVar(ff, Factory.getInstance()
		        .getConfiguration().getString("var.reference_file",
		                "reference_file"));
		return Factory.getInstance().getFileFragmentFactory().create(
		        new File(s));
	}

	public static IFileFragment getRHSFile(final IFileFragment ff) {
		final String s = FragmentTools.getStringVar(ff, Factory.getInstance()
		        .getConfiguration().getString("var.query_file", "query_file"));
		return Factory.getInstance().getFileFragmentFactory().create(
		        new File(s));
	}

	public static Collection<IFileFragment> getSourceFiles(
	        final IFileFragment ff) {
		final String sourceFilesVar = Factory.getInstance().getConfiguration()
		        .getString("var.source_files", "source_files");
		FragmentTools.log.debug("Trying to load {} for {}", sourceFilesVar, ff
		        .getAbsolutePath());
		final Collection<String> c = FragmentTools.getStringArray(ff,
		        sourceFilesVar);
		if (c.isEmpty()) {
			FragmentTools.log.warn("Could not find any source_files in " + ff);
			return Collections.emptyList();
		}
		final ArrayList<IFileFragment> al = new ArrayList<IFileFragment>(c
		        .size());
		FragmentTools.log.info("Found the following source files:");
		for (final String s : c) {
			al.add(Factory.getInstance().getFileFragmentFactory().create(s));
			FragmentTools.log.info("{}", al.get(al.size() - 1)
			        .getAbsolutePath());
		}
		// FragmentTools.setSourceFiles(ff, al);
		return al;
	}

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

	public static String getStringVar(final IFileFragment ff,
	        final String variableName) {
		IVariableFragment vf;
		vf = ff.getChild(variableName);
		final Array a = vf.getArray();
		// EvalTools.notNull(a, FragmentTools.class);
		if (a instanceof ArrayChar.D1) {
			return ((ArrayChar.D1) a).getString();
		} else {
			FragmentTools.log.warn("Received array of type {}, expected {}", a
			        .getClass().getName(), ArrayChar.D1.class.getName());
		}
		FragmentTools.log.info("Type of received array {}", a.getClass()
		        .getName());
		return null;
	}

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

	public static void loadAdditionalVars(final IFileFragment ff) {
		FragmentTools.loadAdditionalVars(ff, null);
	}

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
					final ArrayList<IVariableFragment> al = Factory
					        .getInstance().getDataSourceFactory()
					        .getDataSourceFor(ff).readStructure(ff);
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

	public static void loadDefaultVars(final IFileFragment ff) {
		FragmentTools.loadDefaultVars(ff, null);
	}

	public static void loadDefaultVars(final IFileFragment ff,
	        final String configKey) {
		final List<?> l = Factory.getInstance().getConfiguration().getList(
		        configKey == null ? "default.vars" : configKey);
		final Iterator<?> iter = l.iterator();
		FragmentTools.log.debug("Loading default vars for file {}", ff
		        .getAbsolutePath());
		while (iter.hasNext()) {
			final String var = iter.next().toString();
			if (!var.equals("") && !var.trim().isEmpty()) {
				FragmentTools.log.debug("Loading var {}", var);
				ff.getChild(var).getArray();
			}
		}
	}

	public static void setLHSFile(final IFileFragment ff,
	        final IFileFragment lhs) {
		FragmentTools.createString(ff, Factory.getInstance().getConfiguration()
		        .getString("var.reference_file", "reference_file"), lhs
		        .getAbsolutePath());
	}

	public static void setRHSFile(final IFileFragment ff,
	        final IFileFragment rhs) {
		FragmentTools.createString(ff, Factory.getInstance().getConfiguration()
		        .getString("var.query_file", "query_file"), rhs
		        .getAbsolutePath());
	}

}
