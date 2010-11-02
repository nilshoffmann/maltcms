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
 * $Id: FragmentStringParser.java 86 2010-01-10 14:46:13Z nilshoffmann $
 */

package cross.io.misc;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import cross.Factory;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.exception.NotImplementedException;
import cross.datastructures.tools.EvalTools;

/**
 * This class is used to parse a String representation of a file structure into
 * an Object-Tree representation of that file.
 * 
 * As an example for a valid expression, consider the following line:
 * FILEPATH>varname1&varname2[50:500]&varname3#varname4[0:2000] The prefix, up
 * until ">", designates the resource location, which is currently limited to
 * files. Right of ">" variable names can be given, each of which should be
 * separated by an "&" sign. Each variable can be read in a range only, by
 * putting the range behind the name like "varname2[50:500]" which will try to
 * read varname2 from its 51 st entry (zero based indexing), up until
 * (inclusive), entry 500. You can additionally define an increment like
 * "varname2[50:500:2]" which would only read every second entry in varname2.
 * Finally, variables may be used to index other variables, a typical example
 * are peak lists, which do not contain mass-intensity data for every possible
 * mass, but only for those which have been measured, represented in row
 * compressed storage format. Example: "varname3#varname4[0:2000]", read the
 * items in varname3, starting at the item which is contained in varname4 at
 * position 0 up until the item stored in position 2000. This will give you a
 * list of arrays with 2001 elements, each individual with possibly different
 * length.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FragmentStringParser {

	private final Logger log = Logging.getLogger(this.getClass());

	/**
	 * Create a IVariableFragment from given parameters.
	 * 
	 * @param irnge
	 * @param vrnge
	 * @param parent
	 * @param varname
	 * @param ifrg
	 * @return
	 */
	private IVariableFragment getVariableFragment(final Range irnge,
	        final Range vrnge, final IFileFragment parent,
	        final String varname, final IVariableFragment ifrg) {
		try {
			IVariableFragment vf = null;
			if (parent.hasChild(varname)) {
				vf = parent.getChild(varname);
			} else {
				vf = new VariableFragment(parent, varname);
			}
			vf.setRange((irnge != null ? null : new Range[] { vrnge }));
			if (ifrg != null) {
				vf.setIndex(ifrg);
			}
			// list.add(vf);
			this.log.debug("Created VariableFragment {} with Range {}", vf,
			        vrnge);
			return vf;
		} catch (final IllegalArgumentException iae) {
			this.log.error(iae.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * Parse a string containing a # character, indicating that the variable
	 * preceding the # should be read by using the indices stored under the
	 * variable following the #.
	 * 
	 * @param parent
	 * @param var
	 */
	private void handleIndexVariable(final IFileFragment parent,
	        final String var) {
		this.log.info("Found index var in {}", var);
		// Separate variable name from index variable name
		final String[] varIndex = var.split("#", 0);
		if (varIndex.length > 1) {
			IVariableFragment index = null;
			this.log.info("Index var is {}", varIndex[1]);
			// Look for range of index variable
			final String[] varIndexNameRange = parseIndexRange(varIndex[1]);
			// >1 if there is an additional range
			Range irnge = null;
			this.log.info("varIndexNameRange {}", varIndexNameRange);
			if (varIndexNameRange.length > 1) {
				final String irange = varIndexNameRange[1].substring(0,
				        varIndexNameRange[1].length() - 1);
				final String[] beginEnd = irange.split(":", 0);
				irnge = parseIRange(irnge, beginEnd);
				this.log.info("Index var has range {}", irnge);
				this.log.info("Naming index var {}", varIndexNameRange[0]);
				index = getVariableFragment(null, irnge, parent,
				        varIndexNameRange[0], null);
			} else {
				// one otherwise, so we
				// read the index variable completely, and create it
				this.log.info("Index var has no range");
				if (!parent.hasChild(varIndexNameRange[0])) {
					this.log.info("Parent does not have variable {}",
					        varIndexNameRange[0]);
					this.log.info("Naming index var {}", varIndexNameRange[0]);
					index = getVariableFragment(null, null, parent,
					        varIndexNameRange[0], null);
				}
			}
			handleVariable(varIndex[0], index, parent);

		} else {
			throw new IllegalArgumentException("Could not parse " + var
			        + " after #");
		}
	}

	/**
	 * Parse a string containing a [ character, indicating a range for reading a
	 * variable. [ can also be omitted, if the complete variable should be read.
	 * 
	 * @param string
	 * @param index
	 * @param parent
	 */
	private void handleVariable(final String string,
	        final IVariableFragment index, final IFileFragment parent) {
		this.log.info("Found plain var in {}", string);
		final String[] range = string.split("\\[", 0);
		final String varname = range[0];
		String vrange = null;
		String[] beginEnd = null;
		Range vrnge = null;
		this.log.info("Range {}", Arrays.deepToString(range));
		if (range.length > 1) {
			vrange = range[1].substring(0, range[1].length() - 1);
			// System.out.println("Range: "+range);
			beginEnd = vrange.split(":", 0);
			vrnge = parseIRange(vrnge, beginEnd);
		}

		if (index != null) {
			this.log.info("Setting index for {} to {}", string, index);
		}
		if (parent.hasChild(varname)) {
			parent.getChild(varname).setIndex(index);
			parent.getChild(varname).setRange(new Range[] { vrnge });
		} else {
			getVariableFragment(null, vrnge, parent, varname, index);
		}

	}

	/**
	 * Parses a String describing the structures of an IFileFragment Object-Tree
	 * (document). Initializes all declared variables.
	 * 
	 * @param s
	 * @return
	 */
	public IFileFragment parse(final String s) {
		EvalTools.notNull(s, this);
		if (!s.contains(">")) {
			final IFileFragment ff = Factory.getInstance()
			        .getFileFragmentFactory().create(s);
			return ff;
		}
		this.log.info("Checking if Variables were given explicitly");
		final String[] fileNameVarNameRest = s.split(">", 0);// seperate
		// filename and
		// vars

		final String[] vars = fileNameVarNameRest[1].split("&", 0);// seperate
		// varnames

		String filename = fileNameVarNameRest[0];
		File dir = new File(Factory.getInstance().getConfiguration().getString(
		        "input.basedir", ""));
		final File ff = new File(filename);
		if (ff.isAbsolute()) {
			dir = ff.getParentFile();
			filename = ff.getName();
		}
		this.log.debug("From file {}, parse vars: {}", filename, Arrays
		        .deepToString(vars));

		final IFileFragment parent = Factory.getInstance()
		        .getFileFragmentFactory().create(new File(dir, filename));
		if (vars.length == 0) {
			this.log.info("Vars not given explicitly, loading all!");
			try {
				Factory.getInstance().getDataSourceFactory().getDataSourceFor(
				        parent).readStructure(parent);
			} catch (final IOException e) {
				this.log.error(e.getLocalizedMessage());
			}
		} else {
			// Find index vars first!
			this.log.info("Scanning for variables!");
			for (final String var : vars) {
				// String contains index variable
				if (var.contains("#")) {
					handleIndexVariable(parent, var);
					// Found a normal variable
				} else {
					handleVariable(var, null, parent);
				}
			}
		}
		return parent;
	}

	/**
	 * Parse a range.
	 * 
	 * @param varIndex
	 * @return
	 */
	private String[] parseIndexRange(final String varIndex) {
		String[] indexRange;
		// System.out.println("Longer than 1");
		indexRange = varIndex.split("\\[", 0);// seperate
		this.log.debug("Index range {}", Arrays.deepToString(indexRange));
		return indexRange;
	}

	/**
	 * Parse range.
	 * 
	 * @param irnge2
	 * @param beginEnd
	 * @return
	 */
	private Range parseIRange(final Range irnge2, final String[] beginEnd) {
		Range irnge = irnge2;
		try {
			if (beginEnd.length > 2) {
				irnge = new Range(Integer.parseInt(beginEnd[0]), Integer
				        .parseInt(beginEnd[1]), Integer.parseInt(beginEnd[2]));
			} else {
				irnge = new Range(Integer.parseInt(beginEnd[0]), Integer
				        .parseInt(beginEnd[1]));
			}
		} catch (final NumberFormatException e) {
			this.log.error(e.getLocalizedMessage());
		} catch (final InvalidRangeException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return irnge;
	}

	/**
	 * Return the prefix portion from a String without any suffixes, e.g. the
	 * string itself.
	 * 
	 * @param s
	 * @return
	 */
	public String parsePrefix(final String s) {
		if (s.contains(">")) {
			return s.substring(0, s.indexOf(">") - 1);
		} else {
			if (s.contains(".")) {
				return s;
			} else {
				throw new IllegalArgumentException("String " + s
				        + " cannot be parsed!");
			}
		}
	}

	/**
	 * Creates a new IFileFragment from a given String.
	 * 
	 * @param s
	 * @return
	 */
	public IFileFragment parseString(final String s) {
		if (!s.contains(">")) {
			final IFileFragment ff = Factory.getInstance()
			        .getFileFragmentFactory().create(s);
			return ff;
		}
		this.log.info("Checking if Variables were given explicitly");
		// String[] fileNameVarNameRest = s.split(">", 0);// seperate
		// filename and
		// vars
		// String[] vars = fileNameVarNameRest[1].split("&", 0);// seperate
		throw new NotImplementedException();
		// return null;
	}

}
