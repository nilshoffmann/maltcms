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
package maltcms.commands.fragments2d.preprocessing;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;
import java.io.IOException;
import java.util.Arrays;

import maltcms.commands.fragments2d.preprocessing.Default2DVarLoader;
import maltcms.commands.fragments2d.preprocessing.MeanVarProducer;
import maltcms.commands.fragments2d.preprocessing.MeanVarVis;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */


public class MeanVarVisIT extends AFragmentCommandTest {
    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz");

    @Test
    public void testMeanVarVis() throws IOException {
        setLogLevelFor(Default2DVarLoader.class, Level.ALL);
        Default2DVarLoader d = new Default2DVarLoader();
        d.setEstimateModulationTime(false);
        d.setModulationTime(5.0d);
        d.setScanRate(100.0);
        MeanVarProducer e = new MeanVarProducer();
        MeanVarVis f = new MeanVarVis();
        f.setDifferentVisualizations(true);
        IWorkflow w = createWorkflow(
            Arrays.asList(
                (IFragmentCommand) d,
                (IFragmentCommand) e,
                (IFragmentCommand) f),
            testFiles.getFiles()
        );
        testWorkflow(w);
    }
}
