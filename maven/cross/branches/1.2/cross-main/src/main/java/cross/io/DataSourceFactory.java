/*
 * 
 *
 * $Id$
 */

package cross.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import java.util.LinkedList;
import lombok.Data;
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
@Data
@Slf4j
public class DataSourceFactory implements IDataSourceFactory {

    private final HashMap<String, ArrayList<IDataSource>> formatToDataSource = new HashMap<String, ArrayList<IDataSource>>();
    private List<IDataSource> dataSources = new LinkedList<IDataSource>();

    /**
     * Creates an instance of DataSourceServiceLoader and adds available
     * implementations to internal hash map.
     */
    public DataSourceFactory() {
        DataSourceServiceLoader ids = new DataSourceServiceLoader();
        List<IDataSource> l = ids.getAvailableCommands();
        for (IDataSource source : l) {
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
            if (this.formatToDataSource.containsKey(s.toLowerCase())) {
                al = this.formatToDataSource.get(s.toLowerCase());
            }
            al.add(ids);
            this.formatToDataSource.put(s.toLowerCase(), al);
        }
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
                    for (final IDataSource ids : this.formatToDataSource.get(tmp.
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
        if (this.formatToDataSource.containsKey(fileExtension.toLowerCase())) {
            return true;

        }
        return false;
    }

    /**
     * @return the dataSources
     */
    @Override
    public List<IDataSource> getDataSources() {
        return this.dataSources;
    }

    /**
     * Returns a list of supported file extensions
     * 
     * @return
     */
    @Override
    public List<String> getSupportedFormats() {
        final List<String> l = new ArrayList<String>(this.formatToDataSource.
                keySet());
        return l;
    }

    /**
     * @param dataSources
     *            the dataSources to set
     */
    @Override
    public void setDataSources(final List<IDataSource> dataSources) {
        this.dataSources = dataSources;
        for (final IDataSource s : this.dataSources) {
            log.info(
                    "Trying to load IDataSource {}", s);
            addToHashMap(s);
        }
    }
}
