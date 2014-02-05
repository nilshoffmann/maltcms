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
import maltcms.math.functions.IArraySimilarity;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class SimilarityTools {

    /**
     *
     * @param distance
     * @return
     */
    public static double toSimilarity(double distance) {
        return -distance;
    }

    /**
     *
     * @return
     */
    public static double toDistance(IArraySimilarity sim, Array a, Array b) {
        double dist = 0.0d;
        dist += sim.apply(a, a);
        dist += sim.apply(b, b);
        dist -= (2 * sim.apply(a, b));
        return Math.sqrt(dist);
    }

    public static <K, V> ICacheDelegate<K, V> newValueCache(String name) {
        return CacheFactory.createVolatileCache(name, 30, 60, 10000);
    }
}
