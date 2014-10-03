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
import java.awt.geom.Area;
import java.util.List;

/**
 * <p>IAlignment interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IAlignment extends
        IPairwiseFeatureVectorSequenceOperation<Double> {

    /**
     * <p>getMap.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Point> getMap();

    /**
     * <p>getLeftHandSideId.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLeftHandSideId();

    /**
     * <p>getRightHandSideId.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRightHandSideId();

    /**
     * <p>setRightHandSideId.</p>
     *
     * @param rhsid a {@link java.lang.String} object.
     */
    public void setRightHandSideId(String rhsid);

    /**
     * <p>setLeftHandSideId.</p>
     *
     * @param lhsid a {@link java.lang.String} object.
     */
    public void setLeftHandSideId(String lhsid);

    /**
     * <p>setConstraints.</p>
     *
     * @param a a {@link java.awt.geom.Area} object.
     */
    public void setConstraints(Area a);

    /**
     * <p>getConstraints.</p>
     *
     * @return a {@link java.awt.geom.Area} object.
     */
    public Area getConstraints();

    /**
     * <p>setDefaultValue.</p>
     *
     * @param d a double.
     */
    public void setDefaultValue(double d);

    /**
     * <p>getDefaultValue.</p>
     *
     * @return a double.
     */
    public double getDefaultValue();

    /**
     * <p>setOptimizationFunction.</p>
     *
     * @param iof a {@link maltcms.commands.distances.dtwng.IOptimizationFunction} object.
     */
    public void setOptimizationFunction(IOptimizationFunction iof);

    /**
     * <p>getOptimizationFunction.</p>
     *
     * @return a {@link maltcms.commands.distances.dtwng.IOptimizationFunction} object.
     */
    public IOptimizationFunction getOptimizationFunction();
}
