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
package net.sf.maltcms.evaluation.spi.xcalibur;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import maltcms.datastructures.array.IFeatureVector;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;

/**
 * <p>Peak class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class Peak implements IFeatureVector {

    private Chromatogram parent;
    private String name = "";
    private String creatorname = "";
    private double rt = Double.NaN;
    private double rtstart = rt;
    private double rtstop = rt;
    private double[] mw = new double[]{};
    private double area = 0;
    private double height = 0;
    /** Constant <code>AREA_DEFAULT=0</code> */
    public static final double AREA_DEFAULT = 0;
    /** Constant <code>HEIGHT_DEFAULT=0</code> */
    public static final double HEIGHT_DEFAULT = 0;
    /** Constant <code>RT_DEFAULT=Double.NaN</code> */
    public static final double RT_DEFAULT = Double.NaN;
    /** Constant <code>RT_START_DEFAULT=Double.NaN</code> */
    public static final double RT_START_DEFAULT = Double.NaN;
    /** Constant <code>RT_STOP_DEFAULT=Double.NaN</code> */
    public static final double RT_STOP_DEFAULT = Double.NaN;
    private final UUID uniqueId = UUID.randomUUID();

    /**
     * <p>Constructor for Peak.</p>
     *
     * @param creator a {@link net.sf.maltcms.evaluation.spi.xcalibur.Creator} object.
     * @param parent a {@link net.sf.maltcms.evaluation.spi.xcalibur.Chromatogram} object.
     * @param name a {@link java.lang.String} object.
     * @param rt a double.
     * @param rtstart a double.
     * @param rtstop a double.
     * @param mw an array of double.
     * @param area a double.
     * @param height a double.
     * @param unit a {@link net.sf.maltcms.evaluation.spi.xcalibur.RTUnit} object.
     */
    public Peak(Creator creator, Chromatogram parent, String name, double rt, double rtstart, double rtstop, double[] mw, double area, double height, RTUnit unit) {
        this.parent = parent;
        this.name = name;
        this.creatorname = creator.toString();
        this.mw = mw;
        this.area = area;
        this.height = height;
        switch (unit) {
            case Hours: {
                this.rt = rt * (60.0 * 60.0);
                this.rtstart = rtstart * (60.0 * 60.0);
                this.rtstop = rtstop * (60.0 * 60.0);
                break;
            }
            case Minutes: {
                this.rt = rt * (60.0);
                this.rtstart = rtstart * (60.0);
                this.rtstop = rtstop * (60.0);
                break;

            }
            case Seconds: {
                this.rt = rt;
                this.rtstart = rtstart;
                this.rtstop = rtstop;
                break;
            }

        }
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeature(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public Array getFeature(String name) {
        switch (name) {
            case "RT": {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(this.rt);
                return a;
            }
            case "RTSTART": {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(this.rtstart);
                return a;
            }
            case "RTSTOP": {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(this.rtstop);
                return a;
            }
            case "AREA": {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(this.area);
                return a;
            }
            case "HEIGHT": {
                ArrayDouble.D0 a = new ArrayDouble.D0();
                a.set(this.height);
                return a;
            }
            case "MW": {
                ArrayDouble.D1 a = new ArrayDouble.D1(this.mw.length);
                for (int i = 0; i < this.mw.length; i++) {
                    a.set(i, this.mw[i]);
                }
                return a;
            }
            case "FILE": {
                ArrayChar.D1 a = new ArrayChar.D1(this.parent.getName().length());
                a.setString(this.parent.getName());
                return a;
            }
            case "NAME": {
                ArrayChar.D1 a = new ArrayChar.D1(this.name.length());
                a.setString(this.name);
                return a;
            }
            case "CREATORNAME": {
                ArrayChar.D1 a = new ArrayChar.D1(this.creatorname.length());
                a.setString(this.creatorname);
                return a;
            }
        }

        throw new IllegalArgumentException("Feature with name " + name + " not supported!");
    }

    /* (non-Javadoc)
     * @see maltcms.datastructures.array.IFeatureVector#getFeatureNames()
     */
    /** {@inheritDoc} */
    @Override
    public List<String> getFeatureNames() {
        final String[] names = new String[]{"RT", "RTSTART", "RTSTOP", "AREA", "HEIGHT", "MW", "FILE", "NAME", "CREATORNAME"};
        return Arrays.asList(names);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : getFeatureNames()) {
            sb.append(s + ": " + getFeature(s) + "\t");
        }
        return sb.toString();
    }

    /**
     * <p>Getter for the field <code>parent</code>.</p>
     *
     * @return a {@link net.sf.maltcms.evaluation.spi.xcalibur.Chromatogram} object.
     */
    public Chromatogram getParent() {
        return parent;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Getter for the field <code>creatorname</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCreatorname() {
        return creatorname;
    }

    /**
     * <p>Getter for the field <code>rt</code>.</p>
     *
     * @return a double.
     */
    public double getRt() {
        return rt;
    }

    /**
     * <p>Getter for the field <code>rtstart</code>.</p>
     *
     * @return a double.
     */
    public double getRtstart() {
        return rtstart;
    }

    /**
     * <p>Getter for the field <code>rtstop</code>.</p>
     *
     * @return a double.
     */
    public double getRtstop() {
        return rtstop;
    }

    /**
     * <p>Getter for the field <code>mw</code>.</p>
     *
     * @return an array of double.
     */
    public double[] getMw() {
        return mw;
    }

    /**
     * <p>Getter for the field <code>area</code>.</p>
     *
     * @return a double.
     */
    public double getArea() {
        return area;
    }

    /**
     * <p>Getter for the field <code>height</code>.</p>
     *
     * @return a double.
     */
    public double getHeight() {
        return height;
    }

    /** {@inheritDoc} */
    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

}
