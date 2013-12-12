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

import com.carrotsearch.hppc.ObjectObjectOpenHashMap;
import java.util.WeakHashMap;

import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.math.stat.correlation.Covariance;
import org.openide.util.lookup.ServiceProvider;

import ucar.ma2.Array;

/**
 * Calculates Pearson's product moment correlation as similarity between arrays.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayCov implements IArraySimilarity {

	private final ObjectObjectOpenHashMap<Array,double[]> cache;
	private boolean returnCoeffDetermination = false;

	public ArrayCov() {
		cache = new ObjectObjectOpenHashMap<>();
	}
	
	@Override
	public double apply(final Array t1, final Array t2) {
		Covariance pc = new Covariance();
		double[] t1a = null, t2a = null;

		if (cache.containsKey(t1)) {
			t1a = cache.get(t1);
		} else {
			t1a = (double[]) t1.get1DJavaArray(double.class);
			cache.put(t1, t1a);
		}
		if (cache.containsKey(t2)) {
			t2a = cache.get(t2);
		} else {
			t2a = (double[]) t2.get1DJavaArray(double.class);
			cache.put(t2, t2a);
		}

//        t1a = (double[]) t1.get1DJavaArray(double.class);
//        t2a = (double[]) t2.get1DJavaArray(double.class);
		double pcv = pc.covariance(t1a, t2a);
		if (this.returnCoeffDetermination) {
			return pcv * pcv;
		}
		return pcv;
	}

	@Override
	public IArraySimilarity copy() {
		ArrayCov ac = new ArrayCov();
		ac.setReturnCoeffDetermination(isReturnCoeffDetermination());
		return ac;
	}
		
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("{"+"}");
		return sb.toString();
	}
}
