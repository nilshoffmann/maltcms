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
package maltcms.math.functions;

import lombok.Data;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service = IScalarArraySimilarity.class)
public class ProductSimilarity implements IScalarArraySimilarity {

    private IScalarSimilarity[] scalarSimilarities = new IScalarSimilarity[0];
    
    private IArraySimilarity[] arraySimilarities = new IArraySimilarity[0];

    @Override
    public double apply(double[] s1, double[] s2, Array a1, Array a2) {
        double val = 1.0d;
        for (int i = 0; i < scalarSimilarities.length; i++) {
            double v = scalarSimilarities[i].apply(s1[i], s2[i]);
            if(Double.isInfinite(v) || Double.isNaN(v)) {
                return Double.NEGATIVE_INFINITY;
            }
            val*=v;
        }
        for (int i = 0; i< arraySimilarities.length;i++) {
            val*=arraySimilarities[i].apply(a1, a2);
        }
        return val;
    }
    
}
