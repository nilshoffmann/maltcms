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
package net.sf.maltcms.evaluation.spi.classification;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class PeakNameFeatureVector implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -5936343655074144856L;
    private final String name;
    private final UUID uniqueId = UUID.randomUUID();

    public PeakNameFeatureVector(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    @Override
    public Array getFeature(String name) {
        if (name.equals("NAME")) {
            return ArrayChar.makeFromString(name, 2048);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList("NAME");
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NAME = " + getName());
        return sb.toString();
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
  
}
