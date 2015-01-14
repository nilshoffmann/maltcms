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
package maltcms.commands.fragments2d.peakfinding.srg;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.BfsVariableSearcher;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.test.IntegrationTest;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import maltcms.commands.fragments2d.peakfinding.SeededRegionGrowing;
import maltcms.commands.fragments2d.peakfinding.cwt.CwtPeakFinder;
import maltcms.commands.fragments2d.preprocessing.Default2DVarLoader;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Nils Hoffmann
 */
@Category(IntegrationTest.class)
public class SeededRegionGrowingTest extends AFragmentCommandTest {
    
    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz");
    
    @Test
    public void testPeakFinder() throws IOException {
        setLogLevelFor(CwtPeakFinder.class, Level.DEBUG);
        setLogLevelFor(SeededRegionGrowing.class, Level.DEBUG);
        setLogLevelFor(NetcdfDataSource.class, Level.ERROR);
        setLogLevelFor(BfsVariableSearcher.class, Level.ERROR);
        setLogLevelFor("cross.datastructures", Level.ERROR);
        Default2DVarLoader d2vl = new Default2DVarLoader();
//        d2vl.setEstimateModulationTime(true);
        d2vl.setEstimateModulationTime(false);
        d2vl.setModulationTime(5.0d);
        d2vl.setScanRate(100.0);
        CwtPeakFinder cpf = new CwtPeakFinder();
        SeededRegionGrowing srg = new SeededRegionGrowing();
        List<IFragmentCommand> l = new LinkedList<>();
        l.add(d2vl);
        l.add(cpf);
        l.add(srg);
        IWorkflow w = createWorkflow(l, testFiles.getFiles());
        try {
            TupleND<IFileFragment> results = w.call();
            w.save();
        } catch (Exception ex) {
            Assert.fail(ex.getLocalizedMessage());
        }
    }
}
