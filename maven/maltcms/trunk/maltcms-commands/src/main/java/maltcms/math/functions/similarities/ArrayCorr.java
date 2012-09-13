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

import java.util.WeakHashMap;

import org.apache.commons.math.stat.correlation.PearsonsCorrelation;

import ucar.ma2.Array;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates Pearson's product moment correlation as similarity between arrays.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayCorr implements IArraySimilarity {

    //private final WeakHashMap<Array, double[]> arrayCache = new WeakHashMap<Array, double[]>();
    private boolean returnCoeffDetermination = false;
    private final PearsonsCorrelation pc = new PearsonsCorrelation();

    @Override
    public double apply(final Array t1, final Array t2) {
        double[] t1a = null, t2a = null;
        /*
         if (arrayCache.containsKey(t1)) {
         t1a = arrayCache.get(t1);
         } else {
         t1a = (double[]) t1.get1DJavaArray(double.class);
         arrayCache.put(t1, t1a);
         }
         if (arrayCache.containsKey(t2)) {
         t2a = arrayCache.get(t2);
         } else {
         t2a = (double[]) t2.get1DJavaArray(double.class);
         arrayCache.put(t2, t2a);
         }
         */
        t1a = (double[]) t1.get1DJavaArray(double.class);
        t2a = (double[]) t2.get1DJavaArray(double.class);
        double pcv = pc.correlation(t1a, t2a);
        if (this.returnCoeffDetermination) {
            return pcv * pcv;
        }
        return pcv;
    }
}
