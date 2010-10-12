/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: ACluster.java 43 2009-10-16 17:22:55Z nilshoffmann $
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
