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

/**
 * 
 */
package maltcms.commands.distances;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import annotations.Configurable;

/**
 * @author hoffmann
 * 
 */
public class ArrayDotLp implements IArrayDoubleComp {

	private final IArrayDoubleComp dot = new ArrayDot();

	private final IArrayDoubleComp lp = new ArrayLp();

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 1.0d;

	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		final double dotv = this.dot.apply(i1, i2, time1, time2, t1, t2);
		// double deltatsq = 1+((time1-time2)*(time1-time2));
		// double deltassq = 1+((i1-i2)*(i1-i2));
		// double quot = deltatsq/deltassq;
		// dotv/=(Math.exp(-quot));
		// return dotv;
		final Double d = this.lp.apply(i1, i2, time1, time2, t1, t2);
		// double tsq = ((time1-time2)*(time1-time2));
		return d.doubleValue() * (1.0d - dotv);// *(tsq);
	}

	@Override
	public void configure(final Configuration cfg) {
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 1.0d);
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
