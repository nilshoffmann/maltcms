/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.math.functions.similarities;

import java.util.Map;
import java.util.WeakHashMap;
import ucar.ma2.Array;
import ucar.ma2.MAVector;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of Tanimoto score.
 *
 * @author Mathias Wilhelm
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayTanimoto implements IArraySimilarity {

	private final Map<Array, Double> arrayToIntensityCache;

	public ArrayTanimoto() {
		arrayToIntensityCache = new WeakHashMap<Array, Double>();;
	}

	private double getDotProduct(MAVector v, Array a) {
		Double d = arrayToIntensityCache.get(a);
		if (d == null) {
			d = v.dot(v);
			arrayToIntensityCache.put(a, d);
		}
		return d.doubleValue();
//		return v.dot(v);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double apply(final Array t1, final Array t2) {
		double score = Double.MIN_VALUE;
		if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
			final MAVector ma1 = new MAVector(t1);
			final MAVector ma2 = new MAVector(t2);

			final double dot = ma1.dot(ma2);
			score = dot / (ma1.dot(ma1) + ma2.dot(ma2) - dot);
		}
		return score;
	}
	
	@Override
	public IArraySimilarity copy() {
		ArrayTanimoto alp = new ArrayTanimoto();
		return alp;
	}
}
