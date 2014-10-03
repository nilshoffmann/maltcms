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
package net.sf.maltcms.evaluation.spi.classification;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.sf.maltcms.evaluation.api.classification.INamedPeakFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;

/**
 * <p>PeakRTFeatureVector class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class PeakRTFeatureVector implements INamedPeakFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -5936343655074144856L;
    private final ArrayDouble.D0 rt, area;
    private final String name;
    private final UUID uniqueId;

    /**
     * <p>Constructor for PeakRTFeatureVector.</p>
     *
     * @param rt a double.
     * @param area a double.
     */
    public PeakRTFeatureVector(double rt, double area) {
        this(null, rt, area);
    }

    /**
     * <p>Constructor for PeakRTFeatureVector.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param rt a double.
     * @param area a double.
     */
    public PeakRTFeatureVector(String name, double rt, double area) {
        this.name = name;
        this.rt = new ArrayDouble.D0();
        this.rt.set(rt);
        this.area = new ArrayDouble.D0();
        this.area.set(area);
        this.uniqueId = UUID.nameUUIDFromBytes((name + "-" + rt + "-" + area).getBytes());
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public Array getFeature(String name) {
        if (name.equals("RT")) {
            return this.rt;
        }
        if (name.equals("NAME")) {
            ArrayChar.D1 a = new ArrayChar.D1(name.length());
            a.setString(name);
            return a;
        }
        if (name.equals("AREA")) {
            return this.area;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        return Arrays.asList("RT", "NAME", "AREA");
    }

    /**
     * <p>getRT.</p>
     *
     * @return a double.
     */
    public double getRT() {
        return this.rt.get();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public double getArea() {
        return this.area.get();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName() + "{");
        sb.append("RT = " + getRT() + "; ");
        sb.append("AREA = " + getArea() + "; ");
        sb.append("NAME = " + getName() + "}");
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
}
