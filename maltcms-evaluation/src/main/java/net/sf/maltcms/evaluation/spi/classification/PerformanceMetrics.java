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
package net.sf.maltcms.evaluation.spi.classification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import maltcms.datastructures.array.IFeatureVector;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;
import net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics;

/**
 * <p>PerformanceMetrics class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class PerformanceMetrics<T extends IFeatureVector> implements IPerformanceMetrics {

    private int tp, fp, tn, fn, realfn, groundTruthEntities, toolEntities, unmatchedTool, unmatchedGroundTruth, K;
    private double dist, f1;
    private String toolName;
    private HashSet<EntityGroup<T>> unmatchedToolEnt, unmatchedGroundTruthEnt;
    private HashMap<EntityGroup<T>, EntityGroupClassificationResult<T>> groundTruthToToolMatchResults;

    /**
     * <p>Constructor for PerformanceMetrics.</p>
     *
     * @param toolName a {@link java.lang.String} object.
     * @param tp a int.
     * @param fp a int.
     * @param tn a int.
     * @param fn a int.
     * @param N a int.
     * @param M a int.
     * @param K a int.
     * @param dist a double.
     * @param unmatchedTool a {@link java.util.HashSet} object.
     * @param unmatchedGroundTruth a {@link java.util.HashSet} object.
     * @param gtToClsRes a {@link java.util.HashMap} object.
     */
    public PerformanceMetrics(String toolName, int tp, int fp, int tn, int fn, int N, int M, int K, double dist, HashSet<EntityGroup<T>> unmatchedTool, HashSet<EntityGroup<T>> unmatchedGroundTruth, HashMap<EntityGroup<T>, EntityGroupClassificationResult<T>> gtToClsRes) {
        this.toolName = toolName;
        this.tp = tp;
        this.fp = fp;//+unmatchedTool;
        this.tn = tn;
        this.realfn = fn;
        this.groundTruthEntities = N;
        this.K = K;
        this.toolEntities = M;
        this.dist = dist;
        this.unmatchedTool = 0;////unmatchedTool.size();
        for (EntityGroup<T> group : unmatchedTool) {
            this.unmatchedTool += group.getEntities().size();
        }
        this.unmatchedGroundTruth = 0;//K * unmatchedGroundTruth.size();
        for (EntityGroup<T> group : unmatchedGroundTruth) {
            this.unmatchedGroundTruth += group.getEntities().size();
        }
        this.fn = this.realfn;// + (this.unmatchedGroundTruth);
        this.unmatchedToolEnt = unmatchedTool;
        this.unmatchedGroundTruthEnt = unmatchedGroundTruth;
        this.groundTruthToToolMatchResults = gtToClsRes;
        this.f1 = 2.0d * ((getPrecision() * getRecall()) / (getPrecision() + getRecall()));
    }

    /**
     * <p>Getter for the field <code>dist</code>.</p>
     *
     * @return a double.
     */
    public double getDist() {
        return dist;
    }

    /**
     * <p>Getter for the field <code>toolName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getToolName() {
        return this.toolName;
    }

    /** {@inheritDoc} */
    @Override
    public int getTp() {
        return this.tp;
    }

    /** {@inheritDoc} */
    @Override
    public int getFp() {
        return this.fp;
    }

    /** {@inheritDoc} */
    @Override
    public int getTn() {
        return this.tn;
    }

    /**
     * <p>getFnWithoutUnmatchedToolEntityGroups.</p>
     *
     * @return a int.
     */
    public int getFnWithoutUnmatchedToolEntityGroups() {
        return this.realfn;
    }

    /** {@inheritDoc} */
    @Override
    public int getFn() {
        return this.fn;
    }

    /**
     * <p>Getter for the field <code>toolEntities</code>.</p>
     *
     * @return a int.
     */
    public int getToolEntities() {
        return this.toolEntities;
    }

    /**
     * <p>getCommonEntities.</p>
     *
     * @return a int.
     */
    public int getCommonEntities() {
        return this.tp + this.tn;
    }

    /**
     * <p>Getter for the field <code>groundTruthEntities</code>.</p>
     *
     * @return a int.
     */
    public int getGroundTruthEntities() {
        return this.groundTruthEntities;
    }

    /**
     * <p>getUnmatchedToolEntities.</p>
     *
     * @return a int.
     */
    public int getUnmatchedToolEntities() {
        return this.unmatchedTool;
    }

    /**
     * <p>getUnmatchedGroundTruthEntities.</p>
     *
     * @return a int.
     */
    public int getUnmatchedGroundTruthEntities() {
        return this.unmatchedGroundTruth;
    }

    /**
     * <p>Getter for the field <code>groundTruthToToolMatchResults</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<EntityGroup<T>, EntityGroupClassificationResult<T>> getGroundTruthToToolMatchResults() {
        return this.groundTruthToToolMatchResults;
    }

    /** {@inheritDoc} */
    @Override
    public double getSensitivity() {
        double tpv = tp;
        double fnv = fn;
        double sens = (tpv / (tpv + fnv));
        return sens;
    }

    /** {@inheritDoc} */
    @Override
    public double getSpecificity() {
        double tnv = tn;
        double fpv = fp;
        double spec = (tnv / (tnv + fpv));
        return spec;
    }

    /** {@inheritDoc} */
    @Override
    public double getFPR() {
        return 1 - getSpecificity();
    }

    /** {@inheritDoc} */
    @Override
    public double getFNR() {
        return 1 - getSensitivity();
    }

    /** {@inheritDoc} */
    @Override
    public double getAccuracy() {
        double tpv = tp;
        double tnv = tn;
        double fpv = fp;
        double fnv = fn;
        double acc = ((tpv + tnv) / (tpv + tnv + fpv + fnv));
        return acc;
    }

    /** {@inheritDoc} */
    @Override
    public double getGain() {
        //log.info("tp+fn=" + (tp + fn));
        //log.info("tp+tn+fp+fn=" + (tp + tn + fp + fn));
        double r = ((double) (tp + fn)) / ((double) (tp + tn + fp + fn));
        //log.info("R=" + r);
        //log.info("Precisions=" + getPrecision());
        double gain = getPrecision() / r;
        return gain;
    }

    /** {@inheritDoc} */
    @Override
    public double getPrecision() {
        double tpv = tp;
        double fpv = fp;
        double prec = (tpv) / (tpv + fpv);
        return prec;
    }

    /** {@inheritDoc} */
    @Override
    public double getRecall() {
        return getSensitivity();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Toolname: " + this.toolName + "\n");
        sb.append("TP=" + tp + "\n");
        sb.append("TN=" + tn + "\n");
        sb.append("FP=" + fp + "\n");
        sb.append("FN=" + fn + "\n");
        sb.append("Sensitivity, Recall=" + getSensitivity() + "\n");
        sb.append("Specificity=" + getSpecificity() + "\n");
        sb.append("Precision, Positive Predictive Value=" + getPrecision() + "\n");
        //sb.append("Accuracy=" + getAccuracy() + "\n");
        //sb.append("Gain=" + getGain() + "\n");
        sb.append("F1 score=" + getF1() + "\n");
        sb.append("Distance to ground truth=" + getDist() + "\n");
        //sb.append("Matthews Correlation Coefficient=" + getMCC() + "\n");
        sb.append("Number of entities in ground truth=" + groundTruthEntities + "\n");
        sb.append("Percentage of matched ground truth entities=" + ((float) (groundTruthEntities - unmatchedGroundTruth)) / ((float) groundTruthEntities) + "\n");
        sb.append("Number of entities in testGroup=" + toolEntities + "\n");
        sb.append("Percentage of matched tool entities=" + ((float) (toolEntities - unmatchedTool)) / ((float) toolEntities) + "\n");
        sb.append("Number of entites shared with ground truth = " + getCommonEntities() + "\n");
        sb.append("Percentage of test vs. ground truth entities: " + ((double) toolEntities / (double) groundTruthEntities) + "\n");
        sb.append("Number of entities matched to ground truth = " + groundTruthEntities + "=(tp+tn+fp+fn+unmatched)=" + (tp + tn + fp + fn + unmatchedGroundTruth) + "\n");
        sb.append("Number of unmatched testGroup entities (ignored)= " + unmatchedTool + "\n");
        sb.append("Number of unmatched groundTruth entities (counted as false negatives)= " + unmatchedGroundTruth + "\n");
        sb.append("Unmatched tool entities:\n");
        for (EntityGroup<T> eg : unmatchedToolEnt) {
            sb.append(eg + "\n");
        }
        sb.append("Unmatched ground truth entities:\n");

        for (EntityGroup<T> eg : unmatchedGroundTruthEnt) {
            sb.append(eg + "\n");
        }
        return sb.toString();
    }

    private double getMCC() {
        double a = (getTp() * getTn()) - (getFp() * getFn());
        double b = Math.sqrt((getTp() + getFp()) * (getTp() + getFn()) * (getTn() + getFp()) * (getTn() + getFn()));
        return a / b;
    }

    /** {@inheritDoc} */
    @Override
    public double getF1() {
        return f1;
    }
}
