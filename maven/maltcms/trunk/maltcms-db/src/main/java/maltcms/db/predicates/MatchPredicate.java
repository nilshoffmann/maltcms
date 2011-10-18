package maltcms.db.predicates;

import java.lang.reflect.Method;

public class MatchPredicate<T> implements IMatchPredicate<T> {

    protected Method m;
    protected Class<T> c;

    /* (non-Javadoc)
     * @see maltcms.db.predicates.IMatchPredicate#setMethodOnTargetType(java.lang.reflect.Method)
     */
    public void setMethodOnTargetType(Method m) {
        this.m = m;
    }

    /* (non-Javadoc)
     * @see maltcms.db.predicates.IMatchPredicate#setTargetType(java.lang.Class)
     */
    public void setTargetType(Class<T> c) {
        this.c = c;
    }

    @Override
    public Method getMethodOnTargetType() {
        return this.m;
    }
}
