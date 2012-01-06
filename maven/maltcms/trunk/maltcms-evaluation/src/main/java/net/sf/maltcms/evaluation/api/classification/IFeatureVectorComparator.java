/**
 * 
 */
package net.sf.maltcms.evaluation.api.classification;

import maltcms.datastructures.array.IFeatureVector;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public interface IFeatureVectorComparator {

	public abstract boolean isTP(IFeatureVector gt, IFeatureVector test);
	
	public abstract boolean isTN(IFeatureVector gt, IFeatureVector test);
	
	public abstract boolean isFP(IFeatureVector gt, IFeatureVector test);
	
	public abstract boolean isFN(IFeatureVector gt, IFeatureVector test);

        public abstract double getSquaredDiff(IFeatureVector gt, IFeatureVector test);
	
}
