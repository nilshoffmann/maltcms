/**
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
 */
/*
 * 
 *
 * $Id$
 */
package cross.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.openide.util.Lookup;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class DataSourceServiceLoader {

    /**
     * Returns the available implementations of @see{IDataSource}. Elements are
     * sorted according to lexical order on their classnames.
     * 
     * @return
     */
    public List<IDataSource> getAvailableCommands() {
        HashSet<IDataSource> s = new HashSet<IDataSource>();
        for (IDataSource ifc : Lookup.getDefault().lookupAll(IDataSource.class)) {
            s.add(ifc);
        }
        ArrayList<IDataSource> al = new ArrayList<IDataSource>();
        al.addAll(s);
        Collections.sort(al, new Comparator<IDataSource>() {
            @Override
            public int compare(IDataSource o1, IDataSource o2) {
                return o1.getClass().getName().compareTo(
                        o2.getClass().getName());
            }
        });
        return al;
    }
}
