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
package maltcms.commands.distances.dtwng;

import cross.Factory;
import cross.annotations.Configurable;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.MathTools;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import maltcms.datastructures.array.IArrayD2Double;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.tools.PathTools;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;

/**
 * <p>ThreePredecessorsOptimization class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@Data
public class ThreePredecessorsOptimization implements IOptimizationFunction {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ThreePredecessorsOptimization.class);

    private boolean minimize = true;
    private double[] weights = new double[]{1.0, 1.0, 1.0};
    private double ins = -0.0d;
    private double del = -0.0d;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private State NW = State.NW;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private State N = State.N;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private State W = State.W;
    private TwoFeatureVectorOperation tfvo = new FeatureVectorDtwSimilarity();
    private List<IFeatureVector> l;
    private List<IFeatureVector> r;
    @Setter(AccessLevel.NONE)
    private List<Point> trace = null;
    @Setter(AccessLevel.NONE)
    private List<State> stateSequence = null;
    @Setter(AccessLevel.NONE)
    private String stateSequenceString = "";
    @Setter(AccessLevel.NONE)
    private IArrayD2Double cumulatedScores;
    @Setter(AccessLevel.NONE)
    private IArrayD2Double pwScores;
    @Setter(AccessLevel.NONE)
    private State[][] traceMatrix;
    @Setter(AccessLevel.NONE)
    private int[] opcounter;
    @Configurable(name = "alignment.save.cumulative.distance.matrix")
    private boolean saveCDM = false;
    @Configurable(name = "alignment.save.pairwise.distance.matrix")
    private boolean savePWDM = false;
    @Configurable(name = "var.alignment.cumulative_distance")
    private String cumulativeDistanceVariableName = "cumulative_distance";
    @Configurable(name = "var.alignment.pairwise_distance")
    private String pairwiseDistanceVariableName = "pairwise_distance";
    @Configurable(name = "alignment.normalizeAlignmentValueByMapWeights")
    private boolean normalizeAlignmentValueByMapWeights;

    /** {@inheritDoc} */
    @Override
    public String getOptimalOperationSequenceString() {
        return this.stateSequenceString;
    }

    /** {@inheritDoc} */
    @Override
    public double getWeight(String state) {
        return weights[State.valueOf(state).ordinal()];
    }

    public static enum State {

        NW, N, W;

        public static String toString(State s) {
            if (s == NW) {
                return "o";
            } else if (s == N) {
                return "-";
            } else if (s == W) {
                return "+";
            } else {
                return s.ordinal() + "";
            }

        }
    }

    /** {@inheritDoc} */
    @Override
    public void init(List<IFeatureVector> l, List<IFeatureVector> r,
            IArrayD2Double cumulatedScore, IArrayD2Double pwScores,
            TwoFeatureVectorOperation tfvo) {
        this.l = l;
        this.r = r;
        this.cumulatedScores = cumulatedScore;
        this.pwScores = pwScores;
        this.traceMatrix = new State[this.l.size()][this.r.size()];
        this.trace = Collections.emptyList();
        this.stateSequenceString = "";
        this.tfvo = tfvo;
        if (this.tfvo.isMinimize()) {
            this.minimize = true;
        } else {
            this.minimize = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void apply(int... current) {
        cumDistM(current[0], current[1], l.get(current[0]), r.get(current[1]),
                cumulatedScores, pwScores, traceMatrix);
    }

    private State cumDistM(final int row, final int column, IFeatureVector l,
            IFeatureVector r, final IArrayD2Double cumDistMatrix,
            final IArrayD2Double pwvalues, final State[][] predecessors) {
        final double cij = this.tfvo.apply(l, r);
        pwvalues.set(row, column, cij);
        // log.info(l.getFeature("FEATURE0") + " gegen " +
        // r.getFeature("FEATURE0") + ":" + cij);
        // log.info("Score(" + row + "," + column + ")=" + cij);
        final double init = this.minimize ? Double.POSITIVE_INFINITY
                : Double.NEGATIVE_INFINITY;
        double n = init, w = init, nw = init;
        if ((row == 0) && (column == 0)) {
            nw = weights[NW.ordinal()] * cij;
            predecessors[row][column] = NW;
            // log.info("cij = "+cij + ", nw= "+nw);
            cumDistMatrix.set(row, column, nw);
            // this.log.debug(
            // "Case 1: First column, first row ={}, best from Diag", nw);
            return NW;
        } else if ((row > 0) && (column == 0)) {
            n = ((weights[N.ordinal()] * cij) + cumDistMatrix.get(row - 1, 0))
                    + this.ins;
            // log.info("i="+row+" j="+column+" cij = "+cij +
            // ", n= "+n);
            cumDistMatrix.set(row, column, n);
            predecessors[row][column] = N;
            // this.log.debug("Case 2: First column, row {}={}, best from Up",
            // row, n);
            return N;
        } else if ((row == 0) && (column > 0)) {
            w = ((weights[W.ordinal()] * cij) + cumDistMatrix.get(0, column - 1)) + this.del;
            // log.info("i="+row+" j="+column+" cij = "+cij +
            // ", w= "+w);
            cumDistMatrix.set(row, column, w);
            predecessors[row][column] = W;
            // this.log.debug("Case 3: First row, column {}={}, best from Left",
            // column, w);
            return W;
        } else {
            n = (weights[N.ordinal()] * cij)
                    + cumDistMatrix.get(row - 1, column) + this.ins;
            nw = (weights[NW.ordinal()] * cij)
                    + cumDistMatrix.get(row - 1, column - 1);
            w = (weights[W.ordinal()] * cij)
                    + cumDistMatrix.get(row, column - 1) + this.del;

            final double m = minimize ? MathTools.min(n, w, nw) : MathTools.max(
                    n, w, nw);
            cumDistMatrix.set(row, column, m);
            // log.info("Scores: " + m + " | " + nw + " | " + n +
            // " | "
            // + w);
            if (m == nw) {
                predecessors[row][column] = NW;
                return NW;
            } else if (m == n) {
                predecessors[row][column] = N;
                return N;
            } else {
                predecessors[row][column] = W;
                return W;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setWeight(String state, double weight) {
        this.weights[State.valueOf(state).ordinal()] = weight;
    }

    /** {@inheritDoc} */
    @Override
    public List<Point> getTrace() {
        int a = this.traceMatrix.length - 1;
        int b = this.traceMatrix[0].length - 1;
        State val = State.NW;
        this.opcounter = new int[State.values().length];
        StringBuilder sb = new StringBuilder();
        final ArrayList<Point> al = new ArrayList<>();
        final ArrayList<State> sl = new ArrayList<>();
        while ((a != 0) && (b != 0)) {
            val = this.traceMatrix[a][b];
            // log.info(this.traceMatrix[a][b]);
            // val = State.values()[i];
            switch (val) {
                case NW: {
                    al.add(new Point(a, b));
                    a--;
                    b--;
                    sl.add(NW);
                    sb.append(State.toString(NW));
                    opcounter[NW.ordinal()]++;
                    break;
                }
                case N: {
                    al.add(new Point(a, b));
                    a--;
                    sl.add(N);
                    sb.append(State.toString(N));
                    opcounter[N.ordinal()]++;
                    break;
                }
                case W: {
                    al.add(new Point(a, b));
                    b--;
                    sl.add(W);
                    sb.append(State.toString(W));
                    opcounter[W.ordinal()]++;
                    break;
                }
                default: {
                    throw new IllegalArgumentException(
                            "Don't know how to handle predecessor of type " + val);
                }
            }
        }
        if (a == 0 && b == 0) {
            al.add(new Point(a, b));
            sl.add(NW);
            sb.append(State.toString(NW));
            opcounter[NW.ordinal()]++;
        } else if ((a > 0) && (b == 0)) {
            while (a >= 0) {
                al.add(new Point(a, b));
                a--;
                sl.add(N);
                sb.append(State.toString(N));
                opcounter[N.ordinal()]++;
            }
        } else if ((a == 0) && (b > 0)) {
            while (b >= 0) {
                al.add(new Point(a, b));
                b--;
                sl.add(W);
                sb.append(State.toString(W));
                opcounter[W.ordinal()]++;
            }
        }
        Collections.reverse(al);
        Collections.reverse(sl);
        sb.reverse();
        this.stateSequence = sl;
        this.stateSequenceString = sb.toString();
        this.trace = al;
        return this.trace;
    }

    /**
     * Adds VariableFragments and arrays corresponding to path to parent
     * FileFragment.
     *
     * @param parent
     * @param al
     * @param ia
     */
    private void getFragments(final IFileFragment parent, final List<Point> al,
            final IArrayD2Double ia) {
        final Tuple2D<Array, Array> t = PathTools.pointListToArrays(al);
        final Dimension d = new Dimension("steps", al.size(), true, false,
                false);
        final IVariableFragment pathDist = new VariableFragment(parent, Factory.
                getInstance().getConfiguration().getString(
                        "var.warp_path_distance", "warp_path_distance"));
        final IVariableFragment wpi = new VariableFragment(parent, Factory.
                getInstance().getConfiguration().getString("var.warp.path.i",
                        "warp_path_i"));
        wpi.setDimensions(new Dimension[]{d});
        pathDist.setDimensions(new Dimension[]{d});
        final ArrayDouble.D1 dists = new ArrayDouble.D1(al.size());
        int i = 0;
        for (final Point tp : al) {
            dists.set(i, ia.get(tp.x, tp.y));
            i++;
        }
        pathDist.setArray(dists);
        final IVariableFragment wpj = new VariableFragment(parent, Factory.
                getInstance().getConfiguration().getString("var.warp.path.j",
                        "warp_path_j"));
        wpj.setDimensions(new Dimension[]{d});
        wpi.setArray(t.getFirst());
        wpj.setArray(t.getSecond());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.experimental.datastructures.IFileFragmentModifier#decorate(cross
     * .datastructures.fragments.IFileFragment)
     */
    /** {@inheritDoc} */
    @Override
    public void modify(IFileFragment iff) {
        if (this.saveCDM) {
            final IVariableFragment vf = new VariableFragment(iff,
                    this.cumulativeDistanceVariableName, null);
            vf.setDimensions(new Dimension[]{
                new Dimension("reference_scan", this.l.size(), true,
                false,
                false),
                new Dimension("query_scan", this.r.size(), true, false,
                false)});
            vf.setDataType(DataType.DOUBLE);
            vf.setArray(this.cumulatedScores.getArray());
        }
        if (this.savePWDM) {
            final IVariableFragment vf = new VariableFragment(iff,
                    this.pairwiseDistanceVariableName, null);
            vf.setDimensions(new Dimension[]{
                new Dimension("reference_scan", this.l.size(), true,
                false,
                false),
                new Dimension("query_scan", this.r.size(), true, false,
                false)});
            vf.setDataType(DataType.DOUBLE);
            vf.setArray(this.pwScores.getArray());
        }
        getFragments(iff, getTrace(), this.pwScores);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    /** {@inheritDoc} */
    @Override
    public void configure(Configuration cfg) {
        this.saveCDM = cfg.getBoolean(
                "alignment.save.cumulative.distance.matrix", false);
        this.savePWDM = cfg.getBoolean(
                "alignment.save.pairwise.distance.matrix", false);
        this.cumulativeDistanceVariableName = cfg.getString(
                "var.alignment.cumulative_distance", "cumulative_distance");
        this.pairwiseDistanceVariableName = cfg.getString(
                "var.alignment.pairwise_distance", "pairwise_distance");
        this.normalizeAlignmentValueByMapWeights = cfg.getBoolean(
                "alignment.normalizeAlignmentValueByMapWeights", false);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * maltcms.experimental.operations.IOptimizationFunction#getOptimalValue()
     */
    /** {@inheritDoc} */
    @Override
    public double getOptimalValue() {
        if (this.normalizeAlignmentValueByMapWeights) {
            double[] vals = new double[this.opcounter.length];
            for (State s : State.values()) {
                vals[s.ordinal()] = this.weights[s.ordinal()]
                        * this.opcounter[s.ordinal()];
            }
            double sum = 0;
            for (double d : vals) {
                sum += d;
            }
            return this.cumulatedScores.get(this.l.size() - 1,
                    this.r.size() - 1) / sum;
        }
        return this.cumulatedScores.get(this.l.size() - 1, this.r.size() - 1);
    }

    /**
     * <p>showCumScoreMatrix.</p>
     */
    public void showCumScoreMatrix() {
        log.info("");
        for (int i = 0; i < this.cumulatedScores.getShape().getBounds2D().
                getMaxX(); i++) {
            log.info(i + ": ");
            for (int j = 0; j < this.cumulatedScores.getShape().getBounds2D().
                    getMaxY(); j++) {
                log.info(this.cumulatedScores.get(i, j) + " ");
            }
            log.info("");
        }
    }

    /**
     * <p>showPwScoreMatrix.</p>
     */
    public void showPwScoreMatrix() {
        log.info("");
        for (int i = 0; i < this.pwScores.getShape().getBounds2D().getMaxX(); i++) {
            log.info(i + ": ");
            for (int j = 0; j < this.pwScores.getShape().getBounds2D().getMaxY(); j++) {
                log.info(this.pwScores.get(i, j) + " ");
            }
            log.info("");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.experimental.operations.IOptimizationFunction#getStates()
     */
    /** {@inheritDoc} */
    @Override
    public String[] getStates() {
        State[] s = State.values();
        String[] names = new String[s.length];
        int i = 0;
        for (State state : s) {
            names[i++] = state.name();
        }
        return names;
    }
}
