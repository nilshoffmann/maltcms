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

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import ucar.ma2.Array;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import net.jcip.annotations.NotThreadSafe;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.MAMath;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayWeightedCosine implements IArraySimilarity {

	private final ObjectDoubleOpenHashMap<Array> cache;
	
	private double minimumSimilarity = 0.0d;

	public ArrayWeightedCosine() {
		cache = new ObjectDoubleOpenHashMap<>();
	}

	private double getMaximumIntensity(final Array a) {
		if (!cache.containsKey(a)) {
			double d = MAMath.getMaximum(a);
			cache.put(a, d);
		}
		return cache.get(a);
	}

	@Override
	public double apply(final Array t1, final Array t2) {
		final double maxI1 = getMaximumIntensity(t1);
		final double maxI2 = getMaximumIntensity(t2);
		double s1 = 0, s2 = 0, c = 0;
		for (int i = 0; i < t1.getShape()[0]; i++) {
			s1 += miProduct(i + 1, t1.getDouble(i) / maxI1);
			s2 += miProduct(i + 1, t2.getDouble(i) / maxI2);
			c += miProduct(i + 1, Math.sqrt(t1.getDouble(i) / maxI1 * t2.getDouble(i) / maxI2));
		}
		final double val = (c * c / (s1 * s2));
		if(val>minimumSimilarity) {
			return val;
		}
		return val>minimumSimilarity?val: Double.NEGATIVE_INFINITY;
	}

	private double miProduct(double mass, double intensity) {
		return mass * mass * intensity;
	}
	
	@Override
	public IArraySimilarity copy() {
		ArrayWeightedCosine alp = new ArrayWeightedCosine();
		alp.setMinimumSimilarity(getMinimumSimilarity());
		return alp;
	}
}
