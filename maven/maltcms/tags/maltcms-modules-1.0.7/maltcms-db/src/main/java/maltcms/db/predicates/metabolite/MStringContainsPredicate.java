package maltcms.db.predicates.metabolite;

import java.lang.reflect.InvocationTargetException;

import maltcms.datastructures.ms.IMetabolite;

public class MStringContainsPredicate extends MStringMatchPredicate {

	/**
     * 
     */
    private static final long serialVersionUID = -8108747283945214056L;

	public MStringContainsPredicate(String s) {
		super(s);
	}

	@Override
	public boolean match(IMetabolite q) {
		if(getMethodOnTargetType()!=null) {
			try {
				Object o = getMethodOnTargetType().invoke(q, (Object[])null);
				if(o instanceof String) {
					if(isCaseInsensitiveMatching()) {
						return ((String)o).toLowerCase().contains(this.match.toLowerCase());
					}else {
						return ((String)o).contains(this.match);
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
		}else {
			System.err.println("Method not initialized!");
		}
		return false;
	}

}
