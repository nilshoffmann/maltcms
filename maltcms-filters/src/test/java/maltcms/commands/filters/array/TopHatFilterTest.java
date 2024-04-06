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
package maltcms.commands.filters.array;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.MAMath;

/**
 *
 * @author Nils Hoffmann
 */
public class TopHatFilterTest {

    /**
     * Test of apply method, of class SavitzkyGolayFilter.
     */
    @Test
    public void testApply() throws IOException {
        Array tic = Array.makeFromJavaArray(TestArray.TIC);
        double ticSum = MAMath.sumDouble(tic);
        Assert.assertEquals(1.881895164E9, ticSum, 0.0d);
        TopHatFilter filter = new TopHatFilter();
        filter.setCopyArray(true);
        filter.setWindow(5);
        Array result = filter.apply(tic);
        Assert.assertEquals(tic.getShape()[0], result.getShape()[0]);
        Assert.assertEquals(9.08282779E8, MAMath.sumDouble(result), 0.0d);
        Array added = MAMath.add(tic, new MultiplicationFilter(-1.0d).apply(result));
        Assert.assertTrue(MAMath.sumDouble(added) > MAMath.sumDouble(result));
        filter.setWindow(50);
        result = filter.apply(tic);
        Assert.assertEquals(tic.getShape()[0], result.getShape()[0]);
        Assert.assertEquals(1.292133967E9, MAMath.sumDouble(result), 0.0d);
        added = MAMath.add(tic, new MultiplicationFilter(-1.0d).apply(result));
        Assert.assertTrue(MAMath.sumDouble(added) < MAMath.sumDouble(result));
        filter.setWindow(500);
        result = filter.apply(tic);
        Assert.assertEquals(tic.getShape()[0], result.getShape()[0]);
        Assert.assertEquals(1.392250034E9, MAMath.sumDouble(result), 0.0d);
        added = MAMath.add(tic, new MultiplicationFilter(-1.0d).apply(result));
        Assert.assertTrue(MAMath.sumDouble(added) < MAMath.sumDouble(result));
        filter.setWindow(1000);
        result = filter.apply(tic);
        Assert.assertEquals(tic.getShape()[0], result.getShape()[0]);
        Assert.assertEquals(1.418168227E9, MAMath.sumDouble(result), 0.0d);
        added = MAMath.add(tic, new MultiplicationFilter(-1.0d).apply(result));
        Assert.assertTrue(MAMath.sumDouble(added) < MAMath.sumDouble(result));
    }
}
