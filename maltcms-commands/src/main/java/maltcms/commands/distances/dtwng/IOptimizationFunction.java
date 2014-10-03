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

import java.awt.Point;
import java.util.List;
import maltcms.datastructures.IFileFragmentModifier;
import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.array.IFeatureVector;

/**
 * <p>IOptimizationFunction interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IOptimizationFunction extends IFileFragmentModifier {

    /**
     * <p>init.</p>
     *
     * @param l a {@link java.util.List} object.
     * @param r a {@link java.util.List} object.
     * @param cumulatedScores a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param pwScores a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param tfvo a {@link maltcms.commands.distances.dtwng.TwoFeatureVectorOperation} object.
     */
    public abstract void init(List<IFeatureVector> l, List<IFeatureVector> r,
            IArrayD2Double cumulatedScores, IArrayD2Double pwScores,
            TwoFeatureVectorOperation tfvo);

    /**
     * <p>apply.</p>
     *
     * @param is a int.
     */
    public abstract void apply(int... is);

    /**
     * <p>getTrace.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<Point> getTrace();

    /**
     * <p>getOptimalOperationSequenceString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getOptimalOperationSequenceString();

    /**
     * <p>getStates.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public abstract String[] getStates();

    /**
     * <p>setWeight.</p>
     *
     * @param state a {@link java.lang.String} object.
     * @param d a double.
     */
    public abstract void setWeight(String state, double d);

    /**
     * <p>getWeight.</p>
     *
     * @param state a {@link java.lang.String} object.
     * @return a double.
     */
    public abstract double getWeight(String state);

    /**
     * <p>getOptimalValue.</p>
     *
     * @return a double.
     */
    public abstract double getOptimalValue();
}
