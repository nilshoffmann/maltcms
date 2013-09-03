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

import cross.cache.CacheFactory;
import cross.cache.ICacheDelegate;
import cross.cache.softReference.SoftReferenceCache;
import ucar.ma2.Array;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import maltcms.tools.ArrayTools;
import org.openide.util.lookup.ServiceProvider;

@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayBhattacharryya implements IArraySimilarity {

	private final ICacheDelegate<Array, Double> arrayToIntensityCache;
	
	public ArrayBhattacharryya() {
		arrayToIntensityCache = new SoftReferenceCache<Array,Double>("ArrayBhattacharryyaSumCache");
	}
	
	private double getSum(Array a) {
		Double d = arrayToIntensityCache.get(a);
		if(d==null) {
			d = ArrayTools.integrate(a);
			arrayToIntensityCache.put(a, d);
		}
		return d.doubleValue();
	}
	
    @Override
    public double apply(Array t1,
            Array t2) {
//        if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
            double s1 = 0, s2 = 0;
			s1 = getSum(t1);
			s2 = getSum(t2);
            double sum = 0;
            for(int i = 0; i< t1.getShape()[0]; i++) {
                sum += Math.sqrt((t1.getDouble(i) / s1)
                        * (t2.getDouble(i) / s2));
            }
            //transformation into Hellinger distance
            final double ret = Math.sqrt(1 - sum);
            if (ret > 0.0d && ret <= 1.0d) {
                return SimilarityTools.toSimilarity(ret);
            }
            return Double.NEGATIVE_INFINITY;
//        }
//        throw new IllegalArgumentException("Arrays shapes are incompatible! "
//                + t1.getShape()[0] + " != " + t2.getShape()[0]);
    }
}
