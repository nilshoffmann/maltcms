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
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 * Calculates Spearman's rank correlation as similarity between arrays.
 *
 * @author Nils Hoffmann
 *
 */
@Data
@EqualsAndHashCode
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayRankCorr implements IArraySimilarity {

    private boolean returnCoeffDetermination = false;
    private transient final ICacheDelegate<Array, double[]> cache;
    private final SpearmansCorrelation sc = new SpearmansCorrelation();

    public ArrayRankCorr() {
        cache = SimilarityTools.newValueCache("ArrayCovCache");
    }

    @Override
    public double apply(final Array t1, final Array t2) {
        double[] t1a = null, t2a = null;
        t1a = cache.get(t1);
        t2a = cache.get(t2);
        if (t1a == null) {
            t1a = (double[]) t1.get1DJavaArray(double.class);
            cache.put(t1, t1a);
        }
        if (t2a == null) {
            t2a = (double[]) t2.get1DJavaArray(double.class);
            cache.put(t2, t2a);
        }
        double pcv = sc.correlation(t1a, t2a);
        if (this.returnCoeffDetermination) {
            return pcv * pcv;
        }
        return pcv;
    }

    @Override
    public IArraySimilarity copy() {
        ArrayRankCorr alp = new ArrayRankCorr();
        alp.setReturnCoeffDetermination(isReturnCoeffDetermination());
        return alp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{" + "}");
        return sb.toString();
    }
}
