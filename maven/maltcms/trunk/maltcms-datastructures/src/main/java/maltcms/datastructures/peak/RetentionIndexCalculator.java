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
package maltcms.datastructures.peak;

import java.util.Arrays;

import maltcms.tools.ArrayTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class RetentionIndexCalculator {

    private final double[] riRTs;
    private final int[] nCarbAtoms;

    public RetentionIndexCalculator(int[] numberOfCarbonAtoms,
            Peak1D... riPeaks) {
        riRTs = new double[riPeaks.length];
        for (int i = 0; i < riRTs.length; i++) {
            riRTs[i] = riPeaks[i].getApexTime();
        }
        this.nCarbAtoms = numberOfCarbonAtoms;
    }

    public RetentionIndexCalculator(int[] numberOfCarbonAtoms, double... riRTs) {
        this.nCarbAtoms = numberOfCarbonAtoms;
        this.riRTs = riRTs;
    }

    public double getIsothermalKovatsIndex(double rt) {
        int prevRIIdx = getIndexOfPreviousRI(rt);
        int nextRIIdx = getIndexOfNextRI(rt);
        // System.out.println("Index of previous ri: " + prevRIIdx);
        // System.out.println("Index of next ri: " + nextRIIdx);
        if (prevRIIdx == -1 || nextRIIdx == -1 || prevRIIdx == nextRIIdx) {
            return Double.NaN;
        }
        double prevRIrt = this.riRTs[prevRIIdx];
        double nextRIrt = this.riRTs[nextRIIdx];
        // System.out.println("RT of previous ri: " + prevRIrt);
        // System.out.println("RT of next ri: " + nextRIrt);
        int nCAtoms = nCarbAtoms[prevRIIdx];
        double ri = 100 * (((Math.log10(rt) - Math.log10(prevRIrt)) / (Math
                .log10(nextRIrt) - Math.log10(prevRIrt))) + nCAtoms);
        // System.out.println("cAtoms before: " + nCarbAtoms[prevRIIdx]
        // + " after: " + nCarbAtoms[nextRIIdx]);
        // System.out.println(prevRIrt + " < " + rt + " < " + nextRIrt + " RI: "
        // + ri);
        return ri;
    }

    private int getIndexOfPreviousRI(double rt) {
        // System.out.println("PREVIOUS INDEX:");
        // System.out.println("RT: " + rt);
        int idx = Arrays.binarySearch(this.riRTs, rt);
        // System.out.println(Arrays.toString(this.riRTs));
        int x = ((-1) * (idx + 1)) - 1;
        if (idx >= 0) {
            // System.out.println(this.riRTs[idx] + " < " + rt);
            return idx;
        } else {
            if (x >= 0) {
                // System.out.println(this.riRTs[x] + " > " + rt);
                return x;
            } else {
                // System.out.println("RT smaller than minimum RI rt!");
                return -1;
            }
        }
        // System.out.println("Previous RI at index: " + x);
        // if (idx >= 0) {
        // System.out.println("Direct rt match: " + rt + " at index " + idx);
        // return idx;
        // } else {
        // int prev = x;
        // return prev;
        // }
    }

    private int getIndexOfNextRI(double rt) {
        // System.out.println("NEXT INDEX:");
        // System.out.println("RT: " + rt);
        int idx = Arrays.binarySearch(this.riRTs, rt);
        // System.out.println(Arrays.toString(this.riRTs));
        int x = ((-1) * (idx + 1));
        if (idx >= 0) {
            // System.out.println(this.riRTs[idx] + " < " + rt);
            // return Math.min(this.riRTs.length, idx + 1);
            return Math.min(idx + 1, this.riRTs.length - 1);
        } else {
            if (x >= 0 && x < this.riRTs.length) {
                // System.out.println(this.riRTs[x] + " < " + rt);
                return x;
            } else {
                // System.out.println("RT larger than maximum RI rt!");
                return -1;
            }
        }
    }

    public double getTemperatureProgrammedKovatsIndex(double rt) {
        int prevRIIdx = getIndexOfPreviousRI(rt);
        int nextRIIdx = getIndexOfNextRI(rt);
        System.out.println("Previous idx: " + prevRIIdx + " next idx: "
                + nextRIIdx);
        // if (prevRIIdx == -1 || nextRIIdx == -1 || prevRIIdx == nextRIIdx) {
        // return Double.NaN;
        // }
        double prevRIrt = this.riRTs[prevRIIdx];
        double nextRIrt = this.riRTs[nextRIIdx];
        int nCAtoms = nCarbAtoms[prevRIIdx];
        double ri = 0;
        // handle last case
        if (rt == prevRIrt && nextRIrt == prevRIrt) {
            ri = (100 * nCAtoms);
        } else {
            ri = 100 * (((rt - prevRIrt) / (nextRIrt - prevRIrt)) + nCAtoms);
        }
        // System.out.println("cAtoms before: " + nCarbAtoms[prevRIIdx]
        // + " after: " + nCarbAtoms[nextRIIdx]);
        System.out.println(prevRIrt + " < " + rt + " < " + nextRIrt + " RI: "
                + ri);
        return ri;
    }

    public static void main(String[] args) {
        int[] cs = (int[]) ArrayTools.indexArray(38, 10).get1DJavaArray(
                int.class);
        double[] rts = new double[cs.length * 5];
        double[] rirts = new double[cs.length];
        rts = rirts;
        System.out.println("Number of RIs: " + cs.length);
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
            System.out.println("Item: " + (i + 1) + "/" + rts.length);
            // double istRI = ric.getIsothermalKovatsIndex(rts[i]);
            // System.out.println("Isothermal RI for peak at rt " + rts[i] +
            // " = "
            // + istRI);
            double tcRI = ric.getTemperatureProgrammedKovatsIndex(rts[i]);
            System.out.println("Linear RI for peak at rt " + rts[i] + " = "
                    + tcRI + "; RI rt range: [" + rirts[0] + ":"
                    + rirts[rirts.length - 1] + "]");
        }
    }
}
