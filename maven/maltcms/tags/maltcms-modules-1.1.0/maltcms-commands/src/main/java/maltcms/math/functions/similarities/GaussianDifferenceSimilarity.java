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

import lombok.Data;
import maltcms.math.functions.IScalarSimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service=IScalarSimilarity.class)
public class GaussianDifferenceSimilarity implements
        IScalarSimilarity {

    private double tolerance = 5.0d;
    private double threshold = 0.0d;

    /**
     * Calculates the scalar
     * @param time1
     * @param time2
     * @return 
     */
    @Override
    public double apply(double time1, double time2) {
        // if no time is supplied, use 1 as default -> cosine/dot product
        // similarity
        final double weight = ((time1 == -1) || (time2 == -1)) ? 1.0d
                : Math.exp(
                -((time1 - time2) * (time1 - time2) / (2.0d * this.tolerance * this.tolerance)));
        // 1 for perfect time correspondence, 0 for really bad time
        // correspondence (towards infinity)
        if (weight - this.threshold < 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return weight;
    }

}
