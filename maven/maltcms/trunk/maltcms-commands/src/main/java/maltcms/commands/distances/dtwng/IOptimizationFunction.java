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
package maltcms.commands.distances.dtwng;

import java.awt.Point;
import java.util.List;

import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.IFileFragmentModifier;

/**
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public interface IOptimizationFunction extends IFileFragmentModifier {

    public abstract void init(List<IFeatureVector> l, List<IFeatureVector> r,
            IArrayD2Double cumulatedScores, IArrayD2Double pwScores,
            TwoFeatureVectorOperation tfvo);

    public abstract void apply(int... is);

    public abstract List<Point> getTrace();

    public abstract String getOptimalOperationSequenceString();

    public abstract String[] getStates();

    public abstract void setWeight(String state, double d);

    public abstract double getWeight(String state);

    public abstract double getOptimalValue();
}
