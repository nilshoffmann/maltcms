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

import cross.exception.ResourceNotAvailableException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.sf.maltcms.evaluation.api.classification.IRowIndexNamedPeakFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * <p>Peak2DFeatureVector class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class Peak2DFeatureVector implements IRowIndexNamedPeakFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -5936343655074144856L;
    private final double rt1, rt2, area;
    private final int rowIndex;
    private final String name;
    private final UUID uniqueId;

    public enum FEATURE {

        RT1, RT2, NAME, ROWINDEX, AREA
    };

    /**
     * <p>Constructor for Peak2DFeatureVector.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param rowIndex a int.
     * @param rt1 a double.
     * @param rt2 a double.
     * @param area a double.
     */
    public Peak2DFeatureVector(String name, int rowIndex, double rt1, double rt2, double area) {
        this.name = name;
        this.rt1 = rt1;
        this.rt2 = rt2;
        this.rowIndex = rowIndex;
        this.area = area;
        this.uniqueId = UUID.nameUUIDFromBytes((name + "-" + rowIndex + "-" + rt1 + "-" + rt2 + "-" + area).getBytes());
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public Array getFeature(String name) {
        FEATURE f = FEATURE.valueOf(name);
        switch (f) {
            case RT1: {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(rt1);
                return a;
            }
            case RT2: {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(rt2);
                return a;
            }
            case AREA: {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(area);
                return a;
            }
            case ROWINDEX: {
                ArrayInt.D0 a = new ArrayInt.D0();
                a.set(rowIndex);
                return a;
            }
            case NAME: {
                ArrayChar.D1 a = new ArrayChar.D1(name.length());
                a.setString(name);
                return a;
            }
        }
        throw new ResourceNotAvailableException("No such feature: " + name);
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        List<String> l = new LinkedList<>();
        for (FEATURE f : FEATURE.values()) {
            l.add(f.name());
        }
        return l;
    }

    /**
     * <p>getRT1.</p>
     *
     * @return a double.
     */
    public double getRT1() {
        return rt1;
    }

    /**
     * <p>getRT2.</p>
     *
     * @return a double.
     */
    public double getRT2() {
        return rt2;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    /** {@inheritDoc} */
    @Override
    public double getArea() {
        return area;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName() + "{");
        sb.append("NAME = " + getName() + "; ");
        sb.append("ROWINDEX = " + getRowIndex() + "; ");
        sb.append("AREA = " + getArea() + "; ");
        sb.append("RT1 = " + getRT1() + "; ");
        sb.append("RT2 = " + getRT2() + "}");
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
}
