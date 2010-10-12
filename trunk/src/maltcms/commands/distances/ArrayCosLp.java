/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
import ucar.ma2.MAVector;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * Combinded distance of {@link ArrayCos} and {@link ArrayLp}.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class ArrayCosLp implements IArrayDoubleComp {

	private final IArrayDoubleComp cos = new ArrayCos();
	private final IArrayDoubleComp lp = new ArrayLp();

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 1.0d;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		final MAVector mav1 = new MAVector(t1);
		final MAVector mav2 = new MAVector(t2);
		final double n1 = mav1.norm();
		final double n2 = mav2.norm();
		final double cosd = this.cos.apply(i1, i2, time1, time2, t1, t2);
		final double lpd = this.lp.apply(i1, i2, time1, time2, ArrayTools.mult(
		        t1, 1.0d / n1), ArrayTools.mult(t2, 1.0d / n2));
		final double dim = t1.getShape()[0];
		final double dist = (lpd / dim) * (1.0d - cosd);
		return dist;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 1.0d);
		StringBuilder sb = new StringBuilder();
		sb.append("wComp: " + this.wComp + ", ");
		sb.append("wExp: " + this.wExp + ", ");
		sb.append("wDiag: " + this.wDiag);
		Logging.getLogger(this).info("Parameters of class {}: {}",
		        this.getClass().getName(), sb.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCompressionWeight() {
		return this.wComp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDiagonalWeight() {
		return this.wDiag;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getExpansionWeight() {
		return this.wExp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean minimize() {
		return true;
	}

}
