/*
 * $license$
 *
 * $Id$
 */

package cross.io;

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import java.util.List;

/**
 *
 * @author nilshoffmann
 */
public interface IDataSourceFactory extends IConfigurable {

    /**
     * Returns a compatible IDataSource for given IFileFragment. First hit wins,
     * if multiple DataSource implementations are registered for the same file
     * type.
     *
     * @param ff
     * @return
     */
    IDataSource getDataSourceFor(final IFileFragment ff);

    /**
     * @return the dataSources
     */
    List<IDataSource> getDataSources();

    /**
     * Returns a list of supported file extensions
     *
     * @return
     */
    List<String> getSupportedFormats();

    /**
     * @param dataSources
     * the dataSources to set
     */
    void setDataSources(final List<IDataSource> dataSources);

}
