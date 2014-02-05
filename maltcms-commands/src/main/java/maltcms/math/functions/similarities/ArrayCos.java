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
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import net.jcip.annotations.NotThreadSafe;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.MAVector;

/**
 * Cosine similarity between arrays.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayCos implements IArraySimilarity {

    private final ICacheDelegate<Array, Double> cache;
    private double minimumSimilarity = 0.0d;

    public ArrayCos() {
        cache = SimilarityTools.newValueCache("ArrayCosCache");
    }

    private double getLength(Array a) {
        Double d = cache.get(a);
        if (d == null) {
            MAVector mav = new MAVector(a);
            d = mav.norm();
            cache.put(a, d);
            return d;
        }
        return d;
    }

    @Override
    public double apply(final Array t1, final Array t2) {
        final double l1 = getLength(t1);
        final double l2 = getLength(t2);
        final int len = t1.getShape()[0];
        double dot = 0.0d;
        for (int i = 0; i < len; i++) {
            dot += (t1.getDouble(i) * t2.getDouble(i));
        }
        final double val = dot / (l1 * l2);
        return val > minimumSimilarity ? val : Double.NEGATIVE_INFINITY;
    }

    @Override
    public IArraySimilarity copy() {
        ArrayCos ac = new ArrayCos();
        ac.setMinimumSimilarity(getMinimumSimilarity());
        return ac;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{" + "}");
        return sb.toString();
    }
}
