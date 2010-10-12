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

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.Logging;

/**
 * Time penalized Dot/Cos distance.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class Array2DTimePenalized implements IArrayDoubleComp {

	private volatile IArrayDoubleComp sim = new ArrayCos();

	private double wExp = 1.0d;
	private double wComp = 1.0d;
	private double wDiag = 2.0d;

	private double frtTolerance = 25.0d;
	private double srtTolerance = 0.5d;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double apply(final int i1, final int i2, final double time1,
			final double time2, final Array t1, final Array t2) {
		final double dotP = this.sim.apply(0, 0, 0, 0, t1, t2);
		double frtPenalty = 1.0d;
		if (time1 != 0.0d) {
			frtPenalty = Math
					.exp(-((time1) * (time1) / (2 * this.frtTolerance * this.frtTolerance)));
		}
		double srtPenalty = 1.0d;
		if (time2 != 0.0d) {
			srtPenalty = Math
					.exp(-((time2) * (time2) / (2 * this.srtTolerance * this.srtTolerance)));
		}
		double s = dotP * frtPenalty * srtPenalty;
		return s;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.frtTolerance = cfg.getDouble(this.getClass().getName()
				+ ".frtTolerance", 50.0d);
		this.srtTolerance = cfg.getDouble(this.getClass().getName()
				+ ".srtTolerance", 50.0d);
		this.wComp = cfg.getDouble(this.getClass().getName()
				+ ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
				+ ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
				+ ".diagonal_weight", 1.0d);
		StringBuilder sb = new StringBuilder();
		sb.append("frtTolerance: " + this.frtTolerance + ", ");
		sb.append("srtTolerance: " + this.srtTolerance + ", ");
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
		return false;
	}

	public void setFirstTolerance(double v) {
		this.frtTolerance = Math.abs(v);
	}

	public void setSEcondTolerance(double v) {
		this.srtTolerance = Math.abs(v);
	}
}
