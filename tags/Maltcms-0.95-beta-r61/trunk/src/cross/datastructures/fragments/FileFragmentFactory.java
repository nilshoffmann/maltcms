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
 * $Id$
 */

/**
 * 
 */
package cross.datastructures.fragments;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import cross.Factory;
import cross.io.misc.FragmentStringParser;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.FragmentTools;

/**
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FileFragmentFactory {

	private static FileFragmentFactory fff = null;

	public static FileFragmentFactory getInstance() {
		if (FileFragmentFactory.fff == null) {
			FileFragmentFactory.fff = new FileFragmentFactory();
		}
		return FileFragmentFactory.fff;
	}

	private FragmentStringParser fsp;

	private FileFragmentFactory() {

	}

	public IFileFragment create(final Class<?> creator, final Date d) {
		final FileFragment ff = new FileFragment(creator, d);
		return ff;
	}

	public IFileFragment create(final File f) {
		return new FileFragment(f, null, null);
	}

	public IFileFragment create(final File f, final Class<?> creator) {
		return create(f.getParent(), f.getName(), null, creator);
	}

	public IFileFragment create(final String filename, final Class<?> creator) {
		return create(FileTools.getDirname(filename), FileTools
		        .getFilename(filename), null, creator);
	}

	public IFileFragment create(final String dirname, final String filename,
	        final Collection<IFileFragment> resourceFiles)
	        throws IllegalArgumentException {
		return create(dirname, filename, resourceFiles, null);
	}

	public IFileFragment create(final String dirname, final String filename,
	        final Collection<IFileFragment> resourceFiles,
	        final Class<?> creator) throws IllegalArgumentException {
		if (creator == null) {
			return createDirect(dirname, filename, resourceFiles);
		}
		File f = null;
		IFileFragment ff = null;
		if (filename == null) {
			ff = new FileFragment(f, resourceFiles, creator);
		} else {
			f = new File(dirname, filename);// ,resourceFiles);
			if (FileFragment.hasFragment(f.getAbsolutePath())) {
				ff = FileFragment.getFragment(f.getAbsolutePath());
			} else {
				ff = new FileFragment(f, resourceFiles, null);
			}
		}
		return ff;
	}

	public IFileFragment createDirect(final String dirname,
	        final String filename, final Collection<IFileFragment> resourceFiles) {
		EvalTools.notNull(filename, this);
		String dir = dirname;
		if (dir == null) {
			dir = Factory.getInstance().getConfiguration().getString(
			        "output.basedir");
		}
		final File f = new File(dir, filename);
		if (FileFragment.hasFragment(f.getAbsolutePath())) {
			return FileFragment.getFragment(f.getAbsolutePath());
		}
		final FileFragment ff = new FileFragment(f, resourceFiles, null);
		return ff;
	}

	/**
	 * Creates a new AFragment with default name. Both original FileFragments
	 * files are stored as variables below the newly created fragment.
	 * 
	 * @param f1
	 * @param f2
	 * @return
	 */

	public IFileFragment createFragment(final IFileFragment f1,
	        final IFileFragment f2, final Class<?> creator, final Date d) {
		EvalTools.notNull(new Object[] { f1, f2 }, this);
		final IFileFragment ff = create(creator, d);
		FragmentTools.setLHSFile(ff, f1);
		FragmentTools.setRHSFile(ff, f2);
		// ff.addSourceFile(f1,f2);
		return ff;
	}

	public IFileFragment createFragment(final String s, final Class<?> creator) {
		String filename = "";
		String dirname = "";
		// unqualified filename, without path information
		if (!s.contains(File.separator)) {
			dirname = Factory.getInstance().getConfiguration().getString(
			        "input.basedir");
			filename = s;
		} else {// qualified file, with at least some path information
			dirname = FileTools.getDirname(s);
			filename = FileTools.getFilename(s);
		}
		final IFileFragment ff = create(dirname, filename, null, creator);
		return ff;
	}

	/**
	 * Create a FileFragment and possibly associated VariableFragments.
	 * 
	 * @param dataInfo
	 * @return
	 */
	public IFileFragment fromString(final String dataInfo) {
		if (this.fsp == null) {
			this.fsp = new FragmentStringParser();
		}
		return this.fsp.parse(dataInfo);
	}

	public IFileFragment getFragment(final String filename)
	        throws IllegalArgumentException {
		return getFragment(filename, null);
	}

	public IFileFragment getFragment(final String filename,
	        final Class<?> creator) throws IllegalArgumentException {
		if (filename.contains(File.separator)) {
			return create(filename, creator);
		} else {
			return create(null, filename, null, creator);
		}
	}

}
