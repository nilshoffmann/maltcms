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
package maltcms.commands.fragments2d.peakfinding.cwt;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.test.IntegrationTest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments2d.preprocessing.Default2DVarLoader;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ZipResourceExtractor;
import org.apache.log4j.Level;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author nilshoffmann
 */
@Slf4j
@Category(IntegrationTest.class)
public class CwtPeakFinderTest extends AFragmentCommandTest {

    @Test
    public void testPeakFinder() throws IOException {
        File dataFolder = tf.newFolder("chroma4DTestData");
        File outputBase = tf.newFolder("chroma4DTestOut");
        File inputFile = ZipResourceExtractor.extract(
            "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz", dataFolder);
        setLogLevelFor(CwtPeakFinder.class, Level.ALL);
        Default2DVarLoader d2vl = new Default2DVarLoader();
//        d2vl.setEstimateModulationTime(true);
        d2vl.setEstimateModulationTime(false);
        d2vl.setModulationTime(5.0d);
        d2vl.setScanRate(100.0);
        CwtPeakFinder cpf = new CwtPeakFinder();
        List<IFragmentCommand> l = new LinkedList<IFragmentCommand>();
        l.add(d2vl);
        l.add(cpf);
        IWorkflow w = createWorkflow(outputBase, l, Arrays.asList(inputFile));
        try {
            TupleND<IFileFragment> results = w.call();
            w.save();
        } catch (Exception ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    public static void main(String[] args) {
        CwtPeakFinderTest test = new CwtPeakFinderTest();
        try {
            test.testPeakFinder();
        } catch (IOException ex) {
            Logger.getLogger(CwtPeakFinderTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
