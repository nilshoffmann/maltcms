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

import net.sf.maltcms.evaluation.api.classification.IPerformanceMetrics;

/**
 * <p>PairwisePerformanceMetrics class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class PairwisePerformanceMetrics implements IPerformanceMetrics {

    public static enum Vars {

        TP, FP, TN, FN, F1, SENSITIVITY, SPECIFICITY, FPR, FNR, RECALL, ACCURACY, GAIN
    };
    private int tp, fp, tn, fn;
    private double precision, recall, f1;
    private String toolName, instanceName;

    /**
     * <p>Constructor for PairwisePerformanceMetrics.</p>
     *
     * @param toolName a {@link java.lang.String} object.
     * @param instanceName a {@link java.lang.String} object.
     * @param tp a int.
     * @param fp a int.
     * @param tn a int.
     * @param fn a int.
     */
    public PairwisePerformanceMetrics(String toolName, String instanceName, int tp, int fp, int tn, int fn) {
        this.toolName = toolName;
        this.instanceName = instanceName;
//		log.info("tp: "+tp+" fp: "+fp+" tn: "+tn+" fn: "+fn);
        this.tp = tp;
        this.fp = fp;//+unmatchedTool;
        this.tn = tn;
        this.fn = fn;
        this.recall = (double) tp / (double) (tp + fn);
        this.precision = (double) tp / (double) (tp + fp);
//		log.info("Recall: "+recall);
//		log.info("Precision: "+precision);
//    f1 = 2*t.p.r*p.p.v/(t.p.r+p.p.v)
        this.f1 = 2.0d * ((precision * recall) / (precision + recall));
//		log.info("F1: "+f1);
    }

    /**
     * <p>Getter for the field <code>toolName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getToolName() {
        return this.toolName;
    }

    /**
     * <p>Getter for the field <code>instanceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstanceName() {
        return this.instanceName;
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

    /** {@inheritDoc} */
    @Override
    public int getFn() {
        return this.fn;
    }

    /** {@inheritDoc} */
    @Override
    public double getSensitivity() {
        return this.recall;
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
        return precision;
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
        sb.append("Instance: " + this.instanceName + "\n");
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
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public double getF1() {
        return f1;
    }
}
