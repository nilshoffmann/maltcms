/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import maltcms.tools.ArrayTools;
import net.jcip.annotations.NotThreadSafe;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayWeightedCosine2 implements IArraySimilarity {

    private final ICacheDelegate<Array, Array> cache;

    private double minimumSimilarity = 0.0d;

    public ArrayWeightedCosine2() {
        cache = SimilarityTools.newValueCache("ArrayWeightedCosineCache");
    }

    private Array getNormalizedArray(final Array a) {
        Array d = cache.get(a);
        if (d == null) {
            double max = MAMath.getMaximum(a);
            Array b = ArrayTools.mult(a, 1.0d/max);
            cache.put(a, b);
            return b;
        }
        return d;
    }
    
    @Override
    public double apply(final Array t1, final Array t2) {
        double s1 = 0, s2 = 0, c = 0, v1 = 0, v2 = 0;
        final Array t1c = getNormalizedArray(t1);
        final Array t2c = getNormalizedArray(t2);
        for (int i = 0; i < t1c.getShape()[0]; i++) {
            v1 = t1c.getDouble(i);
            v2 = t2c.getDouble(i);
            s1 += miProduct(i + 1, v1);
            s2 += miProduct(i + 1, v2);
            c += miProduct(i + 1, Math.sqrt(v1 * v2));
        }
        final double val = (c * c / (s1 * s2));
        return val > minimumSimilarity ? val : Double.NEGATIVE_INFINITY;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{" + "}");
        return sb.toString();
    }
}
