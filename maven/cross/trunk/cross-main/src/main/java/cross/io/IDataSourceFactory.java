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

import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;
import java.util.List;

/**
 *
 * @author Nils Hoffmann
 */
public interface IDataSourceFactory extends IConfigurable {

    /**
     * Returns a compatible IDataSource for given IFileFragment. First hit wins,
     * if multiple DataSource implementations are registered for the same file
     * type.
     *
     * @param ff
     * @return a specific datasource for the given IFileFragment
	 * @throws IllegalArgumentException if no data source is available for ff
     */
    IDataSource getDataSourceFor(final IFileFragment ff);

    /**
     * @return the list of complete class names used as data sources
     */
    List<String> getDataSources();

    /**
     * Returns a list of supported file extensions
     *
     * @return
     */
    List<String> getSupportedFormats();

    /**
	 * 
     * @param dataSources a list of complete class names
     */
    void setDataSources(final List<String> dataSources);
}
