package maltcms.db.predicates.metabolite;

import java.lang.reflect.InvocationTargetException;

import maltcms.datastructures.ms.IMetabolite;

public class MStringMatchPredicate extends MetabolitePredicate {

    /**
     * 
     */
    private static final long serialVersionUID = 7126522681487993030L;
    protected String match;
    private boolean matchCaseInsensitive = true;

    public MStringMatchPredicate(String s) {
        this.match = s;
    }

    public void setCaseInsensitiveMatching(boolean b) {
        this.matchCaseInsensitive = b;
    }

    public boolean isCaseInsensitiveMatching() {
        return this.matchCaseInsensitive;
    }

    @Override
    public boolean match(IMetabolite m) {
        if (getMethodOnTargetType() != null) {
            try {
                Object o = getMethodOnTargetType().invoke(m, (Object[]) null);
                if (o instanceof String) {
                    if (matchCaseInsensitive) {
                        return this.match.equalsIgnoreCase((String) o);
                    }
                    return this.match.equals((String) o);
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
        } else {
            System.err.println("Method not initialized!");
        }
        return false;
    }
}
