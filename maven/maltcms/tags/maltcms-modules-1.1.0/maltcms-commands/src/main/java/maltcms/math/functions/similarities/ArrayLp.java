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

import maltcms.tools.ArrayTools;

import ucar.ma2.Array;
import ucar.ma2.MAMath;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Lp-norm based distance between arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayLp implements IArraySimilarity {

	private double p = 2.0d;
	private boolean normalizeByLength = false;
	private double sqrtn = Double.NaN;

	@Override
    public double apply(final Array t1, final Array t2) {
        final double val = Math.pow(MAMath.sumDouble(ArrayTools.pow(
                ArrayTools.diff(t1, t2), 2.0d)), 1.0d / this.p);
//        if (this.normalizeByLength) {
//            return SimilarityTools.transformToUnitRange(val /Math.sqrt(t1.getShape()[0]));
//        } else {
//            return SimilarityTools.transformToUnitRange(val);
//        }
        return -val;
    }
}
