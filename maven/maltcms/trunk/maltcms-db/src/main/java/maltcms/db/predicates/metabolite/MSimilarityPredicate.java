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
package maltcms.db.predicates.metabolite;

import java.util.List;

import maltcms.datastructures.ms.IMetabolite;
import cross.datastructures.tuple.Tuple2D;

public class MSimilarityPredicate extends MetabolitePredicate {

    /**
     *
     */
    private static final long serialVersionUID = -3684834963267981958L;
    private final MetaboliteSimilarity s;

    public MSimilarityPredicate(MetaboliteSimilarity s) {
        this.s = s;
    }

    public List<Tuple2D<Double, IMetabolite>> getSimilaritiesAboveThreshold() {
        return this.s.getMatches();
    }

    public void resetResultList() {
        this.s.getMatches().clear();
    }

    @Override
    public boolean match(IMetabolite arg0) {
        return this.s.match(arg0);
    }
}
