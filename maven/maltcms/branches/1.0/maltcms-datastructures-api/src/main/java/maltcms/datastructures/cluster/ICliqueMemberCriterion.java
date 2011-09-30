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
public interface ICliqueMemberCriterion<T extends IFeatureVector> {

	public abstract boolean shouldBeMemberOf(IClique<T> c, T t);

}
