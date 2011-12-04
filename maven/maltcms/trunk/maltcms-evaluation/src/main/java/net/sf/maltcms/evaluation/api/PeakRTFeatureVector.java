/**
 * 
 */
package net.sf.maltcms.evaluation.api;

import java.util.Arrays;
import java.util.List;

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class PeakRTFeatureVector implements IFeatureVector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5936343655074144856L;
	private final ArrayDouble.D0 rt;
	
	public PeakRTFeatureVector(double rt) {
		this.rt = new ArrayDouble.D0();
		this.rt.set(rt);
	}
	
	/* (non-Javadoc)
	 * @see maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
	 */
	@Override
	public Array getFeature(String name) {
		if(name.equals("RT")) {
			return this.rt;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
	 */
	@Override
	public List<String> getFeatureNames() {
		return Arrays.asList("RT");
	}
	
	public double getRT() {
		return this.rt.get();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RT = "+getRT());
		return sb.toString();
	}

}
