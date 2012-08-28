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
import ucar.ma2.IndexIterator;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Hamming distance between binary vectors.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayHamming implements IArraySimilarity {

    @Override
    public double apply(final Array t1, final Array t2) {
        int d = 0;
        final IndexIterator it1 = t1.getIndexIterator();
        final IndexIterator it2 = t2.getIndexIterator();
        while (it1.hasNext() && it2.hasNext()) {
            boolean b1 = (it1.getDoubleNext()) > 0 ? true : false;
            final boolean b2 = (it2.getDoubleNext()) > 0 ? true : false;
            b1 = ((b1 && !b2) || (!b1 && b2));
            if (b1) {
                d++;
            }
        }
        return SimilarityTools.asSimilarity(d);
    }
}
