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
public interface ICliqueMemberCriterion<T extends IFeatureVector> {

	public abstract boolean shouldBeMemberOf(IClique<T> c, T t);

}
