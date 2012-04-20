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

import lombok.Data;
import maltcms.commands.filters.AElementFilter;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * This class will set all elements smaller than minimum to zero.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class MinimumFilter extends AArrayFilter {

    private AElementFilter aef = null;

    public MinimumFilter() {
        super();
    }

    /**
     * Default constructor.
     * 
     * @param iMinimum
     *            minimum
     */
    public MinimumFilter(final double iMinimum) {
        this();
        this.aef = new AElementFilter() {

            @Override
            public Double apply(final Double d) {
                if (d < iMinimum) {
                    return 0.0d;
                } else {
                    return d;
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Array apply(final Array a) {
        Array arr = super.apply(a);
        final IndexIterator ii = arr.getIndexIteratorFast();
        while (ii.hasNext()) {
            ii.setDoubleCurrent(this.aef.apply(ii.getDoubleNext()));
        }
        return arr;
    }
}
