/**
 * 
 */
package maltcms.datastructures.cluster;

import maltcms.datastructures.array.IFeatureVector;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public interface ICliqueUpdater<T extends IFeatureVector> {

	public abstract void update(Clique<T> c, T t);

	public abstract void setCentroid(Clique<T> c);

}
