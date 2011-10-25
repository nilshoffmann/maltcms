/**
 * 
 */
package maltcms.experimental.bipace.datastructures.api;

import maltcms.datastructures.array.IFeatureVector;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public interface ICliqueUpdater<T extends IFeatureVector> {

	public abstract void update(IClique<T> c, T t);

	public abstract void setCentroid(IClique<T> c);

}
