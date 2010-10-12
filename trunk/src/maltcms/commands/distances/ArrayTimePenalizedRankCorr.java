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

import java.util.WeakHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

import ucar.ma2.Array;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * Calculates Spearman's rank correlation as similarity between arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ArrayTimePenalizedRankCorr implements IArrayDoubleComp {

	private final WeakHashMap<Array, double[]> arrayCache = new WeakHashMap<Array, double[]>();
	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 2.25d;

	@Configurable
	private boolean returnCoeffDetermination = false;

	@Configurable
	private double rtTolerance = 50.0d;

	@Configurable
	private double rtEpsilon = 0.01d;

	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		// if no time is supplied, use 1 as default -> cosine/dot product
		// similarity
		final double weight = ((time1 == -1) || (time2 == -1)) ? 1.0d
		        : Math
		                .exp(-((time1 - time2) * (time1 - time2) / (2 * this.rtTolerance * this.rtTolerance)));
		// 1 for perfect time correspondence, 0 for really bad time
		// correspondence (towards infinity)
		if (weight - this.rtEpsilon < 0) {
			return Double.NEGATIVE_INFINITY;
		}
		SpearmansCorrelation sc = new SpearmansCorrelation();
		double[] t1a = null, t2a = null;
		if (arrayCache.containsKey(t1)) {
			t1a = arrayCache.get(t1);
		} else {
			t1a = (double[]) t1.get1DJavaArray(double.class);
			arrayCache.put(t1, t1a);
		}
		if (arrayCache.containsKey(t2)) {
			t2a = arrayCache.get(t2);
		} else {
			t2a = (double[]) t2.get1DJavaArray(double.class);
			arrayCache.put(t2, t2a);
		}
		double pcv = sc.correlation(t1a, t2a);
		if (this.returnCoeffDetermination) {
			return Double.valueOf(pcv * pcv);
		}
		return weight * Double.valueOf(pcv);
	}

	@Override
	public void configure(final Configuration cfg) {
		this.rtTolerance = cfg.getDouble(this.getClass().getName()
		        + ".rtTolerance", 50.0d);
		this.rtEpsilon = cfg.getDouble(
		        this.getClass().getName() + ".rtEpsilon", 0.01d);
		this.returnCoeffDetermination = cfg
		        .getBoolean(
		                "maltcms.commands.distances.ArrayCorr.returnCoeffDeterm",
		                false);
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 2.25d);
		StringBuilder sb = new StringBuilder();
		sb.append("returnCoeffDetermination: " + this.returnCoeffDetermination
		        + ", ");
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
		return false;
	}

}
