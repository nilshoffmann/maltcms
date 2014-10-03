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
package net.sf.maltcms.evaluation.api.classification;

/**
 * <p>IPerformanceMetrics interface.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public interface IPerformanceMetrics {

    public static enum Vars {

        TP, FP, TN, FN, F1, SENSITIVITY, SPECIFICITY, FPR, FNR, RECALL, ACCURACY, GAIN
    };

    /**
     * <p>getAccuracy.</p>
     *
     * @return a double.
     */
    double getAccuracy();

    /**
     * <p>getF1.</p>
     *
     * @return a double.
     */
    double getF1();

    /**
     * <p>getFNR.</p>
     *
     * @return a double.
     */
    double getFNR();

    /**
     * <p>getFPR.</p>
     *
     * @return a double.
     */
    double getFPR();

    /**
     * <p>getFn.</p>
     *
     * @return a int.
     */
    int getFn();

    /**
     * <p>getFp.</p>
     *
     * @return a int.
     */
    int getFp();

    /**
     * <p>getGain.</p>
     *
     * @return a double.
     */
    double getGain();

    /**
     * <p>getPrecision.</p>
     *
     * @return a double.
     */
    double getPrecision();

    /**
     * <p>getRecall.</p>
     *
     * @return a double.
     */
    double getRecall();

    /**
     * <p>getSensitivity.</p>
     *
     * @return a double.
     */
    double getSensitivity();

    /**
     * <p>getSpecificity.</p>
     *
     * @return a double.
     */
    double getSpecificity();

    /**
     * <p>getTn.</p>
     *
     * @return a int.
     */
    int getTn();

    /**
     * <p>getTp.</p>
     *
     * @return a int.
     */
    int getTp();

}
