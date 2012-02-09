/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
    List<String> getDataSources();

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
    void setDataSources(final List<String> dataSources);

}
