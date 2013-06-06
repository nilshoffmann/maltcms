/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io;

import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;

/**
 * Factory managing objects of type
 * <code>IDataSource</code>. Objects can be registered within cfg/io.properties,
 * key "cross.io.IDataSource". Or by defining them as service implementations of @code{cross.io.IDataSource} in a
 * jar files manifest under META-INF/services/cross.io.IDataSource.
 *
 * @author Nils Hoffmann
 * @see java.util.ServiceLoader
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
            log.debug("Adding datasource provider {} for formats {}", source, source.supportedFormats());
            Factory.getInstance().getObjectFactory().configureType(source);
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
//        setDataSources(StringTools.toStringList(cfg.getList(
//                "cross.io.IDataSource")));
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
						try{
							if (ids.canRead(ff) == 1) {
								return ids;
							}
						}catch(Exception e) {
							log.warn("Caught exception while querying IDataSource "+ids.getClass().getName()+"! IDataSource.canRead should not throw Exceptions: Please fix!");
						}
                    }
                }
                if (cnt > 0) {
                    tmp = parts[cnt - 1] + "." + tmp;
                }
                cnt--;
            }
        }
        throw new IllegalArgumentException("Unsupported file type '" + fname+"'");
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
     * @param dataSources the dataSources to set
     */
    @Override
    public void setDataSources(final List<String> dataSources) {
        this.dataSources = dataSources;
        for (final String s : this.dataSources) {
            log.debug(
                    "Trying to load IDataSource {}", s);
            EvalTools.notNull(s, this);
            final IDataSource ids = Factory.getInstance().getObjectFactory().
                    instantiate(s, IDataSource.class);
            addToHashMap(ids);
        }
    }
}
