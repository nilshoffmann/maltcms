/**
 * 
 */
package maltcms.db.predicates;

import java.lang.reflect.Method;

/*
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 * @param <T>
 */
public interface IMatchPredicate<T> {

	public abstract void setMethodOnTargetType(Method m);

	public abstract void setTargetType(Class<T> c);
	
	public abstract Method getMethodOnTargetType();

}
