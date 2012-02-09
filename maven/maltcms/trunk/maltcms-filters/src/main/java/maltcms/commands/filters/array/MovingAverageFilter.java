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

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.tools.MathTools;
import lombok.Data;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class MovingAverageFilter extends AArrayFilter {

    @Configurable
    private int window = 10;

    public MovingAverageFilter() {
        super();
    }

    @Override
    public Array apply(final Array a) {
        Array arr = super.apply(a);
        if (arr.getRank() == 1) {
            final double[] d = (double[]) arr.get1DJavaArray(double.class);
            arr = Array.factory(MathTools.weightedAverage(this.window, d));
        } else {
            throw new IllegalArgumentException(
                    "Can only work on arrays of dimension 1");
        }
        return arr;
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.window = cfg.getInt(this.getClass().getName() + ".window", 10);
    }
}
