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

import lombok.Data;
import maltcms.math.functions.IArraySimilarity;

import org.openide.util.lookup.ServiceProvider;

import ucar.ma2.Array;

/**
 * @author Nils Hoffmann
 *
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayDotLp implements IArraySimilarity {

    private final IArraySimilarity dot = new ArrayDot();
    private final IArraySimilarity lp = new ArrayLp();

    @Override
    public double apply(final Array t1, final Array t2) {
        final double dotv = this.dot.apply(t1, t2);
        final double d = this.lp.apply(t1, t2);
        return SimilarityTools.asSimilarity(d) * dotv;
    }
}
