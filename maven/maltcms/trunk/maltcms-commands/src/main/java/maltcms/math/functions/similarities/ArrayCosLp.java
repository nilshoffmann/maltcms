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
import ucar.ma2.MAVector;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Combinded distance of {@link ArrayCos} and {@link ArrayLp}.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayCosLp implements IArraySimilarity {

    private final IArraySimilarity cos = new ArrayCos();
    private final IArraySimilarity lp = new ArrayLp();

    /**
     * {@inheritDoc}
     */
    @Override
    public double apply(final Array t1, final Array t2) {
        final MAVector mav1 = new MAVector(t1);
        final MAVector mav2 = new MAVector(t2);
        final double n1 = mav1.norm();
        final double n2 = mav2.norm();
        final double cosd = this.cos.apply(t1, t2);
        final double lpd = this.lp.apply(ArrayTools.mult(
                t1, 1.0d / n1), ArrayTools.mult(t2, 1.0d / n2));
        final double dim = t1.getShape()[0];
        final double dist = (lpd / dim) * (1.0d - cosd);
        return SimilarityTools.asSimilarity(dist);
    }

}
