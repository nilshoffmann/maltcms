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
package maltcms.math.functions;

import java.io.Serializable;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public interface IScalarArraySimilarity extends Serializable {

    public double apply(double[] s1, double[] s2, Array a1, Array a2);

    public IScalarSimilarity[] getScalarSimilarities();

    public void setScalarSimilarities(IScalarSimilarity... scalarSimilarities);

    public IArraySimilarity[] getArraySimilarities();

    public void setArraySimilarities(IArraySimilarity... arraySimilarities);

    /**
     * Creates and returns a semantic deep copy of this similarity.
     *
     * @return
     */
    public IScalarArraySimilarity copy();
}
