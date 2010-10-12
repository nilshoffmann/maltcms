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

import maltcms.commands.scanners.ArrayStatsScanner;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import annotations.Configurable;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;

/**
 * Correlation as similarity between arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ArrayCorr implements IArrayDoubleComp {

	private final HashMap<Array, StatsMap> arrayCache = new HashMap<Array, StatsMap>();
	private final ArrayStatsScanner ass = new ArrayStatsScanner();
	private final ArrayDot dot = new ArrayDot();
	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 2.25d;

	@Configurable(name = "maltcms.commands.distances.ArrayCorr.returnCoeffDeterm")
	private boolean returnCoeffDetermination = false;

	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		StatsMap smt1 = null, smt2 = null;
		if (this.arrayCache.containsKey(t1)) {
			smt1 = this.arrayCache.get(t1);
		} else {
			final StatsMap[] sm = this.ass.apply(new Array[] { t1 });
			smt1 = sm[0];
			this.arrayCache.put(t1, smt1);
		}
		if (this.arrayCache.containsKey(t2)) {
			smt2 = this.arrayCache.get(t2);
		} else {
			final StatsMap[] sm = this.ass.apply(new Array[] { t2 });
			smt2 = sm[0];
			this.arrayCache.put(t2, smt2);
		}
		final double n = t1.getShape()[0];
		final double dotP = this.dot.apply(i1, i2, time1, time2, t1, t2);
		final double meant1 = smt1.get(Vars.Mean.toString());
		final double meant2 = smt2.get(Vars.Mean.toString());
		double corr = dotP - (n * meant1 * meant2);
		corr /= ((n - 1) * Math.sqrt(smt1.get(Vars.Variance.toString())) * (Math
		        .sqrt(smt2.get(Vars.Variance.toString()))));
		if (this.returnCoeffDetermination) {
			return Double.valueOf(corr * corr);
		}
		return Double.valueOf(corr);
	}

	@Override
	public void configure(final Configuration cfg) {
		this.returnCoeffDetermination = cfg
		        .getBoolean(
		                "maltcms.commands.distances.ArrayCorr.returnCoeffDeterm",
		                false);
		this.dot.configure(cfg);
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 2.25d);
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
