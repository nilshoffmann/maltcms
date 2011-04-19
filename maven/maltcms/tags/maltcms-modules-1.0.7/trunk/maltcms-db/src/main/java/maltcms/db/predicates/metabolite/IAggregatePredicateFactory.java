/**
 * 
 */
package maltcms.db.predicates.metabolite;

import com.db4o.query.Predicate;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public interface IAggregatePredicateFactory<T> {

	/**
	 * RetentionIndex=[1500,2000] => NumRangePredicate on method getRetentionIndex
	 * Name=BLABLA => StringMatchPredicate on method getName
	 * Name=BLABLA< => StringContainsPredicate on method getName, BLABLA should be a substring
	 * @param args
	 */
	public abstract Predicate<T> digestCommandLine(String[] args);

}
