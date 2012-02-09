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

import cross.datastructures.tuple.Tuple2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import maltcms.datastructures.ms.IMetabolite;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class DBMatch implements Comparable {

    private final String dbLocation;
    private final double matchScore;
    private final IMetabolite metabolite;

    @Override
    public int compareTo(Object t) {
        if (t instanceof DBMatch) {
            return Double.compare(matchScore, ((DBMatch) t).getMatchScore());
        }
        return 0;
    }
    
    public static List<Tuple2D<Double,IMetabolite>> asMatchList(Collection<DBMatch> c) {
        List<Tuple2D<Double,IMetabolite>> l = new ArrayList<Tuple2D<Double,IMetabolite>>();
        for(DBMatch dbm:c) {
            l.add(new Tuple2D<Double,IMetabolite>(dbm.getMatchScore(),dbm.getMetabolite()));
        }
        return l;
    }
}
