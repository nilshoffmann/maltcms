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
package maltcms.datastructures.cluster;

/**
 * Abstract base class for general, n-ary clusters.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public abstract class ACluster implements ICluster {

    private String name;
    private double distanceToParent = 0.0d;
    private String labelstring = "\\tlput";
    private int id;
    private int size = 1;
    private double[] dist;

    public double getDistanceTo(final ICluster bc) {
        return getDistanceTo(bc.getID());
    }

    public double getDistanceTo(final int i) {
        return this.dist[i];
    }

    public double getDistanceToParent() {
        return this.distanceToParent;
    }

    public int getID() {
        return this.id;
    }

    public String getLabelString() {
        return this.labelstring;
    }

    public String getName() {
        return this.name;
    }

    public int getSize() {
        return this.size;
    }

    public String printDistances() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ICluster " + getID() + " has distance to parent: "
            + getDistanceToParent());
        return sb.toString();
    }

    public void setDistances(final double[] d) {
        this.dist = d;
    }

    public void setDistanceTo(final ICluster bc, final double d) {
        setDistanceTo(bc.getID(), d);
    }

    public void setDistanceTo(final int i, final double d) {
        this.dist[i] = d;
    }

    public void setDistanceToParent(final double d) {
        this.distanceToParent = d;
    }

    public void setID(final int id1) {
        this.id = id1;
    }

    public void setLabelString(final String s) {
        this.labelstring = s;
    }

    public void setName(final ICluster lc, final ICluster rc) {
        this.name = "(" + lc.getName() + " " + rc.getName() + ")";
    }

    public void setName(final String name1) {
        this.name = name1;
    }

    public void setSize(final int size1) {
        this.size = size1;
    }
}
