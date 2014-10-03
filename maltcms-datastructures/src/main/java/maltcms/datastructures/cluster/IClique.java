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
package maltcms.datastructures.cluster;

import java.util.List;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.array.IMutableFeatureVector;

/**
 * <p>IClique interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IClique<T extends IFeatureVector> {

    /**
     * <p>add.</p>
     *
     * @param p a T object.
     * @return a boolean.
     * @throws java.lang.IllegalArgumentException if any.
     */
    boolean add(T p) throws IllegalArgumentException;

    /**
     * <p>clear.</p>
     */
    void clear();

    /**
     * <p>getCliqueCentroid.</p>
     *
     * @return a T object.
     */
    T getCliqueCentroid();

    /**
     * <p>getFeatureVectorList.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<T> getFeatureVectorList();

    /**
     * <p>getID.</p>
     *
     * @return a long.
     */
    long getID();

    /**
     * <p>setCentroid.</p>
     *
     * @param ifv a T object.
     */
    void setCentroid(T ifv);

    /**
     * <p>size.</p>
     *
     * @return a int.
     */
    int size();

    /**
     * <p>getArrayStatsMap.</p>
     *
     * @return a {@link maltcms.datastructures.array.IMutableFeatureVector} object.
     */
    IMutableFeatureVector getArrayStatsMap();
}
