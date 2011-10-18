/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: ArrayTanimoto.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.math.functions.similarities;


import ucar.ma2.Array;
import ucar.ma2.MAVector;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of Tanimoto score.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayTanimoto implements IArraySimilarity {

    /**
     * {@inheritDoc}
     */
    @Override
    public double apply(final Array t1, final Array t2) {

        double score = Double.MIN_VALUE;

        if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
            final MAVector ma1 = new MAVector(t1);
            final MAVector ma2 = new MAVector(t2);

            final double dot = ma1.dot(ma2);
            score = dot / (ma1.dot(ma1) + ma2.dot(ma2) - dot);

        }

        return score;
    }

}
