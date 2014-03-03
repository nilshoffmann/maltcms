/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
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
 * @author Nils Hoffmann
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

    public static List<Tuple2D<Double, IMetabolite>> asMatchList(Collection<DBMatch> c) {
        List<Tuple2D<Double, IMetabolite>> l = new ArrayList<Tuple2D<Double, IMetabolite>>();
        for (DBMatch dbm : c) {
            l.add(new Tuple2D<Double, IMetabolite>(dbm.getMatchScore(), dbm.getMetabolite()));
        }
        return l;
    }
}
