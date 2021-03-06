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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import maltcms.math.functions.IScalarArraySimilarity;

/**
 * <p>IWorkerFactory interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IWorkerFactory extends Serializable {

    /**
     * <p>create.</p>
     *
     * @param outputDirectory a {@link java.io.File} object.
     * @param input a {@link cross.datastructures.tuple.TupleND} object.
     * @param fragmentToPeaks a {@link java.util.Map} object.
     * @return a {@link java.util.List} object.
     */
    List<Callable<PairwiseSimilarityResult>> create(File outputDirectory, TupleND<IFileFragment> input, Map<String, List<IBipacePeak>> fragmentToPeaks);

    /**
     * <p>setSimilarityFunction.</p>
     *
     * @param similarityFunction a {@link maltcms.math.functions.IScalarArraySimilarity} object.
     * @since 1.3.2
     */
    void setSimilarityFunction(IScalarArraySimilarity similarityFunction);

    /**
     * <p>setAssumeSymmetricSimilarity.</p>
     *
     * @param assumeSymmetricSimilarity a boolean.
     * @since 1.3.2
     */
    void setAssumeSymmetricSimilarity(boolean assumeSymmetricSimilarity);

    /**
     * <p>setSavePeakSimilarities.</p>
     *
     * @param savePeakSimilarities a boolean.
     * @since 1.3.2
     */
    void setSavePeakSimilarities(boolean savePeakSimilarities);

    /**
     * <p>getSimilarityFunction.</p>
     *
     * @return a {@link maltcms.math.functions.IScalarArraySimilarity} object.
     * @since 1.3.2
     */
    IScalarArraySimilarity getSimilarityFunction();

    /**
     * <p>isAssumeSymmetricSimilarity.</p>
     *
     * @return a boolean.
     * @since 1.3.2
     */
    boolean isAssumeSymmetricSimilarity();

    /**
     * <p>isSavePeakSimilarities.</p>
     *
     * @return a boolean.
     * @since 1.3.2
     */
    boolean isSavePeakSimilarities();

}
