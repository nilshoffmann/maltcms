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
 * $Id$
 */

package maltcms.commands.distances;

import maltcms.tools.ArrayTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.MAMath;
import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * Lp-norm based distance between arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ArrayLp implements IArrayDoubleComp {

	@Configurable(name = "maltcms.commands.distances.ArrayLp.type")
	private double p = 2.0d;

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 1.0d;
	@Configurable(name = "normalizeByLength")
	private boolean normalizeByLength = true;

	private double sqrtn = Double.NaN;

	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		if (MAMath.conformable(t1, t2)) {
			// if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
			final double val = Math.pow(MAMath.sumDouble(ArrayTools.pow(
			        ArrayTools.diff(t1, t2), 2.0d)), 1.0d / this.p);
			if (this.normalizeByLength) {
				if (Double.isNaN(this.sqrtn)) {
					this.sqrtn = Math.sqrt(t1.getShape()[0]);
				}
				return val / this.sqrtn;
			} else {
				return val;
			}
			// return MAMath.sumDouble(ArrayTools.pow(ArrayTools
			// .diff(t1, t2), 2.0d));
			// } else {
			// throw new NotImplementedException();
			// }
		} else {
			throw new IllegalArgumentException(
			        "Arrays shapes are incompatible: LHS=" + t1.getShape()[0]
			                + " RHS=" + t2.getShape()[0]);
		}
	}

	@Override
	public void configure(final Configuration cfg) {
		this.p = Factory.getInstance().getConfiguration().getDouble(
		        this.getClass().getName() + ".type", 2.0d);
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 2.0d);
		this.normalizeByLength = cfg.getBoolean(this.getClass().getName()
		        + ".normalizeByLength", true);
		StringBuilder sb = new StringBuilder();
		sb.append("p: " + this.p + ", ");
		sb.append("normalizeByLength: " + this.normalizeByLength + ", ");
		sb.append("wComp: " + this.wComp + ", ");
		sb.append("wExp: " + this.wExp + ", ");
		sb.append("wDiag: " + this.wDiag);
		Logging.getLogger(this).info("Parameters of class {}: {}",
		        this.getClass().getName(), sb.toString());
	}

	public double getCompressionWeight() {
		return this.wComp;
	}

	public double getDiagonalWeight() {
		return this.wDiag;
	}

	public double getExpansionWeight() {
		return this.wExp;
	}

	@Override
	public boolean minimize() {
		return true;
	}

}
