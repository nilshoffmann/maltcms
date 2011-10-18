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
 * $Id: ArrayDot.java 116 2010-06-17 08:46:30Z nilshoffmann $
 */
package maltcms.math.functions.similarities;

import ucar.ma2.Array;
import ucar.ma2.MAVector;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Dot product as similarity measure between arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayDot implements IArraySimilarity {

    @Override
    public double apply(final Array t1, final Array t2) {
        // log.info("{},{}",Arrays.toString(t1.getShape()),Arrays.toString(t2.
        // getShape()));
        // if (MAMath.conformable(t1, t2)) {
        if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
            final MAVector ma1 = new MAVector(t1);
            final MAVector ma2 = new MAVector(t2);
            return ma1.dot(ma2);
        }
        throw new IllegalArgumentException("Arrays shapes are incompatible!");
    }

}
