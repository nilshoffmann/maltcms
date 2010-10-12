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

import maltcms.datastructures.array.IArrayD2Double;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import annotations.Configurable;
import cross.Logging;
import cross.tools.MathTools;

/**
 * Objects of this type are used to calculate the cumulative distance within the
 * {@link maltcms.commands.distances.dtw.ADynamicTimeWarp} Implementations.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class CumulativeDistance implements IRecurrence {

	@Configurable(name = "alignment.algorithm.compressionweight")
	private double comp_weight = 1.0d;

	@Configurable(name = "alignment.algorithm.expansionweight")
	private double exp_weight = 1.0d;

	@Configurable(name = "alignment.algorithm.diagonalweight")
	private double diag_weight = 1.0d;

	private boolean minimize = true;

	private final String[] directions = new String[] { "Left", "Diag", "Up" };

	private final Logger log = Logging.getLogger(this);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
	 * )
	 */
	@Override
	public void configure(final Configuration cfg) {
		this.comp_weight = cfg.getDouble(
		        "alignment.algorithm.compressionweight", 1.0);
		this.exp_weight = cfg.getDouble("alignment.algorithm.expansionweight",
		        1.0);
		this.diag_weight = cfg.getDouble("alignment.algorithm.diagonalweight",
		        1.0);

	}

	private double cumDistM(final int row, final int column,
	        final IArrayD2Double cumDistMatrix, final double cij,
	        final boolean minimize1, final int[][] predecessors) {
		final double init = minimize1 ? Double.POSITIVE_INFINITY
		        : Double.NEGATIVE_INFINITY;
		double n = init, w = init, nw = init;
		if ((row == 0) && (column == 0)) {
			nw = this.diag_weight * cij;
			predecessors[row][column] = 0;
			// System.out.println("cij = "+cij + ", nw= "+nw);
			cumDistMatrix.set(row, column, nw);
			// this.log.debug(
			// "Case 1: First column, first row ={}, best from Diag", nw);
			return nw;
		} else if ((row > 0) && (column == 0)) {
			n = ((this.comp_weight * cij) + cumDistMatrix.get(row - 1, 0));
			// System.out.println("i="+row+" j="+column+" cij = "+cij +
			// ", n= "+n);
			cumDistMatrix.set(row, column, n);
			predecessors[row][column] = 1;
			// this.log.debug("Case 2: First column, row {}={}, best from Up",
			// row, n);
			return n;
		} else if ((row == 0) && (column > 0)) {
			w = ((this.exp_weight * cij) + cumDistMatrix.get(0, column - 1));
			// System.out.println("i="+row+" j="+column+" cij = "+cij +
			// ", w= "+w);
			cumDistMatrix.set(row, column, w);
			predecessors[row][column] = -1;
			// this.log.debug("Case 3: First row, column {}={}, best from Left",
			// column, w);
			return w;
		} else { // all other cases
			n = (this.comp_weight * cij) + cumDistMatrix.get(row - 1, column);
			nw = (this.diag_weight * cij)
			        + cumDistMatrix.get(row - 1, column - 1);
			w = (this.exp_weight * cij) + cumDistMatrix.get(row, column - 1);
			final int neq = nequal(n, nw, w);
			// if (neq > 0) {
			// this.log.debug("{} values are equal at {},{}", new Object[] {
			// neq, row, column });
			// this.log.debug("n={},nw={},w={}", new Object[] { n, nw, w });
			// }
			final double m = minimize1 ? MathTools.min(n, w, nw) : MathTools
			        .max(n, w, nw);
			if (m == nw) {
				predecessors[row][column] = 0;
			} else if (m == n) {
				predecessors[row][column] = 1;
			} else if (m == w) {
				predecessors[row][column] = -1;
			}
			cumDistMatrix.set(row, column, m);
			// if (minimize1) {
			// this.log
			// .debug(
			// "Case 4: common element: min(n={},nw={},w={})={}, best from {}",
			// new Object[] {
			// n,
			// nw,
			// w,
			// m,
			// this.directions[predecessors[row][column] + 1] });
			// } else {
			// this.log
			// .debug(
			// "Case 4: common element: max(n={},nw={},w={})={}, best from {}",
			// new Object[] {
			// n,
			// nw,
			// w,
			// m,
			// this.directions[predecessors[row][column] + 1] });
			// }
			return m;
		}
	}

	private double cumDistM(final int row, final int column,
	        final IArrayD2Double prev, final IArrayD2Double curr,
	        final double cij, final boolean minimize1) {
		double m = cij;
		final double init = minimize1 ? Double.POSITIVE_INFINITY
		        : Double.NEGATIVE_INFINITY;
		double n = init, w = init, nw = init;
		if ((row == 0) && (column == 0)) {
			nw = this.diag_weight * cij;
		} else if ((row > 0) && (column == 0)) {
			n = ((this.comp_weight * cij) + prev.get(0, 0));
		} else if ((row == 0) && (column > 0)) {
			w = ((this.exp_weight * cij) + curr.get(column - 1, 0));
		} else { // all other cases
			n = (this.comp_weight * cij) + prev.get(column, 0);
			nw = (this.diag_weight * cij) + prev.get(column - 1, 0);
			w = (this.exp_weight * cij) + curr.get(column - 1, 0);
		}
		m = minimize1 ? MathTools.min(n, w, nw) : MathTools.max(n, w, nw);
		curr.set(column, 0, m);
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IRecurrence#cumDist(int, int,
	 * ucar.ma2.ArrayDouble.D2, double)
	 */
	public double eval(final int row, final int column,
	        final IArrayD2Double cumDistMatrix, final double dij,
	        final int[][] predecessors) {
		if (this.minimize) {
			return cumDistM(row, column, cumDistMatrix, dij, true, predecessors);
		}
		return cumDistM(row, column, cumDistMatrix, dij, false, predecessors);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IRecurrence#cumDist(int, int,
	 * ucar.ma2.ArrayDouble.D1, ucar.ma2.ArrayDouble.D1, double)
	 */
	public double eval(final int row, final int column,
	        final IArrayD2Double previousRow, final IArrayD2Double currentRow,
	        final double dij) {
		if (this.minimize) {
			return cumDistM(row, column, previousRow, currentRow, dij, true);
		}
		return cumDistM(row, column, previousRow, currentRow, dij, false);
	}

	public double getCompressionWeight() {
		return this.comp_weight;
	}

	public double getDiagonalWeight() {
		return this.diag_weight;
	}

	public double getExpansionWeight() {
		return this.exp_weight;
	}

	private int nequal(final double a, final double b, final double c) {
		int n = 0;
		if ((a == b) || (b == c)) {
			n += 2;
		}
		if (a == c) {
			n++;
		}
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IRecurrence#set(double, double)
	 */
	public void set(final double compression_weight1,
	        final double expansion_weight1, final double diagonal_weight1) {
		this.comp_weight = compression_weight1;
		this.exp_weight = expansion_weight1;
		this.diag_weight = diagonal_weight1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see maltcms.commands.distances.IRecurrence#setMinimizing(boolean)
	 */
	public void setMinimizing(final boolean b) {
		this.minimize = b;
		// minimize
		// if (b) {
		// this.comp_weight = 2.0d;
		// this.exp_weight = 2.0d;
		// this.diag_weight = 1.0d;
		// } else {// maximize
		// this.comp_weight = 1.0d;
		// this.exp_weight = 1.0d;
		// this.diag_weight = 3.0d;
		// }
	}

}
