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

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.commands.ICommand;
import lombok.Data;

/**
 * AArrayFilter applicable to Array objects, returning Array objects.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
public abstract class AArrayFilter implements ICommand<Array, Array> {

    protected transient AElementFilter ef = null;
    @Configurable
    private boolean copyArray = true;

    /*
     * (non-Javadoc)
     * 
     * @see maltcms.ucar.ma2.Filter#filter(java.lang.Object)
     */
    @Override
    public Array apply(final Array a) {
        if (this.copyArray) {
            return a.copy();
        }
        return a;
    }

    public Array[] apply(final Array[] a) {
        Array[] ret = new Array[a.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = apply(a[i]);
        }
        return ret;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.copyArray = cfg.getBoolean(this.getClass().getName()
                + ".copyArray", true);
    }
}
