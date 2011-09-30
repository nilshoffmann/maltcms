/**
 * 
 */
package maltcms.datastructures.cluster;

import cross.datastructures.feature.IFeatureVector;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public interface ICliqueUpdater<T extends IFeatureVector> {

	public abstract void update(IClique<T> c, T t);

	public abstract void setCentroid(IClique<T> c);

}
