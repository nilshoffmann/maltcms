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

package maltcms.io.xml.mzML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.slf4j.Logger;

import ucar.ma2.Array;
import uk.ac.ebi.jmzml.xml.xxindex.MzMLIndexer;
import uk.ac.ebi.jmzml.xml.xxindex.MzMLIndexerFactory;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;

public class MZMLDataSource implements IDataSource {

	private final Logger log = Logging.getLogger(this.getClass());

	private final String[] fileEnding = new String[] { "mzml", "xml" };

	private static WeakHashMap<IFileFragment, MzMLIndexer> fileToIndex = new WeakHashMap<IFileFragment, MzMLIndexer>();

	@Override
	public int canRead(final IFileFragment ff) {
		final int dotindex = ff.getName().lastIndexOf(".");
		final String fileending = ff.getName().substring(dotindex + 1);
		if (dotindex == -1) {
			throw new RuntimeException("Could not determine File extension of "
			        + ff);
		}
		for (final String s : this.fileEnding) {
			if (s.equalsIgnoreCase(fileending)) {
				return 1;
			}
		}

		this.log.debug("no!");
		return 0;
	}

	@Override
	public void configurationChanged(final ConfigurationEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void configure(final Configuration configuration) {
		// TODO Auto-generated method stub

	}

	private MzMLIndexer initIndex(final IFileFragment ff) {
		if (fileToIndex.containsKey(ff)) {
			return fileToIndex.get(ff);
		}
		MzMLIndexer mzidx = MzMLIndexerFactory.getInstance().buildIndex(
		        new File(ff.getAbsolutePath()));
		fileToIndex.put(ff, mzidx);
		return mzidx;
	}

	@Override
	public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
	        ResourceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Array> readIndexed(final IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Array readSingle(final IVariableFragment f) throws IOException,
	        ResourceNotAvailableException {
		return null;
	}

	@Override
	public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
	        throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecross.io.IDataSource#readStructure(cross.datastructures.fragments.
	 * IVariableFragment)
	 */
	@Override
	public IVariableFragment readStructure(final IVariableFragment f)
	        throws IOException, ResourceNotAvailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> supportedFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean write(final IFileFragment f) {
		// TODO Auto-generated method stub
		return false;
	}

}
