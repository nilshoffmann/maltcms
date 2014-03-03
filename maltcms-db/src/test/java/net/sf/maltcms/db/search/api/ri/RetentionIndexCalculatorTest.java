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

import java.util.Arrays;
import maltcms.tools.ArrayTools;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
public class RetentionIndexCalculatorTest {

    @Test
    public void testSameNumberOfRisAndCompounds() {
        //create an array of 38 elements from [10 to 10+38-1]
        int[] cs = (int[]) ArrayTools.indexArray(38, 10).get1DJavaArray(
            int.class);
        double[] rts;
        double[] rirts = new double[cs.length];
        rts = rirts;
        System.out.println("Number of RIs: " + cs.length);
        double startSAT = 300;
        double endSAT = 3621;
        //initialize rts of ris
        for (int i = 0; i < rirts.length; i++) {
            rirts[i] = (startSAT + (Math.random() * (endSAT - startSAT)));
        }
        Arrays.sort(rirts);
        System.out.println("RI rts: " + Arrays.toString(rirts));
        //initialize rts of peaks
        for (int i = 0; i < rts.length; i++) {
            rts[i] = (startSAT - 100 + (Math.random() * (endSAT - startSAT + 121)));
        }
        Arrays.sort(rts);
        //calculate and populate ri arrays
        RetentionIndexCalculator ric = new RetentionIndexCalculator(cs, rirts);
        double[] risIso = new double[rts.length];
        double[] risTc = new double[rts.length];
        double[] risLin = new double[rts.length];
        for (int i = 0; i < rts.length; i++) {
            risIso[i] = ric.getIsothermalKovatsIndex(rts[i]);
            assertFalse(Double.isNaN(risIso[i]));
            assertFalse(Double.isInfinite(risIso[i]));
            risTc[i] = ric.getTemperatureProgrammedKovatsIndex(rts[i]);
            assertFalse(Double.isNaN(risTc[i]));
            assertFalse(Double.isInfinite(risTc[i]));
            risLin[i] = ric.getLinearIndex(rts[i]);
            assertFalse(Double.isNaN(risLin[i]));
            assertFalse(Double.isInfinite(risLin[i]));
        }
        //check that ri values are weakly increasing
        for (int i = 1; i < rts.length; i++) {
            risIso[i] = ric.getIsothermalKovatsIndex(rts[i]);
            assertEquals(true, risIso[i] >= risIso[i - 1]);
            risTc[i] = ric.getTemperatureProgrammedKovatsIndex(rts[i]);
            assertEquals(true, risTc[i] >= risTc[i - 1]);
            risLin[i] = ric.getLinearIndex(rts[i]);
            assertEquals(true, risLin[i] >= risLin[i - 1]);
        }
    }

    @Test
    public void testMoreCompoundsThanRis() {
        //create an array of 38 elements from [10 to 10+38-1]
        int[] cs = (int[]) ArrayTools.indexArray(38, 10).get1DJavaArray(
            int.class);
        double[] rts = new double[cs.length * 5];
        double[] rirts = new double[cs.length];
        System.out.println("Number of RIs: " + cs.length);
        double startSAT = 300;
        double endSAT = 3621;
        //initialize rts of ris
        for (int i = 0; i < rirts.length; i++) {
            rirts[i] = (startSAT + (Math.random() * (endSAT - startSAT)));
        }
        Arrays.sort(rirts);
        //initialize rts of peaks
        System.out.println("RI rts: " + Arrays.toString(rirts));
        for (int i = 0; i < rts.length; i++) {
            rts[i] = (startSAT - 100 + (Math.random() * (endSAT - startSAT + 121)));
        }
        Arrays.sort(rts);
        //calculate and populate ri arrays
        RetentionIndexCalculator ric = new RetentionIndexCalculator(cs, rirts);
        double[] risIso = new double[rts.length];
        double[] risTc = new double[rts.length];
        double[] risLin = new double[rts.length];
        for (int i = 0; i < rts.length; i++) {
            risIso[i] = ric.getIsothermalKovatsIndex(rts[i]);
            assertFalse(Double.isNaN(risIso[i]));
            assertFalse(Double.isInfinite(risIso[i]));
            risTc[i] = ric.getTemperatureProgrammedKovatsIndex(rts[i]);
            assertFalse(Double.isNaN(risTc[i]));
            assertFalse(Double.isInfinite(risTc[i]));
            risLin[i] = ric.getLinearIndex(rts[i]);
            assertFalse(Double.isNaN(risLin[i]));
            assertFalse(Double.isInfinite(risLin[i]));
        }
        //check that ri values are weakly increasing
        for (int i = 1; i < rts.length; i++) {
            risIso[i] = ric.getIsothermalKovatsIndex(rts[i]);
            assertEquals(true, risIso[i] >= risIso[i - 1]);
            risTc[i] = ric.getTemperatureProgrammedKovatsIndex(rts[i]);
            assertEquals(true, risTc[i] >= risTc[i - 1]);
            risLin[i] = ric.getLinearIndex(rts[i]);
            assertEquals(true, risLin[i] >= risLin[i - 1]);
        }
    }
}
