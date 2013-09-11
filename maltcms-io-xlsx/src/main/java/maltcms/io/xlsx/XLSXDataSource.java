/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.io.xlsx;

import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@ServiceProvider(service = IDataSource.class)
public final class XLSXDataSource implements IDataSource {

	@Override
	public int canRead(IFileFragment ff) {
		final int dotindex = ff.getName().lastIndexOf(".");
		final String filename = ff.getName().toLowerCase();
		if (dotindex == -1) {
			throw new RuntimeException("Could not determine File extension of "
					+ ff);
		}
		try {
			IXLSDataSource ds = getDataSourceFor(ff);
			return 1;
		} catch (NotImplementedException nie) {
			log.debug("no!");
		}
		return 0;
	}

	@Override
	public ArrayList<Array> readAll(IFileFragment f) throws IOException, ResourceNotAvailableException {
		return getDataSourceFor(f).readAll(f);
	}

	@Override
	public ArrayList<Array> readIndexed(IVariableFragment f) throws IOException, ResourceNotAvailableException {
		return getDataSourceFor(f.getParent()).readIndexed(f);
	}

	@Override
	public Array readSingle(IVariableFragment f) throws IOException, ResourceNotAvailableException {
		return getDataSourceFor(f.getParent()).readSingle(f);
	}

	@Override
	public ArrayList<IVariableFragment> readStructure(IFileFragment f) throws IOException {
		return getDataSourceFor(f).readStructure(f);
	}

	@Override
	public IVariableFragment readStructure(IVariableFragment f) throws IOException, ResourceNotAvailableException {
		return getDataSourceFor(f.getParent()).readStructure(f);
	}

	@Override
	public List<String> supportedFormats() {
		final Set<String> al = new HashSet<String>();
		for (final IDataSource ids : getDataSources()) {
			al.addAll(ids.supportedFormats());
		}
		return new ArrayList<String>(al);
	}

	private Collection<? extends IXLSDataSource> getDataSources() {
		Collection<? extends IXLSDataSource> c = Lookup.getDefault().lookupAll(IXLSDataSource.class);
		log.info("Retrieved {} implementations on classpath.",c.size());
		return c;
	}

	private IXLSDataSource getDataSourceFor(IFileFragment f) {
		log.info("Retrieving datasources for xls/xlsx");
		for (IXLSDataSource ds : getDataSources()) {
			log.info("Checking data source: {}", ds.getClass().getName());
			if (ds.canRead(f) > 0) {
				IXLSDataSource dataSource = Factory.getInstance().getObjectFactory().instantiate(ds.getClass().getName(),
						IXLSDataSource.class);
				return dataSource;
			}
		}
		throw new NotImplementedException("No provider available for "
				+ StringTools.getFileExtension(f.getName()));
	}

	@Override
	public boolean write(IFileFragment f) {
		return getDataSourceFor(f).write(f);
//		EvalTools.notNull(f, this);
//		// TODO Implement real write support
//		log.info("Saving {} with XLSXDataSource", f.getUri());
//		log.info("Changing output file from: {}", f.toString());
//		File file = new File(f.getUri());
//		String filename = StringTools.removeFileExt(file.getAbsolutePath());
//		filename += ".cdf";
//		f.setFile(filename);
//		f.addSourceFile(new FileFragment(f.getUri()));
//		log.info("To: {}", filename);
//		return Factory.getInstance().getDataSourceFactory().getDataSourceFor(f).write(f);
	}

	@Override
	public void configure(Configuration cfg) {
	}

	@Override
	public void configurationChanged(ConfigurationEvent ce) {
	}
}
