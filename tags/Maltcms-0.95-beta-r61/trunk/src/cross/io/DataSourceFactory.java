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

package cross.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import annotations.Configurable;
import cross.Factory;
import cross.IConfigurable;
import cross.Logging;
import cross.datastructures.fragments.IFileFragment;
import cross.tools.EvalTools;
import cross.tools.StringTools;

/**
 * Factory managing objects of type <code>IDataSource</code>. Objects can be
 * registered within cfg/io.properties, key "cross.io.IDataSource".
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class DataSourceFactory implements IConfigurable {

	private static DataSourceFactory dsf = null;

	public static DataSourceFactory getInstance() {
		if (DataSourceFactory.dsf == null) {
			DataSourceFactory.dsf = new DataSourceFactory();
		}
		return DataSourceFactory.dsf;
	}

	private final HashMap<String, ArrayList<IDataSource>> formatToIDataSource = new HashMap<String, ArrayList<IDataSource>>();

	@Configurable(name = "cross.io.IDataSource")
	private List<?> dataSources = null;

	private DataSourceFactory() {

	}

	/**
	 * Adds IDataSource to internal HashMap
	 * 
	 * @param ids
	 */
	private void addToHashMap(final IDataSource ids) {
		for (final String s : ids.supportedFormats()) {
			ArrayList<IDataSource> al = new ArrayList<IDataSource>(1);
			if (this.formatToIDataSource.containsKey(s.toLowerCase())) {
				al = this.formatToIDataSource.get(s.toLowerCase());
			}
			al.add(ids);
			this.formatToIDataSource.put(s.toLowerCase(), al);
		}
	}

	@Override
	public void configure(final Configuration cfg) {
		this.dataSources = cfg.getList("cross.io.IDataSource");
		final ArrayList<String> al1 = StringTools
		        .toStringList(this.dataSources);
		for (final String s : al1) {
			Logging.getLogger(this.getClass()).info(
			        "Trying to load IDataSource {}", s);
			EvalTools.notNull(s, this);
			final IDataSource ids = Factory.getInstance().instantiate(s,
			        IDataSource.class);
			addToHashMap(ids);
		}
	}

	/**
	 * Returns a compatible IDataSource for given IFileFragment.
	 * 
	 * @param ff
	 * @return
	 */
	public IDataSource getDataSourceFor(final IFileFragment ff) {
		final String ext = StringTools.getFileExtension(ff.getAbsolutePath())
		        .toLowerCase();
		if (this.formatToIDataSource.containsKey(ext)) {
			for (final IDataSource ids : this.formatToIDataSource.get(ext)) {
				if (ids.canRead(ff) == 1) {
					return ids;
				}
			}
		}
		throw new IllegalArgumentException("Unsupported file extension " + ext);
	}

	/**
	 * Returns a list of supported file extensions
	 * 
	 * @return
	 */
	public List<String> getSupportedFormats() {
		List<String> l = new ArrayList<String>(this.formatToIDataSource
		        .keySet());
		return l;
	}

	/**
	 * @return the dataSources
	 */
	public List<?> getDataSources() {
		return this.dataSources;
	}

	/**
	 * @param dataSources
	 *            the dataSources to set
	 */
	public void setDataSources(final List<?> dataSources) {
		this.dataSources = dataSources;
	}

}
