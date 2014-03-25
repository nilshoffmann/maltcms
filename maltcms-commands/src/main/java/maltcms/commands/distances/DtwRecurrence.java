/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.commands.distances;

import cross.annotations.Configurable;
import cross.exception.ConstraintViolationException;
import cross.tools.MathTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.array.IArrayD2Double;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.ArrayByte;

/**
 * Objects of this type are used to calculate the cumulative distance within the
 * {@link maltcms.commands.distances.dtw.ADynamicTimeWarp} Implementations.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
@ServiceProvider(service = IRecurrence.class)
public class DtwRecurrence implements IRecurrence {

    @Configurable(name = "alignment.algorithm.compressionweight")
    private double comp_weight = 1.0d;
    @Configurable(name = "alignment.algorithm.expansionweight")
    private double exp_weight = 1.0d;
    @Configurable(name = "alignment.algorithm.diagonalweight")
    private double diag_weight = 1.0d;
    @Configurable(name = "alignment.algorithm.globalGapPenalty")
    protected double globalGapPenalty = 0;
    private boolean minimize = true;
    private final String[] directions = new String[]{"Left", "Diag", "Up"};

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
        this.globalGapPenalty = cfg.getDouble(
                "alignment.algorithm.globalGapPenalty", 0);
    }

    private double cumDistM(final int row, final int column,
            final IArrayD2Double cumDistMatrix, final double cij,
            final boolean minimize1, final ArrayByte.D2 predecessors) {
        final double init = minimize1 ? Double.POSITIVE_INFINITY
                : Double.NEGATIVE_INFINITY;
        double n = init, w = init, nw = init;
        if ((row == 0) && (column == 0)) {
            nw = this.diag_weight * cij;
            predecessors.set(row, column, (byte) 1);
            // System.out.println("cij = "+cij + ", nw= "+nw);
            cumDistMatrix.set(row, column, nw);
            // log.debug(
            // "Case 1: First column, first row ={}, best from Diag", nw);
            return nw;
        } else if ((row > 0) && (column == 0)) {
            n = ((this.comp_weight * cij) + cumDistMatrix.get(row - 1, 0)) + this.globalGapPenalty;
            // System.out.println("i="+row+" j="+column+" cij = "+cij +
            // ", n= "+n);
            cumDistMatrix.set(row, column, n);
            predecessors.set(row, column, (byte) 2);
            // log.debug("Case 2: First column, row {}={}, best from Up",
            // row, n);
            return n;
        } else if ((row == 0) && (column > 0)) {
            w = ((this.exp_weight * cij) + cumDistMatrix.get(0, column - 1)) + this.globalGapPenalty;
            // System.out.println("i="+row+" j="+column+" cij = "+cij +
            // ", w= "+w);
            cumDistMatrix.set(row, column, w);
            predecessors.set(row, column, (byte) 3);
            // log.debug("Case 3: First row, column {}={}, best from Left",
            // column, w);
            return w;
        } else { // all other cases
            n = (this.comp_weight * cij) + cumDistMatrix.get(row - 1, column) + this.globalGapPenalty;
            nw = (this.diag_weight * cij)
                    + cumDistMatrix.get(row - 1, column - 1);
            w = (this.exp_weight * cij) + cumDistMatrix.get(row, column - 1) + this.globalGapPenalty;
            final int neq = nequal(n, nw, w);
            if (neq == 3 && (Double.isInfinite(n) || Double.isNaN(n))) {
                log.error("{} values are equal at {},{}, n={},nw={},w={}",
                        new Object[]{
                            neq, row, column, n, nw, w});
                throw new ConstraintViolationException(
                        "Illegal recursion state detected! Please check alignment constraints and pairwise similarity! Example: rtEpsilon should not be set too low for DTW!");
//				log.warn("n={},nw={},w={}", new Object[] { n, nw, w });
            }
            final double m = minimize1 ? MathTools.min(n, w, nw) : MathTools.max(
                    n, w, nw);
            // int neq = nequal(n, w, nw);
            if (m == nw) {
                predecessors.set(row, column, (byte) 1);
            } else if (m == n) {
                predecessors.set(row, column, (byte) 2);
            } else if (m == w) {
                predecessors.set(row, column, (byte) 3);
            }
            cumDistMatrix.set(row, column, m);
            // if (minimize1) {
            // log
            // .debug(
            // "Case 4: common element: min(n={},nw={},w={})={}, best from {}",
            // new Object[] {
            // n,
            // nw,
            // w,
            // m,
            // this.directions[predecessors[row][column] + 1] });
            // } else {
            // log
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
        //System.out.println("Gap penalty: " + this.globalGapPenalty);
        double m = cij;
        final double init = minimize1 ? Double.POSITIVE_INFINITY
                : Double.NEGATIVE_INFINITY;
        double n = init, w = init, nw = init;
        if ((row == 0) && (column == 0)) {
            nw = this.diag_weight * cij;
        } else if ((row > 0) && (column == 0)) {
            n = ((this.comp_weight * cij) + prev.get(0, 0))
                    + this.globalGapPenalty;
        } else if ((row == 0) && (column > 0)) {
            w = ((this.exp_weight * cij) + curr.get(column - 1, 0))
                    + this.globalGapPenalty;
        } else { // all other cases
            n = (this.comp_weight * cij) + prev.get(column, 0)
                    + this.globalGapPenalty;
            nw = (this.diag_weight * cij) + prev.get(column - 1, 0);
            w = (this.exp_weight * cij) + curr.get(column - 1, 0)
                    + this.globalGapPenalty;
            final int neq = nequal(n, nw, w);
            if (neq == 3) {
                log.error("{} values are equal at {},{}, n={},nw={},w={}",
                        new Object[]{
                            neq, row, column, n, nw, w});
                throw new ConstraintViolationException(
                        "Illegal recursion state detected! Please check alignment constraints and pairwise similarity! Example: rtEpsilon should not be set too low for DTW!");
//				log.warn("n={},nw={},w={}", new Object[] { n, nw, w });
            }
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
    @Override
    public double eval(final int row, final int column,
            final IArrayD2Double cumDistMatrix, final double dij,
            final ArrayByte.D2 predecessors) {
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
    @Override
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

    @Override
    public double getGlobalGapPenalty() {
        return this.globalGapPenalty;
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
    @Override
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
    @Override
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
