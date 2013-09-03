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

import cross.cache.ICacheDelegate;
import cross.cache.softReference.SoftReferenceCache;
import ucar.ma2.Array;
import ucar.ma2.MAVector;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.MAMath;

/**
 * Cosine similarity between arrays.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayCos implements IArraySimilarity {

	private final ICacheDelegate<Array, Double> arrayLengthCache;
	
	public ArrayCos() {
		arrayLengthCache = new SoftReferenceCache<Array,Double>("ArrayCosArrayLengthCache");
	}
	
	private double getLength(Array a) {
		Double d = arrayLengthCache.get(a);
		if(d==null) {
			MAVector mav = new MAVector(a);
			d = mav.norm();
			arrayLengthCache.put(a, d);
		}
		return d.doubleValue();
	}
	
    @Override
    public double apply(final Array t1, final Array t2) {
//        if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
			final double l1 = getLength(t1);
			final double l2 = getLength(t2);
			final int len = t1.getShape()[0];
			double dot = 0.0d;
			for (int i = 0; i < len; i++) {
				dot+=(t1.getDouble(i)*t2.getDouble(i));
			}
			return dot/(l1*l2);
//        }
		
//        throw new IllegalArgumentException("Arrays shapes are incompatible! "
//                + t1.getShape()[0] + " != " + t2.getShape()[0]);
    }
}
