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
