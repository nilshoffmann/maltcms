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

import maltcms.commands.filters.AElementFilter;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.annotations.Configurable;
import lombok.Data;
import org.openide.util.lookup.ServiceProvider;

/**
 * Applies log10 or ln to all elements of an array.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class LogFilter extends AArrayFilter {

    private AElementFilter aef = null;
    @Configurable
    private final boolean naturalLog = false;

    public LogFilter() {
        super();
        this.aef = new AElementFilter() {
            @Override
            public Double apply(final Double t) {
                return Math.log10(t);
            }
        };
    }

    public LogFilter(final boolean natural) {
        this();
        if (natural) {
            this.aef = new AElementFilter() {
                @Override
                public Double apply(final Double t) {
                    return Math.log(t);
                }
            };
        } else {
            this.aef = new AElementFilter() {
                @Override
                public Double apply(final Double t) {
                    return Math.log10(t);
                }
            };
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.ArrayFilter#filter(maltcms.ucar.ma2.Array)
     */
    @Override
    public Array apply(final Array a) {
        final Array arr = super.apply(a);
        final IndexIterator ii = arr.getIndexIteratorFast();
        double next = 0.0d;
        while (ii.hasNext()) {
            next = ii.getDoubleNext();
            ii.setDoubleCurrent(this.aef.apply(next));
        }
        return arr;
    }
}
