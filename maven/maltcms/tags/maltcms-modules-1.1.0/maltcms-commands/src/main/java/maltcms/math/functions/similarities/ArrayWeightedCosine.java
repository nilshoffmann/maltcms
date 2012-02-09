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
package maltcms.math.functions.similarities;

import ucar.ma2.Array;
import ucar.ma2.Index;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayWeightedCosine implements IArraySimilarity {

    @Override
    public double apply(final Array t1, final Array t2) {
        Index i1idx = t1.getIndex();
        Index i2idx = t2.getIndex();
        double s1 = 0, s2 = 0;
        double v = 0.0d;
        for (int i = 0; i < t1.getShape()[0]; i++) {
            s1 += (i + 1.0) * t1.getDouble(i1idx.set(i));
            s2 += (i + 1.0) * t2.getDouble(i2idx.set(i));
        }
        double w1 = 0, w2 = 0;
        for (int i = 0; i < t1.getShape()[0]; i++) {
            w1 = (i + 1.0) * t1.getDouble(i1idx.set(i)) / s1;
            w2 = (i + 1.0) * t2.getDouble(i2idx.set(i)) / s2;
            v += (w1 * w2);
        }
        return v;
    }

}
