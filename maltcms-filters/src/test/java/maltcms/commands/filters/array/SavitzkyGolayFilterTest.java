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

import cross.datastructures.fragments.ImmutableFileFragment;
import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import maltcms.test.ZipResourceExtractor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class SavitzkyGolayFilterTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    /**
     * Test of apply method, of class SavitzkyGolayFilter.
     */
    @Test
    public void testApply() throws IOException {
        File outputFolder = tf.newFolder(
                "cdf");
        File outputFile = ZipResourceExtractor.extract("/cdf/1D/glucoseA.cdf.gz", outputFolder);
        ImmutableFileFragment ff = new ImmutableFileFragment(outputFile);
        Array tic = ff.getChild("total_intensity").getArray();
        SavitzkyGolayFilter instance = new SavitzkyGolayFilter();
        instance.setWindow(2);
        instance.setPolynomialDegree(2);
        Array result = instance.apply(tic);
        Assert.assertEquals(tic.getShape()[0], result.getShape()[0]);
    }
}
