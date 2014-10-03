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

import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import maltcms.tools.ArrayTools;
import net.jcip.annotations.NotThreadSafe;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

@Data
/**
 * <p>ArrayBhattacharryya class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayBhattacharryya implements IArraySimilarity {

    private transient final ObjectDoubleOpenHashMap<Array> cache;

    /**
     * <p>Constructor for ArrayBhattacharryya.</p>
     */
    public ArrayBhattacharryya() {
        cache = new ObjectDoubleOpenHashMap<>();
    }

    private double getSum(Array a) {
        if (cache.size() > 5000) {
            cache.clear();
        }
        if (!cache.containsKey(a)) {
            double d = ArrayTools.integrate(a);
            cache.put(a, d);
        }
        return cache.get(a);
    }

    /** {@inheritDoc} */
    @Override
    public double apply(Array t1,
            Array t2) {
        double s1 = 0, s2 = 0;
        s1 = getSum(t1);
        s2 = getSum(t2);
        double sum = 0;
        for (int i = 0; i < t1.getShape()[0]; i++) {
            sum += Math.sqrt((t1.getDouble(i) / s1)
                    * (t2.getDouble(i) / s2));
        }
        //transformation into Hellinger distance
        final double ret = Math.sqrt(1 - sum);
        if (ret > 0.0d && ret <= 1.0d) {
            return SimilarityTools.toSimilarity(ret);
        }
        return Double.NEGATIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public IArraySimilarity copy() {
        return new ArrayBhattacharryya();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{" + "}");
        return sb.toString();
    }
}
