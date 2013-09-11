/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.evaluation.api;

import java.util.HashSet;

/**
 * @author Nils Hoffmann
 *
 *
 */
public final class PerformanceMetrics {

    private final int tp, fp, tn, fn, realfn, N, M, unmatchedTool, unmatchedGroundTruth, K;
    private final double dist;
    private final String toolname;
    private final HashSet<EntityGroup> unmatchedToolEnt, unmatchedGroundTruthEnt;

    public PerformanceMetrics(String toolname, int tp, int fp, int tn, int fn, int N, int M, int K, double dist, HashSet<EntityGroup> unmatchedTool, HashSet<EntityGroup> unmatchedGroundTruth) {
        this.toolname = toolname;
        this.tp = tp;
        this.fp = fp;//+unmatchedTool;
        this.tn = tn;
        this.realfn = fn;
        this.N = N;
        this.K = K;
        this.M = M;
        this.dist = dist;
        this.unmatchedTool = unmatchedTool.size();
        this.unmatchedGroundTruth = K * unmatchedGroundTruth.size();
        this.fn = this.realfn;// + (this.unmatchedGroundTruth);
        this.unmatchedToolEnt = unmatchedTool;
        this.unmatchedGroundTruthEnt = unmatchedGroundTruth;
    }

    public double getDist() {
        return dist;
    }

    public String getToolname() {
        return this.toolname;
    }

    public int getTP() {
        return this.tp;
    }

    public int getFP() {
        return this.fp;
    }

    public int getTN() {
        return this.tn;
    }

    public int getFNWithoutUnmatchedToolEntityGroups() {
        return this.realfn;
    }

    public int getFN() {
        return this.fn;
    }

    public int getToolEntities() {
        return this.M;
    }

    public int getCommonEntities() {
        return this.tp + this.tn;
    }

    public int getGroundTruthEntities() {
        return this.N;
    }

    public int getUnmatchedToolEntities() {
        return this.unmatchedTool;
    }

    public int getUnmatchedGroundTruthEntities() {
        return this.unmatchedGroundTruth;
    }

    public double getSensitivity() {
        double tpv = tp;
        double fnv = fn;
        double sens = (tpv / (tpv + fnv));
        return sens;
    }

    public double getSpecificity() {
        double tnv = tn;
        double fpv = fp;
        double spec = (tnv / (tnv + fpv));
        return spec;
    }

    public double getFPR() {
        return 1 - getSpecificity();
    }

    public double getFNR() {
        return 1 - getSensitivity();
    }

    public double getAccuracy() {
        double tpv = tp;
        double tnv = tn;
        double fpv = fp;
        double fnv = fn;
        double acc = ((tpv + tnv) / (tpv + tnv + fpv + fnv));
        return acc;
    }

    public double getGain() {
        //System.out.println("tp+fn=" + (tp + fn));
        //System.out.println("tp+tn+fp+fn=" + (tp + tn + fp + fn));
        double r = ((double) (tp + fn)) / ((double) (tp + tn + fp + fn));
        //System.out.println("R=" + r);
        //System.out.println("Precisions=" + getPrecision());
        double gain = getPrecision() / r;
        return gain;
    }

    public double getPrecision() {
        double tpv = tp;
        double fpv = fp;
        double prec = (tpv) / (tpv + fpv);
        return prec;
    }

    public double getRecall() {
        return getSensitivity();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Toolname: " + this.toolname + "\n");
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
        sb.append("Number of entities in ground truth=" + N + "\n");
        sb.append("Percentage of matched ground truth entities=" + ((float) (N - unmatchedGroundTruth)) / ((float) N) + "\n");
        sb.append("Number of entities in testGroup=" + M + "\n");
        sb.append("Percentage of matched tool entities=" + ((float) (M - unmatchedTool)) / ((float) M) + "\n");
        sb.append("Number of entites shared with ground truth = " + getCommonEntities() + "\n");
        sb.append("Percentage of test vs. ground truth entities: " + ((double) M / (double) N) + "\n");
        sb.append("Number of entities matched to ground truth = " + N + "=(tp+tn+fp+fn)=" + (tp + tn + fp + fn) + "\n");
        sb.append("Number of unmatched testGroup entities (ignored)= " + unmatchedTool + "\n");
        sb.append("Number of unmatched groundTruth entities (counted as false negatives)= " + unmatchedGroundTruth + "\n");
        sb.append("Unmatched tool entities:\n");
        for (EntityGroup eg : unmatchedToolEnt) {
            sb.append(eg + "\n");
        }
        sb.append("Unmatched ground truth entities:\n");

        for (EntityGroup eg : unmatchedGroundTruthEnt) {
            sb.append(eg + "\n");
        }
        return sb.toString();
    }

    private double getMCC() {
        double a = (getTP() * getTN()) - (getFP() * getFN());
        double b = Math.sqrt((getTP() + getFP()) * (getTP() + getFN()) * (getTN() + getFP()) * (getTN() + getFN()));
        return a / b;
    }

    public double getF1() {
        double f = 2.0d * ((getPrecision() * getRecall()) / (getPrecision() + getRecall()));
        return f;
    }
}
