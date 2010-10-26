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
 * $Id: RetentionInfo.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package maltcms.datastructures.ms;

/**
 * Concrete implementation of {@link maltcms.datastructures.ms.IRetentionInfo}.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class RetentionInfo implements IRetentionInfo {

	private double ri = -1;

	private double rt = -1;

	private String rtu = "seconds";

	private String name = "";

	private int scanIndex = -1;

	public int compareTo(final IAnchor o) {
		if (o instanceof IRetentionInfo) {
			// If no retention index is given, use names as comparison criterion
			if ((((IRetentionInfo) o).getRetentionIndex() < 0)
			        || (getRetentionIndex() < 0)) {
				return getName().compareTo(o.getName());
			}
			// if retention index is given
			if (((IRetentionInfo) o).getRetentionIndex() > getRetentionIndex()) {
				return -1;
			}
			if (((IRetentionInfo) o).getRetentionIndex() < getRetentionIndex()) {
				return 1;
			}
			return 0;
		}
		return toString().compareTo(o.toString());
	}

	public String getName() {
		return this.name;
	}

	public double getRetentionIndex() {
		return this.ri;
	}

	public double getRetentionTime() {
		return this.rt;
	}

	public String getRetentionTimeUnit() {
		return this.rtu;
	}

	public int getScanIndex() {
		return this.scanIndex;
	}

	public void setName(final String s) {
		this.name = s;
	}

	public void setRetentionIndex(final double d) {
		this.ri = d;
	}

	public void setRetentionTime(final double d) {
		this.rt = d;
	}

	public void setRetentionTimeUnit(final String s) {
		this.rtu = s;
	}

	public void setScanIndex(final int scan) {
		this.scanIndex = scan;
	}

	@Override
	public String toString() {
		if (getRetentionIndex() <= 0) {
			return "" + getName();
		} else {
			return "" + getRetentionIndex();
		}
	}

}
