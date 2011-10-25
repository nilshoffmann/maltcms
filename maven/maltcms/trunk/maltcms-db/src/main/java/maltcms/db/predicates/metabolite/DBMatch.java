/*
 * $license$
 *
 * $Id$
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
