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

import java.util.HashMap;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.MAVector;
import annotations.Configurable;

/**
 * 
 * FIXME requires citation of paper
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class ArrayTimePenalizedDot implements IArrayDoubleComp {

	private volatile HashMap<Array, MAVector> arrayCache = new HashMap<Array, MAVector>();
	private volatile ArrayCos dot = new ArrayCos();

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 2.0d;

	@Configurable(name = "maltcms.commands.distances.ArrayTimePenalizedDot.rtTolerance")
	private double rtTolerance = 50.0d;

	@Configurable(name = "maltcms.commands.distances.ArrayTimePenalizedDot.rtEpsilon")
	private double rtEpsilon = 0.01d;

	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		// MAVector mv1 = null;
		// MAVector mv2 = null;
		// if (this.arrayCache.containsKey(t1)) {
		// mv1 = this.arrayCache.get(t1);
		// } else {
		// mv1 = new MAVector(t1);
		// mv1.normalize();
		// this.arrayCache.put(t1, mv1);
		// }
		// if (this.arrayCache.containsKey(t2)) {
		// mv2 = this.arrayCache.get(t2);
		// } else {
		// mv2 = new MAVector(t2);
		// mv2.normalize();
		// this.arrayCache.put(t2, mv2);
		// }
		final double penalty = ((time1 == -1) || (time2 == -1)) ? 1.0d
		        : Math
		                .exp(-((time1 - time2) * (time1 - time2) / (2 * this.rtTolerance * this.rtTolerance)));
		if (penalty - this.rtEpsilon < 0) {
			return Double.NEGATIVE_INFINITY;
		}
		final double dotP = this.dot.apply(i1, i2, time1, time2, t1, t2);
		// Robinson
		return dotP * penalty;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.rtTolerance = cfg.getDouble(this.getClass().getName()
		        + ".rtTolerance", 50.0d);
		this.rtEpsilon = cfg.getDouble(
		        this.getClass().getName() + ".rtEpsilon", 0.01d);
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
		return false;
	}
}
