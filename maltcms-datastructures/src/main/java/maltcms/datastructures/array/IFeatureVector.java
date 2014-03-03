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
package maltcms.datastructures.array;

import cross.annotations.NoFeature;
import cross.exception.ResourceNotAvailableException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public interface IFeatureVector extends Serializable {

    /**
     * Return an Array for a specified feature with name. This method should only
     * be used when the implementation type of a feature vector is unknown. Implementations
     * of IFeatureVector should provide more convenient getter methods to access feature values.
     *
     * @param name the name of the feature to retrieve
     * @return the array of the requested feature
     * @throws ResourceNotAvailableException if a resource with the given name is available but has not been initialized
     * @throws IllegalArgumentException      if a resource with the given name is unknown to this feature vector
     */
    @NoFeature
    public abstract Array getFeature(String name) throws ResourceNotAvailableException, IllegalArgumentException;

    /**
     * Get a list of available feature names for this FeatureVector.
     *
     * @return the list of known and available feature names
     */
    @NoFeature
    public List<String> getFeatureNames();

    /**
     * Returns the unique id of this feature vector.
     *
     * @return the unique id
     */
    public UUID getUniqueId();
}
