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
import lombok.EqualsAndHashCode;
import maltcms.math.functions.IArraySimilarity;
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
@EqualsAndHashCode(exclude = {"cache"})
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayWeightedCosine implements IArraySimilarity {

    private final ICacheDelegate<Array, Double> cache;

    private double minimumSimilarity = 0.0d;

    public ArrayWeightedCosine() {
        cache = SimilarityTools.newValueCache("ArrayWeightedCosineCache");
    }

    private double getMaximumIntensity(final Array a) {
        Double d = cache.get(a);
        if (d == null) {
            d = MAMath.getMaximum(a);
            cache.put(a, d);
            return d;
        }
        return d;
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
        if (val > minimumSimilarity) {
            return val;
        }
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
