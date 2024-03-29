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
import maltcms.tools.ArrayTools;
import net.jcip.annotations.NotThreadSafe;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.MAVector;

/**
 * Combinded distance of {@link maltcms.math.functions.similarities.ArrayCos} and {@link maltcms.math.functions.similarities.ArrayLp}.
 *
 * @author Mathias Wilhelm
 * 
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
@NotThreadSafe
public class ArrayCosLp implements IArraySimilarity {

    private final IArraySimilarity cos = new ArrayCos();
    private final IArraySimilarity lp = new ArrayLp();

    /** {@inheritDoc} */
    @Override
    public double apply(final Array t1, final Array t2) {
        final MAVector mav1 = new MAVector(t1);
        final MAVector mav2 = new MAVector(t2);
        final double n1 = mav1.norm();
        final double n2 = mav2.norm();
        final double cosd = this.cos.apply(t1, t2);
        final double lpd = this.lp.apply(ArrayTools.mult(
                t1, 1.0d / n1), ArrayTools.mult(t2, 1.0d / n2));
        final double dim = t1.getShape()[0];
        final double dist = (lpd / dim) * (1.0d - cosd);
        return SimilarityTools.toSimilarity(dist);
    }

    /** {@inheritDoc} */
    @Override
    public IArraySimilarity copy() {
        ArrayCosLp acl = new ArrayCosLp();
        return acl;
    }
}
