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

/**
 * Abstract base class for general, n-ary clusters.
 *
 * @author Nils Hoffmann
 * 
 */
public abstract class ACluster implements ICluster {

    private String name;
    private double distanceToParent = 0.0d;
    private String labelstring = "\\tlput";
    private int id;
    private int size = 1;
    private double[] dist;

    /**
     * <p>getDistanceTo.</p>
     *
     * @param bc a {@link maltcms.datastructures.cluster.ICluster} object.
     * @return a double.
     */
    public double getDistanceTo(final ICluster bc) {
        return getDistanceTo(bc.getID());
    }

    /**
     * <p>getDistanceTo.</p>
     *
     * @param i a int.
     * @return a double.
     */
    public double getDistanceTo(final int i) {
        return this.dist[i];
    }

    /** {@inheritDoc} */
    @Override
    public double getDistanceToParent() {
        return this.distanceToParent;
    }

    /** {@inheritDoc} */
    @Override
    public int getID() {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public String getLabelString() {
        return this.labelstring;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return this.size;
    }

    /**
     * <p>printDistances.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String printDistances() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ICluster " + getID() + " has distance to parent: "
                + getDistanceToParent());
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void setDistances(final double[] d) {
        this.dist = d;
    }

    /**
     * <p>setDistanceTo.</p>
     *
     * @param bc a {@link maltcms.datastructures.cluster.ICluster} object.
     * @param d a double.
     */
    public void setDistanceTo(final ICluster bc, final double d) {
        setDistanceTo(bc.getID(), d);
    }

    /**
     * <p>setDistanceTo.</p>
     *
     * @param i a int.
     * @param d a double.
     */
    public void setDistanceTo(final int i, final double d) {
        this.dist[i] = d;
    }

    /** {@inheritDoc} */
    @Override
    public void setDistanceToParent(final double d) {
        this.distanceToParent = d;
    }

    /** {@inheritDoc} */
    @Override
    public void setID(final int id1) {
        this.id = id1;
    }

    /** {@inheritDoc} */
    @Override
    public void setLabelString(final String s) {
        this.labelstring = s;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param lc a {@link maltcms.datastructures.cluster.ICluster} object.
     * @param rc a {@link maltcms.datastructures.cluster.ICluster} object.
     */
    public void setName(final ICluster lc, final ICluster rc) {
        this.name = "(" + lc.getName() + " " + rc.getName() + ")";
    }

    /** {@inheritDoc} */
    @Override
    public void setName(final String name1) {
        this.name = name1;
    }

    /** {@inheritDoc} */
    @Override
    public void setSize(final int size1) {
        this.size = size1;
    }
}
