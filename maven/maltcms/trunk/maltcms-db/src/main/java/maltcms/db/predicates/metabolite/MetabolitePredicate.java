/**
 * 
 */
package maltcms.db.predicates.metabolite;

import java.lang.reflect.Method;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.db.predicates.IMatchPredicate;
import maltcms.db.predicates.MatchPredicate;

import com.db4o.query.Predicate;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public abstract class MetabolitePredicate extends Predicate<IMetabolite>
        implements IMatchPredicate<IMetabolite> {

    /**
     * 
     */
    private static final long serialVersionUID = 4401086253537298137L;

    @Override
    public Method getMethodOnTargetType() {
        return this.im.getMethodOnTargetType();
    }
    protected IMatchPredicate<IMetabolite> im = new MatchPredicate<IMetabolite>();

    @Override
    public void setMethodOnTargetType(Method m) {
        this.im.setMethodOnTargetType(m);
    }

    @Override
    public void setTargetType(Class<IMetabolite> c) {
        this.im.setTargetType(c);
    }
    
}
