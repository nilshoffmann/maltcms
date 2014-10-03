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
 * <p>IScalarArraySimilarity interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IScalarArraySimilarity extends Serializable {

    /**
     * <p>apply.</p>
     *
     * @param s1 an array of double.
     * @param s2 an array of double.
     * @param a1 a {@link ucar.ma2.Array} object.
     * @param a2 a {@link ucar.ma2.Array} object.
     * @return a double.
     */
    public double apply(double[] s1, double[] s2, Array a1, Array a2);

    /**
     * <p>getScalarSimilarities.</p>
     *
     * @return an array of {@link maltcms.math.functions.IScalarSimilarity} objects.
     */
    public IScalarSimilarity[] getScalarSimilarities();

    /**
     * <p>setScalarSimilarities.</p>
     *
     * @param scalarSimilarities a {@link maltcms.math.functions.IScalarSimilarity} object.
     */
    public void setScalarSimilarities(IScalarSimilarity... scalarSimilarities);

    /**
     * <p>getArraySimilarities.</p>
     *
     * @return an array of {@link maltcms.math.functions.IArraySimilarity} objects.
     */
    public IArraySimilarity[] getArraySimilarities();

    /**
     * <p>setArraySimilarities.</p>
     *
     * @param arraySimilarities a {@link maltcms.math.functions.IArraySimilarity} object.
     */
    public void setArraySimilarities(IArraySimilarity... arraySimilarities);

    /**
     * Creates and returns a semantic deep copy of this similarity.
     *
     * @return a {@link maltcms.math.functions.IScalarArraySimilarity} object.
     * @since 1.3.2
     */
    public IScalarArraySimilarity copy();
}
