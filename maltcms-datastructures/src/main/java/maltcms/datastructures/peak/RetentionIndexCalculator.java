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
package maltcms.datastructures.peak;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.ArrayTools;

/**
 * <p>RetentionIndexCalculator class.</p>
 *
 * @author Nils Hoffmann
 * @deprecated since 1.3.1, please use
 * {@link net.sf.maltcms.db.search.api.ri.RetentionIndexCalculator}
 * 
 */
@Slf4j
@Deprecated
public class RetentionIndexCalculator {

    private final double[] riRTs;
    private final int[] nCarbAtoms;

    /**
     * <p>Constructor for RetentionIndexCalculator.</p>
     *
     * @param numberOfCarbonAtoms an array of int.
     * @param riPeaks a {@link maltcms.datastructures.peak.Peak1D} object.
     */
    public RetentionIndexCalculator(int[] numberOfCarbonAtoms,
            Peak1D... riPeaks) {
        riRTs = new double[riPeaks.length];
        for (int i = 0; i < riRTs.length; i++) {
            riRTs[i] = riPeaks[i].getApexTime();
        }
        this.nCarbAtoms = numberOfCarbonAtoms;
    }

    /**
     * <p>Constructor for RetentionIndexCalculator.</p>
     *
     * @param numberOfCarbonAtoms an array of int.
     * @param riRTs a double.
     */
    public RetentionIndexCalculator(int[] numberOfCarbonAtoms, double... riRTs) {
        this.nCarbAtoms = numberOfCarbonAtoms;
        this.riRTs = riRTs;
    }

    /**
     * <p>getIsothermalKovatsIndex.</p>
     *
     * @param rt a double.
     * @return a double.
     */
    public double getIsothermalKovatsIndex(double rt) {
        int prevRIIdx = getIndexOfPreviousRI(rt);
        int nextRIIdx = getIndexOfNextRI(rt);
        // log.info("Index of previous ri: " + prevRIIdx);
        // log.info("Index of next ri: " + nextRIIdx);
        if (prevRIIdx == -1 || nextRIIdx == -1) {
            return Double.NaN;
        }
        double prevRIrt = this.riRTs[prevRIIdx];
        double nextRIrt = this.riRTs[nextRIIdx];
        // log.info("RT of previous ri: " + prevRIrt);
        // log.info("RT of next ri: " + nextRIrt);
        int nCAtoms = nCarbAtoms[prevRIIdx];
        int NCAtoms = nCarbAtoms[nextRIIdx];
        double ri = 0;
        ri = riIt(nCAtoms, NCAtoms, prevRIrt, nextRIrt, rt);
        return ri;
    }

    private int getIndexOfPreviousRI(double rt) {
        // log.info("PREVIOUS INDEX:");
        // log.info("RT: " + rt);
        int idx = Arrays.binarySearch(this.riRTs, rt);
        // log.info(Arrays.toString(this.riRTs));
        int x = ((-1) * (idx + 1)) - 1;
        if (idx >= 0) {
            // log.info(this.riRTs[idx] + " < " + rt);
            return idx;
        } else {
            if (x >= 0) {
                // log.info(this.riRTs[x] + " > " + rt);
                return x;
            } else {
                // log.info("RT smaller than minimum RI rt!");
                return -1;
            }
        }
        // log.info("Previous RI at index: " + x);
        // if (idx >= 0) {
        // log.info("Direct rt match: " + rt + " at index " + idx);
        // return idx;
        // } else {
        // int prev = x;
        // return prev;
        // }
    }

    private int getIndexOfNextRI(double rt) {
        // log.info("NEXT INDEX:");
        // log.info("RT: " + rt);
        int idx = Arrays.binarySearch(this.riRTs, rt);
        // log.info(Arrays.toString(this.riRTs));
        int x = ((-1) * (idx + 1));
        if (idx >= 0) {
            // log.info(this.riRTs[idx] + " < " + rt);
            // return Math.min(this.riRTs.length, idx + 1);
            return Math.min(idx + 1, this.riRTs.length - 1);
        } else {
            if (x >= 0 && x < this.riRTs.length) {
                // log.info(this.riRTs[x] + " < " + rt);
                return x;
            } else {
                // log.info("RT larger than maximum RI rt!");
                return -1;
            }
        }
    }

    /**
     * <p>getTemperatureProgrammedKovatsIndex.</p>
     *
     * @param rt a double.
     * @return a double.
     */
    public double getTemperatureProgrammedKovatsIndex(double rt) {
        int prevRIIdx = getIndexOfPreviousRI(rt);
        int nextRIIdx = getIndexOfNextRI(rt);
        log.debug("Previous idx: " + prevRIIdx + " next idx: "
                + nextRIIdx);
        if (prevRIIdx == -1 || nextRIIdx == -1) {
            return Double.NaN;
        }
        double prevRIrt = this.riRTs[prevRIIdx];
        double nextRIrt = this.riRTs[nextRIIdx];
        int nCAtoms = nCarbAtoms[prevRIIdx];
        int NCAtoms = nCarbAtoms[nextRIIdx];
        double ri = 0;
        ri = riTp(nCAtoms, NCAtoms, prevRIrt, nextRIrt, rt);
        // log.info("cAtoms before: " + nCarbAtoms[prevRIIdx]
        // + " after: " + nCarbAtoms[nextRIIdx]);
        log.debug(prevRIrt + " < " + rt + " < " + nextRIrt + " RI: "
                + ri);
        return ri;
    }

    private double riIt(int n, int N, double trn, double trN, double tr) {
        if (n == N) {
            return 100.0d * n;
        }
        return 100.0d * (n + ((N - n) * logRatio(tr, trn, trN)));
    }

    private double ratio(double tr, double trn, double trN) {
        return (tr - trn) / (trN - trn);
    }

    private double logRatio(double tr, double trn, double trN) {
        return (Math.log10(tr) - Math.log10(trn)) / (Math.log10(trN) - Math.log10(trn));
    }

    private double riTp(int n, int N, double trn, double trN, double tr) {
        if (n == N) {
            return 100.0d * n;
        }
        return 100.0d * (n + ((N - n) * ratio(tr, trn, trN)));
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        int[] cs = (int[]) ArrayTools.indexArray(38, 10).get1DJavaArray(
                int.class);
        double[] rts;
        double[] rirts = new double[cs.length];
        rts = rirts;
        log.debug("Number of RIs: " + cs.length);
        double startSAT = 300;
        double endSAT = 3621;
        for (int i = 0; i < rirts.length; i++) {
            rirts[i] = (startSAT + (Math.random() * (endSAT - startSAT)));
        }
        Arrays.sort(rirts);
        for (int i = 0; i < rts.length; i++) {
            rts[i] = (startSAT - 100 + (Math.random() * (endSAT - startSAT + 121)));
        }
        Arrays.sort(rts);
        RetentionIndexCalculator ric = new RetentionIndexCalculator(cs, rirts);
        for (int i = 0; i < rts.length; i++) {
            log.debug("Item: " + (i + 1) + "/" + rts.length);
            // double istRI = ric.getIsothermalKovatsIndex(rts[i]);
            // log.info("Isothermal RI for peak at rt " + rts[i] +
            // " = "
            // + istRI);
            double tcRI = ric.getTemperatureProgrammedKovatsIndex(rts[i]);
            log.debug("Linear RI for peak at rt " + rts[i] + " = "
                    + tcRI + "; RI rt range: [" + rirts[0] + ":"
                    + rirts[rirts.length - 1] + "]");
        }
    }
}
