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
package maltcms.db.predicates.metabolite;

import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import maltcms.datastructures.ms.IMetabolite;

/**
 * <p>MSimilarityPredicate class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class MSimilarityPredicate extends MetabolitePredicate {

    /**
     *
     */
    private static final long serialVersionUID = -3684834963267981958L;
    private final MetaboliteSimilarity s;

    /**
     * <p>Constructor for MSimilarityPredicate.</p>
     *
     * @param s a {@link maltcms.db.predicates.metabolite.MetaboliteSimilarity} object.
     */
    public MSimilarityPredicate(MetaboliteSimilarity s) {
        this.s = s;
    }

    /**
     * <p>getSimilaritiesAboveThreshold.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Tuple2D<Double, IMetabolite>> getSimilaritiesAboveThreshold() {
        return this.s.getMatches();
    }

    /**
     * <p>resetResultList.</p>
     */
    public void resetResultList() {
        this.s.getMatches().clear();
    }

    /** {@inheritDoc} */
    @Override
    public boolean match(IMetabolite arg0) {
        return this.s.match(arg0);
    }
}
