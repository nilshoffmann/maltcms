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
package maltcms.commands.distances.dtwng;

import java.util.List;
import maltcms.datastructures.IFileFragmentModifier;
import maltcms.datastructures.array.IFeatureVector;

/**
 * <p>IPairwiseFeatureVectorSequenceOperation interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IPairwiseFeatureVectorSequenceOperation<RESULT> extends
        IFileFragmentModifier {

    /**
     * <p>apply.</p>
     *
     * @param l1 a {@link java.util.List} object.
     * @param l2 a {@link java.util.List} object.
     * @return a RESULT object.
     */
    public abstract RESULT apply(List<IFeatureVector> l1,
            List<IFeatureVector> l2);

    /**
     * <p>setPairwiseFeatureVectorOperation.</p>
     *
     * @param pao a {@link maltcms.commands.distances.dtwng.TwoFeatureVectorOperation} object.
     */
    public abstract void setPairwiseFeatureVectorOperation(
            TwoFeatureVectorOperation pao);

    /**
     * <p>getPairwiseFeatureVectorOperation.</p>
     *
     * @return a {@link maltcms.commands.distances.dtwng.TwoFeatureVectorOperation} object.
     */
    public TwoFeatureVectorOperation getPairwiseFeatureVectorOperation();
}
