/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 *
 * $Id$
 */
package maltcms.math.functions.similarities;


import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayBhattacharryya implements IArraySimilarity {

    @Override
    public double apply(Array t1,
            Array t2) {
        if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
            IndexIterator iter1 = t1.getIndexIterator();
            double s1 = 0, s2 = 0;
            while (iter1.hasNext()) {
                s1 += iter1.getDoubleNext();
            }
            IndexIterator iter2 = t2.getIndexIterator();
            while (iter2.hasNext()) {
                s2 += iter2.getDoubleNext();
            }
            iter1 = t1.getIndexIterator();
            iter2 = t2.getIndexIterator();
            double sum = 0;
            while (iter1.hasNext() && iter2.hasNext()) {
                sum += Math.sqrt((iter1.getDoubleNext() / s1)
                        * (iter2.getDoubleNext() / s2));
            }
            //transformation into Hellinger distance
            final double ret = Math.sqrt(1 - sum);
            if (ret > 0.0d && ret <= 1.0d) {
                return SimilarityTools.transformToUnitRange(ret);
            }
            return Double.NEGATIVE_INFINITY;
        }
        throw new IllegalArgumentException("Arrays shapes are incompatible! "
                + t1.getShape()[0] + " != " + t2.getShape()[0]);
    }

}
