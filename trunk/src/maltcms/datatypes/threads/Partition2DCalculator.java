/**
 * 
 */
package maltcms.datatypes.threads;

import java.util.List;
import java.util.concurrent.Callable;

import maltcms.datastructures.array.ArrayFactory;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.experimental.operations.Cosine;
import maltcms.experimental.operations.IPairwiseFeatureVectorSequenceOperation;
import maltcms.experimental.operations.TwoFeatureVectorOperation;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import cross.Factory;
import cross.IConfigurable;
import cross.Logging;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.exception.NotImplementedException;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class Partition2DCalculator implements Callable<IArrayD2Double>,
        IConfigurable, IPairwiseFeatureVectorSequenceOperation<IArrayD2Double> {

	private final Logger log = Logging.getLogger(this);
	private List<IFeatureVector> l1, l2;
	private int minl1, maxl1, minl2, maxl2;

	@Configurable
	private TwoFeatureVectorOperation featureVectorOperation;

	@Configurable
	private boolean useDenseArrays = true;

	public void setData(final List<IFeatureVector> l1,
	        final List<IFeatureVector> l2) {
		this.l1 = l1;
		this.l2 = l2;
	}

	public void setRange(int minl1, int maxl1, int minl2, int maxl2) {
		this.minl1 = minl1;
		this.maxl1 = maxl1;
		this.minl2 = minl2;
		this.maxl2 = maxl2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public IArrayD2Double call() throws Exception {
		return apply(this.l1, this.l2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecross.IConfigurable#configure(org.apache.commons.configuration.
	 * Configuration)
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.featureVectorOperation = Factory.getInstance().getObjectFactory()
		        .instantiate(
		                cfg.getString(this.getClass().getName()
		                        + "featureVectorOperation", Cosine.class
		                        .getCanonicalName()),
		                TwoFeatureVectorOperation.class);
		this.useDenseArrays = cfg.getBoolean(this.getClass().getName()
		        + ".useDenseArrays", true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.experimental.operations.IPairwiseFeatureVectorSequenceOperation
	 * #apply(java.util.List, java.util.List)
	 */
	@Override
	public IArrayD2Double apply(List<IFeatureVector> l1, List<IFeatureVector> l2) {
		ArrayFactory af = new ArrayFactory();
		IArrayD2Double a;
		if (this.useDenseArrays) {
			a = af.create(l1.size(), l2.size(), this.featureVectorOperation
			        .isMinimize() ? Double.POSITIVE_INFINITY
			        : Double.NEGATIVE_INFINITY);
		} else {
			a = af.createSparseArray(l1.size(), l2.size());
		}
		for (int i = this.minl1; i <= this.maxl1; i++) {
			for (int j = this.minl2; j <= this.maxl2; j++) {
				a.set(i, j, this.featureVectorOperation.apply(l1.get(i), l2
				        .get(j)));
			}
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.experimental.operations.IPairwiseFeatureVectorSequenceOperation
	 * #getPairwiseFeatureVectorOperation()
	 */
	@Override
	public TwoFeatureVectorOperation getPairwiseFeatureVectorOperation() {
		return this.featureVectorOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.experimental.operations.IPairwiseFeatureVectorSequenceOperation
	 * #setPairwiseFeatureVectorOperation
	 * (maltcms.experimental.operations.TwoFeatureVectorOperation)
	 */
	@Override
	public void setPairwiseFeatureVectorOperation(TwoFeatureVectorOperation pao) {
		this.featureVectorOperation = pao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.experimental.datastructures.IFileFragmentModifier#modify(cross
	 * .datastructures.fragments.IFileFragment)
	 */
	@Override
	public void modify(IFileFragment iff) {
		throw new NotImplementedException();

	}

}
