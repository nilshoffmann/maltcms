/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package net.sf.maltcms.evaluation.spi.classification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.sf.maltcms.evaluation.api.classification.EntityGroup;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE

 *
 */
public class PerformanceMetrics{

    private int tp, fp, tn, fn, realfn, groundTruthEntities, toolEntities, unmatchedTool, unmatchedGroundTruth, K;
    private double dist;
    private String toolName;
    private HashSet<EntityGroup> unmatchedToolEnt, unmatchedGroundTruthEnt;
    private HashMap<EntityGroup, EntityGroupClassificationResult> groundTruthToToolMatchResults;

    public PerformanceMetrics(String toolName, int tp, int fp, int tn, int fn, int N, int M, int K, double dist, HashSet<EntityGroup> unmatchedTool, HashSet<EntityGroup> unmatchedGroundTruth,HashMap<EntityGroup, EntityGroupClassificationResult> gtToClsRes) {
        this.toolName = toolName;
        this.tp = tp;
        this.fp = fp;//+unmatchedTool;
        this.tn = tn;
        this.realfn = fn;
        this.groundTruthEntities = N;
        this.K = K;
        this.toolEntities = M;
        this.dist = dist;
        this.unmatchedTool = unmatchedTool.size();
        this.unmatchedGroundTruth = K * unmatchedGroundTruth.size();
        this.fn = this.realfn;// + (this.unmatchedGroundTruth);
        this.unmatchedToolEnt = unmatchedTool;
        this.unmatchedGroundTruthEnt = unmatchedGroundTruth;
        this.groundTruthToToolMatchResults = gtToClsRes;
    }

    public double getDist() {
        return dist;
    }

    public String getToolName() {
        return this.toolName;
    }

    public int getTp() {
        return this.tp;
    }

    public int getFp() {
        return this.fp;
    }

    public int getTn() {
        return this.tn;
    }

    public int getFnWithoutUnmatchedToolEntityGroups() {
        return this.realfn;
    }

    public int getFn() {
        return this.fn;
    }

    public int getToolEntities() {
        return this.toolEntities;
    }

    public int getCommonEntities() {
        return this.tp+this.tn;
    }

    public int getGroundTruthEntities() {
        return this.groundTruthEntities;
    }

    public int getUnmatchedToolEntities() {
        return this.unmatchedTool;
    }

    public int getUnmatchedGroundTruthEntities() {
        return this.unmatchedGroundTruth;
    }
    
    public Map<EntityGroup, EntityGroupClassificationResult> getGroundTruthToToolMatchResults() {
        return this.groundTruthToToolMatchResults;
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
        return 1-getSpecificity();
    }

    public double getFNR() {
        return 1-getSensitivity();
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
        sb.append("Distance to ground truth="+getDist()+"\n");
        //sb.append("Matthews Correlation Coefficient=" + getMCC() + "\n");
        sb.append("Number of entities in ground truth=" + groundTruthEntities + "\n");
        sb.append("Percentage of matched ground truth entities="+((float)(groundTruthEntities-unmatchedGroundTruth))/((float)groundTruthEntities)+"\n");
        sb.append("Number of entities in testGroup=" + toolEntities + "\n");
        sb.append("Percentage of matched tool entities="+((float)(toolEntities-unmatchedTool))/((float)toolEntities)+"\n");
        sb.append("Number of entites shared with ground truth = " + getCommonEntities()+"\n");
        sb.append("Percentage of test vs. ground truth entities: " + ((double) toolEntities / (double) groundTruthEntities) + "\n");
        sb.append("Number of entities matched to ground truth = " + groundTruthEntities + "=(tp+tn+fp+fn)=" + (tp + tn + fp + fn) + "\n");
        sb.append("Number of unmatched testGroup entities (ignored)= " + unmatchedTool + "\n");
        sb.append("Number of unmatched groundTruth entities (counted as false negatives)= " + unmatchedGroundTruth + "\n");
        sb.append("Unmatched tool entities:\n");
        for(EntityGroup eg:unmatchedToolEnt) {
            sb.append(eg+"\n");
        }
        sb.append("Unmatched ground truth entities:\n");
        
        for(EntityGroup eg:unmatchedGroundTruthEnt) {
            sb.append(eg+"\n");
        }
        return sb.toString();
    }

    private double getMCC() {
        double a = (getTp() * getTn()) - (getFp() * getFn());
        double b = Math.sqrt((getTp() + getFp()) * (getTp() + getFn()) * (getTn() + getFp()) * (getTn() + getFn()));
        return a / b;
    }

    public double getF1() {
        double f = 2.0d * ((getPrecision() * getRecall()) / (getPrecision() + getRecall()));
        return f;
    }
}
