/**
 * 
 */
package maltcms.db;

import java.util.Collection;

import maltcms.datastructures.ms.IMetabolite;

import com.db4o.query.Predicate;

import cross.datastructures.tuple.Tuple2D;

/*
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public interface IDBQuery<T extends Predicate<G>,G>{

	/**
	 * @param d
	 */
	public abstract void setThreshold(double d);

	public abstract void setDB(String dbLocation);

	public abstract Collection<Tuple2D<Double, IMetabolite>> getBestHits(int k, T ssp);

}
