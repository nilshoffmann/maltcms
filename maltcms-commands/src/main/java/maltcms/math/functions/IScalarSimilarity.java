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

/**
 * The implemented similarity function should have the following properties: The
 * maximal function value must be greater than the minimal function value,
 * assuming that a similarity between two scalars is maximal iff both entities
 * are identical/equal and minimal iff they are completely unrelated and that
 * increasing similarity is reflected by a greater value of the function. Note
 * that -Inf is reserved for special cases, where the similarity is not
 * determinable or was not calculated due to an unmet threshold criterion.
 *
 * @author Nils Hoffmann
 */
public interface IScalarSimilarity extends Serializable {

    public double apply(double a, double b);

    /**
     * Creates and returns a semantic deep copy of this similarity.
     *
     * @return
     */
    public IScalarSimilarity copy();
}
