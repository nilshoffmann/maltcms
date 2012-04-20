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
package maltcms.commands.filters.array;

import java.util.List;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
public class BatchFilter {

    public static Array applyFilters(Array a, List<AArrayFilter> filters) {
        Array b = a;
        for (AArrayFilter filter : filters) {
            LoggerFactory.getLogger(BatchFilter.class).info(
                    "Applying Filter: {}", filter.getClass().getName());
            b = filter.apply(b);
        }
        return b;
    }
}
