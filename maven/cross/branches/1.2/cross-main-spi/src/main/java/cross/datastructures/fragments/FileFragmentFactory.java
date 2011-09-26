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
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.configuration.Configuration;

import cross.annotations.Configurable;
import cross.io.misc.FragmentStringParser;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.FragmentTools;

/**
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FileFragmentFactory implements IFileFragmentFactory {

	@Configurable(name = "input.basedir")
	private String inputBasedir = "";

	private FragmentStringParser fsp;

        @Override
	public void configure(final Configuration cfg) {
		this.inputBasedir = cfg.getString("input.basedir", "");
	}

        @Override
	public IFileFragment create(final File f) {
		if (FileFragment.hasFragment(f.getAbsolutePath())) {
			return FileFragment.getFragment(f.getAbsolutePath());
		}
		return new FileFragment(f);
	}

        @Override
	public IFileFragment create(final File f,
	        final IFileFragment... sourceFiles) {
		final IFileFragment iff = create(f);
		iff.addSourceFile(sourceFiles);
		return iff;
	}

        @Override
	public IFileFragment create(final String dirname, final String filename) {
		return create(dirname, filename, new LinkedList<IFileFragment>());
	}

        @Override
	public IFileFragment create(final String dirname, final String filename,
	        final IFileFragment... sourceFiles) {
		final IFileFragment iff = create(new File(dirname, filename));
		iff.addSourceFile(sourceFiles);
		return iff;
	}
	
        @Override
	public IFileFragment create(final File dir, final String filename,
	        final IFileFragment... sourceFiles) {
		final IFileFragment iff = create(new File(dir, filename));
		iff.addSourceFile(sourceFiles);
		return iff;
	}

        @Override
	public IFileFragment create(final String dirname, final String filename,
	        final Collection<IFileFragment> resourceFiles)
	        throws IllegalArgumentException {
		File f = null;
		IFileFragment ff = null;
		if (filename == null) {
			ff = new FileFragment(new File(dirname), null);
			ff.addSourceFile(resourceFiles);
		} else {
			f = new File(dirname, filename);// ,resourceFiles);
			if (FileFragment.hasFragment(f.getAbsolutePath())) {
				ff = FileFragment.getFragment(f.getAbsolutePath());
			} else {
				ff = new FileFragment(f);
				ff.addSourceFile(resourceFiles);
			}
		}
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
        @Override
	public IFileFragment create(final IFileFragment f1, final IFileFragment f2,
	        final File outputdir) {
		EvalTools.notNull(new Object[] { f1, f2 }, this);
		final IFileFragment ff = create(outputdir.getAbsolutePath(), null);
		FragmentTools.setLHSFile(ff, f1);
		FragmentTools.setRHSFile(ff, f2);
		return ff;
	}

        @Override
	public IFileFragment create(final String s) {
		String filename = "";
		String dirname = "";
		// unqualified filename, without path information
		if (!s.contains(File.separator)) {
			dirname = this.inputBasedir;
			filename = s;
		} else {// qualified file, with at least some path information
			dirname = FileTools.getDirname(s);
			filename = FileTools.getFilename(s);
		}
		final IFileFragment ff = create(dirname, filename,
		        new LinkedList<IFileFragment>());
		return ff;
	}

	/**
	 * Create a FileFragment and possibly associated VariableFragments.
	 * 
	 * @param dataInfo
	 * @return
	 */
        @Override
	public IFileFragment fromString(final String dataInfo) {
		if (this.fsp == null) {
			this.fsp = new FragmentStringParser();
		}
		return this.fsp.parse(dataInfo);
	}

}
