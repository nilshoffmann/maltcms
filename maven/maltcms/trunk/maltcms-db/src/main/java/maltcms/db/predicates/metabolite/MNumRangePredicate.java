package maltcms.db.predicates.metabolite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import maltcms.datastructures.ms.IMetabolite;

public class MNumRangePredicate extends MetabolitePredicate {

    /**
     * 
     */
    private static final long serialVersionUID = 4384503672151011994L;
    private Number lB, uB;

    public MNumRangePredicate(Number lowerBound, Number upperBound, Method value) {
        this.lB = lowerBound;
        this.uB = upperBound;
        setMethodOnTargetType(value);
    }

    @Override
    public boolean match(IMetabolite met) {
        try {
            Object val = getMethodOnTargetType().invoke(met, (Object[]) null);
            if (val instanceof Number) {
                Number n = (Number) val;
                if (val instanceof Float) {
                    return ((lB.floatValue() <= n.floatValue()) && (n.floatValue() <= uB.
                            floatValue()));
                }
                if (val instanceof Double) {
                    return ((lB.doubleValue() <= n.doubleValue()) && (n.
                            doubleValue() <= uB.doubleValue()));
                }
                if (val instanceof Integer) {
                    return ((lB.intValue() <= n.intValue()) && (n.intValue() <= uB.
                            intValue()));
                }
                if (val instanceof Long) {
                    return ((lB.longValue() <= n.longValue()) && (n.longValue() <= uB.
                            longValue()));
                }
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
