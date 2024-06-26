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
package net.sf.maltcms.evaluation.api.alignment;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * <p>AlignmentColumn class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class AlignmentColumn implements IFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -5936343655074144856L;
    private final ArrayDouble.D1 values;
    private final UUID uniqueId = UUID.randomUUID();

    /**
     * <p>Constructor for AlignmentColumn.</p>
     *
     * @param rt a double.
     */
    public AlignmentColumn(double... rt) {
        this.values = (ArrayDouble.D1) Array.factory(rt);
    }

    /*
     * (non-Javadoc) @see
     * maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public Array getFeature(String name) {
        if (name.equals("RT")) {
            return this.values;
        }
        return null;
    }

    /*
     * (non-Javadoc) @see
     * maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList("RT");
    }

    /**
     * <p>getRT.</p>
     *
     * @return a {@link ucar.ma2.ArrayDouble.D1} object.
     */
    public ArrayDouble.D1 getRT() {
        return this.values;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RT = " + getRT());
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

}
