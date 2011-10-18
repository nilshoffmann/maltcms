/*
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
 * 
 * $Id: DataSourceFactory.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */
package cross.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.tools.StringTools;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory managing objects of type <code>IDataSource</code>. Objects can be
 * registered within cfg/io.properties, key "cross.io.IDataSource". Or by
 * defining them as service implementations of @code{cross.io.IDataSource} in a
 * jar files manifest.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Slf4j
public class DataSourceFactory implements IDataSourceFactory {

    private final HashMap<String, ArrayList<IDataSource>> formatToIDataSource = new HashMap<String, ArrayList<IDataSource>>();
    @Configurable(name = "cross.io.IDataSource")
    private List<String> dataSources = null;

    /**
     * Creates an instance of DataSourceServiceLoader and adds available
     * implementations to internal hash map.
     */
    public DataSourceFactory() {
        DataSourceServiceLoader ids = new DataSourceServiceLoader();
        List<IDataSource> l = ids.getAvailableCommands();
        for (IDataSource source : l) {
            log.info("Adding datasource provider {} for formats {}",source,source.supportedFormats());
            addToHashMap(source);
        }
    }

    /**
     * Adds IDataSource to internal HashMap
     * 
     * @param ids
     */
    private void addToHashMap(final IDataSource ids) {
        EvalTools.notNull(ids, this);
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
        setDataSources(StringTools.toStringList(cfg.getList(
                "cross.io.IDataSource")));
    }

    /**
     * Returns a compatible IDataSource for given IFileFragment. First hit wins,
     * if multiple DataSource implementations are registered for the same file
     * type.
     * 
     * @param ff
     * @return
     */
    @Override
    public IDataSource getDataSourceFor(final IFileFragment ff) {
        String fname = ff.getName();
        String tmp = "";
        String[] parts = fname.split("\\.");
        if (parts.length > 1) {
            int cnt = parts.length - 1;
            tmp = parts[cnt];
            while (cnt >= 0) {
                if (hasDataSourceFor(tmp)) {
                    for (final IDataSource ids : this.formatToIDataSource.get(tmp.
                            toLowerCase())) {
                        if (ids.canRead(ff) == 1) {
                            return ids;
                        }
                    }
                }
                if (cnt > 0) {
                    tmp = parts[cnt - 1] + "." + tmp;
                }
                cnt--;
            }
        }
        throw new IllegalArgumentException("Unsupported file type " + fname);
    }

    private boolean hasDataSourceFor(String fileExtension) {
        if (this.formatToIDataSource.containsKey(fileExtension.toLowerCase())) {
            return true;

        }
        return false;
    }

    /**
     * @return the dataSources
     */
    @Override
    public List<String> getDataSources() {
        return this.dataSources;
    }

    /**
     * Returns a list of supported file extensions
     * 
     * @return
     */
    @Override
    public List<String> getSupportedFormats() {
        final List<String> l = new ArrayList<String>(this.formatToIDataSource.
                keySet());
        return l;
    }

    /**
     * @param dataSources
     *            the dataSources to set
     */
    @Override
    public void setDataSources(final List<String> dataSources) {
        this.dataSources = dataSources;
        for (final String s : this.dataSources) {
            Logging.getLogger(this.getClass()).info(
                    "Trying to load IDataSource {}", s);
            EvalTools.notNull(s, this);
            final IDataSource ids = Factory.getInstance().getObjectFactory().
                    instantiate(s, IDataSource.class);
            addToHashMap(ids);
        }
    }
}
