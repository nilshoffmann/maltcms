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

import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Hamming distance between binary vectors.
 *
 * @author Nils Hoffmann
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayHamming implements IArraySimilarity {

    @Override
    public double apply(final Array t1, final Array t2) {
        int d = 0;
        final IndexIterator it1 = t1.getIndexIterator();
        final IndexIterator it2 = t2.getIndexIterator();
        while (it1.hasNext() && it2.hasNext()) {
            boolean b1 = (it1.getDoubleNext()) > 0;
            final boolean b2 = (it2.getDoubleNext()) > 0;
            b1 = ((b1 && !b2) || (!b1 && b2));
            if (b1) {
                d++;
            }
        }
        return SimilarityTools.toSimilarity(d);
    }

    @Override
    public IArraySimilarity copy() {
        return new ArrayHamming();
    }
}
