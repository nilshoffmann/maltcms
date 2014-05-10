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

import junit.framework.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nils Hoffmann
 */
public class RetentionIndexCalculatorTest {

    @Test
    public void testIsothermalRi() {
        RetentionIndexCalculator ric = new RetentionIndexCalculator(new int[]{12, 14, 16, 18}, 252.24, 273.46, 346.46, 464.63);
        Assert.assertTrue(Double.isNaN(ric.getIsothermalKovatsIndex(212.63)));
        Assert.assertEquals(1200.0d, ric.getIsothermalKovatsIndex(252.24), 0.0d);
        Assert.assertEquals(riIt(12, 14, 252.24, 273.46, 252.24 + ((273.46 - 252.24) / 2.0d)), ric.getIsothermalKovatsIndex(252.24 + ((273.46 - 252.24) / 2.0d)), 0.0d);
        Assert.assertEquals(1400.0d, ric.getIsothermalKovatsIndex(273.46), 0.0d);
        Assert.assertEquals(1600.0d, ric.getIsothermalKovatsIndex(346.46), 0.0d);
        Assert.assertEquals(1800.0d, ric.getIsothermalKovatsIndex(464.63), 0.0d);
        Assert.assertTrue(Double.isNaN(ric.getIsothermalKovatsIndex(521.11)));
    }

    @Test
    public void testTemperatureProgrammedRi() {
        RetentionIndexCalculator ric = new RetentionIndexCalculator(new int[]{12, 14, 16, 18}, 252.24, 273.46, 346.46, 464.63);
        Assert.assertTrue(Double.isNaN(ric.getTemperatureProgrammedKovatsIndex(212.63)));
        Assert.assertEquals(1200.0d, ric.getTemperatureProgrammedKovatsIndex(252.24), 0.0d);
        Assert.assertEquals(riTp(12, 14, 252.24, 273.46, 252.24 + ((273.46 - 252.24) / 2.0d)), ric.getTemperatureProgrammedKovatsIndex(252.24 + ((273.46 - 252.24) / 2.0d)), 0.0d);
        Assert.assertEquals(1400.0d, ric.getTemperatureProgrammedKovatsIndex(273.46), 0.0d);
        Assert.assertEquals(1600.0d, ric.getTemperatureProgrammedKovatsIndex(346.46), 0.0d);
        Assert.assertEquals(1800.0d, ric.getTemperatureProgrammedKovatsIndex(464.63), 0.0d);
        Assert.assertTrue(Double.isNaN(ric.getTemperatureProgrammedKovatsIndex(521.11)));
    }

    private double riIt(int n, int N, double trn, double trN, double tr) {
        return 100.0d * (n + ((N - n) * logRatio(tr, trn, trN)));
    }

    private double ratio(double tr, double trn, double trN) {
        return (tr - trn) / (trN - trn);
    }

    private double logRatio(double tr, double trn, double trN) {
        return (Math.log10(tr) - Math.log10(trn)) / (Math.log10(trN) - Math.log10(trn));
    }

    private double riTp(int n, int N, double trn, double trN, double tr) {
        return 100.0d * (n + ((N - n) * ratio(tr, trn, trN)));
    }
}
