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
package net.sf.maltcms.db.search.api.ri;

import cross.datastructures.tools.EvalTools;
import cross.exception.ConstraintViolationException;
import cross.tools.MathTools;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.IScan1D;
import maltcms.tools.ArrayTools;
import org.slf4j.LoggerFactory;

/**
 * <p>RetentionIndexCalculator class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

public class RetentionIndexCalculator {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RetentionIndexCalculator.class);

    private final double[] riRTs;
    private final int[] nCarbAtoms;

    /**
     * Arguments must be sorted in increasing order of retention time.
     *
     * @param riPeaks a {@link maltcms.datastructures.ms.IMetabolite} object.
     */
    public RetentionIndexCalculator(
            IMetabolite... riPeaks) {
        IMetabolite[] metabolites = riPeaks;
        Arrays.sort(metabolites, new Comparator<IMetabolite>() {
            @Override
            public int compare(IMetabolite t, IMetabolite t1) {
                return Double.compare(t.getRetentionTime(), t1.getRetentionTime());
            }
        });
        riRTs = new double[metabolites.length];
        String[] formulas = new String[metabolites.length];
        for (int i = 0; i < riRTs.length; i++) {
            riRTs[i] = metabolites[i].getRetentionTime();
            if (i >= 1) {
                if (riRTs[i] <= riRTs[i - 1]) {
                    throw new ConstraintViolationException("Retention times are not in ascending order!");
                }
            }
            if (riRTs[i] <= 0.0d) {
                throw new ConstraintViolationException("Retention time for retention index compound " + metabolites[i].getName() + " must not be smaller or equal to 0!");
            }
            formulas[i] = metabolites[i].getFormula();
            if (formulas[i] == null || formulas[i].isEmpty()) {
                throw new ConstraintViolationException("Formula for retention index compound " + metabolites[i].getName() + " must not be empty!");
            }
        }
        this.nCarbAtoms = parseCarbonNumberFromMolecularFormulas(formulas);
        log.info("nCarbonAtoms: " + Arrays.toString(nCarbAtoms));
    }

    /**
     * Arguments must be sorted in increasing order of retention time.
     *
     * @param numberOfCarbonAtoms an array of int.
     * @param riPeaks a {@link maltcms.datastructures.ms.IScan1D} object.
     */
    public RetentionIndexCalculator(int[] numberOfCarbonAtoms,
            IScan1D... riPeaks) {
        riRTs = new double[riPeaks.length];
        for (int i = 0; i < riPeaks.length; i++) {
            riRTs[i] = riPeaks[i].getScanAcquisitionTime();
            if (riRTs[i] <= 0.0d) {
                throw new ConstraintViolationException("Retention time for retention index compound " + (i + 1) + " must not be smaller or equal to 0!");
            }
        }
        this.nCarbAtoms = numberOfCarbonAtoms;
    }

    /**
     * Arguments must be sorted in increasing order of retention time.
     *
     * @param numberOfCarbonAtoms an array of int.
     * @param riRTs a double.
     */
    public RetentionIndexCalculator(int[] numberOfCarbonAtoms, double... riRTs) {
        this.nCarbAtoms = numberOfCarbonAtoms;
        this.riRTs = riRTs;
    }

    /**
     * <p>parseCarbonNumberFromMolecularFormulas.</p>
     *
     * @param formulas an array of {@link java.lang.String} objects.
     * @return an array of int.
     */
    public static int[] parseCarbonNumberFromMolecularFormulas(String[] formulas) {
        int[] ns = new int[formulas.length];
        int i = 0;
        for (String formula : formulas) {
            Pattern pattern = Pattern.compile("c\\d+");
            Matcher matcher = pattern.matcher(formula.toLowerCase());
            int count = 0;
            while (matcher.find()) {
                count = Integer.parseInt(formula.substring(matcher.start() + 1,
                        matcher.end()));
                break;
            }
            EvalTools.gt(0, count, RetentionIndexCalculator.class);
            ns[i++] = count;
        }
        return ns;
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
        if (prevRIIdx == -1 || nextRIIdx == -1 || prevRIIdx == nextRIIdx) {

            if (this.riRTs.length >= 2) {
                if (prevRIIdx == -1) {
//                log.info("Interpolating ri value from first and second ri");
                    double x0 = this.riRTs[0];
                    double y0 = nCarbAtoms[0] * 100;
                    double x1 = this.riRTs[1];
                    double y1 = nCarbAtoms[1] * 100;
                    return extrapolate(x0, y0, x1, y1, rt);
                } else if (nextRIIdx == -1 || prevRIIdx == nextRIIdx) {
//                log.info("Interpolating ri value from last and previous to last ri");
                    double x0 = this.riRTs[riRTs.length - 2];
                    double y0 = nCarbAtoms[riRTs.length - 2] * 100;
                    double x1 = this.riRTs[riRTs.length - 1];
                    double y1 = nCarbAtoms[riRTs.length - 1] * 100;
                    return extrapolate(x0, y0, x1, y1, rt);
                }
            }
            return Double.NaN;
        }
        // log.info("Index of previous ri: " + prevRIIdx);
        // log.info("Index of next ri: " + nextRIIdx);
//        if (prevRIIdx == -1 || nextRIIdx == -1 || prevRIIdx == nextRIIdx) {
//            return Double.NaN;
//        }
        double prevRIrt = this.riRTs[prevRIIdx];
        double nextRIrt = this.riRTs[nextRIIdx];
        // log.info("RT of previous ri: " + prevRIrt);
        // log.info("RT of next ri: " + nextRIrt);
        int nCAtoms = nCarbAtoms[prevRIIdx];
        double ri = (100 * nCAtoms) + (100 * ((Math.log10(rt) - Math.log10(prevRIrt)) / (Math.
                log10(nextRIrt) - Math.log10(prevRIrt))));
        // log.info("cAtoms before: " + nCarbAtoms[prevRIIdx]
        // + " after: " + nCarbAtoms[nextRIIdx]);
//		 log.info(prevRIrt + " < " + rt + " < " + nextRIrt + " RI: "
//		 + ri);
        return ri;
    }

    private int getIndexOfPreviousRI(double rt) {
        // log.info("PREVIOUS INDEX:");
        // log.info("RT: " + rt);
        int idx = Arrays.binarySearch(this.riRTs, rt);
        // log.info(Arrays.toString(this.riRTs));
        //positionToInsertAt = (-(insertion point) - 1)
        //=> is larger position
        int posToInsert = ((-idx) - 1);
//        int x = ((-1) * (idx + 1)) - 1;
        if (idx >= 0) {
            // log.info(this.riRTs[idx] + " < " + rt);
            return idx;
        } else {
            if (posToInsert > 0) {
                // log.info(this.riRTs[x] + " > " + rt);
                return posToInsert - 1;
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
        int posToInsert = ((-idx) - 1);
//        int x = ((-1) * (idx + 1));
        if (idx >= 0) {
            // log.info(this.riRTs[idx] + " < " + rt);
            // return Math.min(this.riRTs.length, idx + 1);
            return Math.min(idx + 1, this.riRTs.length - 1);
        } else {
            if (posToInsert >= 0 && posToInsert < this.riRTs.length) {
                // log.info(this.riRTs[x] + " < " + rt);
                return posToInsert;
            } else {
                // log.info("RT larger than maximum RI rt!");
                return -1;
            }
        }
    }

    /**
     * <p>extrapolate.</p>
     *
     * @param x0 a double.
     * @param y0 a double.
     * @param x1 a double.
     * @param y1 a double.
     * @param x a double.
     * @return a double.
     */
    public double extrapolate(double x0, double y0, double x1, double y1, double x) {
        double value = MathTools.getLinearInterpolatedY(x0, y0, x1, y1, x);
//        log.info("Extrapolated ri for rt="+x+" [x0="+x0+" y0="+y0+"; x1="+x1+" y1="+y1+"] = "+value);
        return value;
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
//		log.info("Previous idx: " + prevRIIdx + " next idx: "
//				+ nextRIIdx);
        if (prevRIIdx == -1 || nextRIIdx == -1 || prevRIIdx == nextRIIdx) {

            if (this.riRTs.length >= 2) {
                if (prevRIIdx == -1) {
                    //                log.info("Interpolating ri value from first and second ri");
                    double x0 = this.riRTs[0];
                    double y0 = nCarbAtoms[0] * 100;
                    double x1 = this.riRTs[1];
                    double y1 = nCarbAtoms[1] * 100;
                    return extrapolate(x0, y0, x1, y1, rt);
                } else if (nextRIIdx == -1 || nextRIIdx == prevRIIdx) {
                    //                log.info("Interpolating ri value from last and previous to last ri");
                    double x0 = this.riRTs[riRTs.length - 2];
                    double y0 = nCarbAtoms[riRTs.length - 2] * 100;
                    double x1 = this.riRTs[riRTs.length - 1];
                    double y1 = nCarbAtoms[riRTs.length - 1] * 100;
                    return extrapolate(x0, y0, x1, y1, rt);
                }
            }
            return Double.NaN;
        }
        double prevRIrt = this.riRTs[prevRIIdx];
        double nextRIrt = this.riRTs[nextRIIdx];
        int nCAtoms = nCarbAtoms[prevRIIdx];
        double ri = 0;
        // handle last case
//		if (rt == prevRIrt && nextRIrt == prevRIrt) {
//			ri = (100 * nCAtoms);
//		} else {
        ri = 100 * nCAtoms + 100 * ((rt - prevRIrt) / (nextRIrt - prevRIrt));
//		}
//        log.info("cAtoms before: " + nCarbAtoms[prevRIIdx]
//                + " after: " + nCarbAtoms[nextRIIdx]);
//        log.info(prevRIrt + " < " + rt + " < " + nextRIrt + " RI: "
//                + ri);
        return ri;
    }

    /**
     * <p>getLinearIndex.</p>
     *
     * @param rt a double.
     * @return a double.
     */
    public double getLinearIndex(double rt) {
        int prevRIIdx = getIndexOfPreviousRI(rt);
        int nextRIIdx = getIndexOfNextRI(rt);
        if (prevRIIdx == -1 || nextRIIdx == -1 || prevRIIdx == nextRIIdx) {

            if (this.riRTs.length >= 2) {
                if (prevRIIdx == -1) {
                    double x0 = this.riRTs[0];
                    double y0 = nCarbAtoms[0] * 100;
                    double x1 = this.riRTs[1];
                    double y1 = nCarbAtoms[1] * 100;
                    return extrapolate(x0, y0, x1, y1, rt);
                } else if (nextRIIdx == -1 || prevRIIdx == nextRIIdx) {
                    double x0 = this.riRTs[riRTs.length - 2];
                    double y0 = nCarbAtoms[riRTs.length - 2] * 100;
                    double x1 = this.riRTs[riRTs.length - 1];
                    double y1 = nCarbAtoms[riRTs.length - 1] * 100;
                    return extrapolate(x0, y0, x1, y1, rt);
                }
            }
            return Double.NaN;
        }

        double x0 = this.riRTs[prevRIIdx];
        double y0 = nCarbAtoms[prevRIIdx] * 100;
        double x1 = this.riRTs[nextRIIdx];
        double y1 = nCarbAtoms[nextRIIdx] * 100;
        return extrapolate(x0, y0, x1, y1, rt);
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        int[] cs = (int[]) ArrayTools.indexArray(38, 10).get1DJavaArray(
                int.class);
        double[] rts = new double[cs.length * 5];
        double[] rirts = new double[cs.length];
//		rts = rirts;
        log.info("Number of RIs: " + cs.length);
        double startSAT = 300;
        double endSAT = 3621;
        for (int i = 0; i < rirts.length; i++) {
            rirts[i] = (startSAT + (Math.random() * (endSAT - startSAT)));
        }
        Arrays.sort(rirts);
        log.info("RI rts: " + Arrays.toString(rirts));
        for (int i = 0; i < rts.length; i++) {
            rts[i] = (startSAT - 100 + (Math.random() * (endSAT - startSAT + 121)));
        }
        Arrays.sort(rts);
        RetentionIndexCalculator ric = new RetentionIndexCalculator(cs, rirts);
        for (int i = 0; i < rts.length; i++) {
            log.info("Item: " + (i + 1) + "/" + rts.length);
            double istRI = ric.getIsothermalKovatsIndex(rts[i]);
            log.info("Isothermal RI for peak at rt " + rts[i]
                    + " = "
                    + istRI + "; RI rt range: [" + rirts[0] + ":"
                    + rirts[rirts.length - 1] + "]");
            double tcRI = ric.getTemperatureProgrammedKovatsIndex(rts[i]);
            log.info("Temperature programmed RI for peak at rt " + rts[i] + " = "
                    + tcRI + "; RI rt range: [" + rirts[0] + ":"
                    + rirts[rirts.length - 1] + "]");
            double liRI = ric.getLinearIndex(rts[i]);
            log.info("Linear RI for peak at rt " + rts[i] + " = "
                    + liRI + "; RI rt range: [" + rirts[0] + ":"
                    + rirts[rirts.length - 1] + "]");
        }
    }
}
