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

package maltcms.io.proteomecommons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.proteomecommons.io.GenericPeakListReader;
import org.proteomecommons.io.Peak;
import org.proteomecommons.io.PeakList;
import org.proteomecommons.io.PeakListReader;
import org.proteomecommons.io.UnknownFileFormatException;
import org.proteomecommons.io.mzdata.MzDataPeakListReader;
import org.proteomecommons.io.mzxml.MzXMLPeakListReader;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;

public class PCDataSource implements IDataSource {

	private final Logger log = Logging.getLogger(this);

	@Override
	public int canRead(final IFileFragment ff) {
		try {
			GenericPeakListReader
			        .getPeakListReaderFactory(ff.getAbsolutePath());
			return 1;
		} catch (final UnknownFileFormatException e) {
			return 0;
		}
	}

	@Override
	public void configurationChanged(final ConfigurationEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void configure(final Configuration configuration) {

	}

	protected void processMetadata(final IFileFragment f, final PeakListReader p) {
		if (p instanceof MzXMLPeakListReader) {
			processMetadataMZXML(f, (MzXMLPeakListReader) p);
		}
		if (p instanceof MzDataPeakListReader) {
			processMetadataMZDATA(f, (MzDataPeakListReader) p);
		}
		throw new NotImplementedException("No handler implemented for "
		        + p.getClass());
	}

	protected void processMetadataMZDATA(final IFileFragment f,
	        final MzDataPeakListReader p) {
		throw new NotImplementedException();
	}

	protected void processMetadataMZXML(final IFileFragment f,
	        final MzXMLPeakListReader p) {
		throw new NotImplementedException();
	}

	@Override
	public ArrayList<Array> readAll(final IFileFragment f) throws IOException {
		final ArrayList<Array> al = new ArrayList<Array>();
		try {
			final PeakListReader reader = GenericPeakListReader
			        .getPeakListReader(f.getAbsolutePath());
			final PeakList pl = reader.getPeakList();
			final ArrayDouble.D1 mz = new ArrayDouble.D1(pl.getPeaks().length);
			final ArrayDouble.D1 intens = new ArrayDouble.D1(
			        pl.getPeaks().length);
			final int cnt = 0;
			// FIXME get centroided / averaged information
			for (final Peak p : pl.getPeaks()) {
				mz.set(cnt, p.getMassOverCharge());
				intens.set(cnt, p.getIntensity());
			}
			al.add(mz);
			al.add(intens);
		} catch (final UnknownFileFormatException e) {
			this.log.error(e.getLocalizedMessage());
		}
		return al;
	}

	@Override
	public ArrayList<Array> readIndexed(final IVariableFragment f)
	        throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public Array readSingle(final IVariableFragment f) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
	        throws IOException {
		throw new NotImplementedException();
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
		throw new NotImplementedException();
	}

	@Override
	public List<String> supportedFormats() {
		final ArrayList<String> formats = new ArrayList<String>(2);
		formats.add("mzXML");
		formats.add("mzData");
		formats.add("xml");
		return formats;
	}

	@Override
	public boolean write(final IFileFragment f) {
		throw new NotImplementedException();
	}

}
