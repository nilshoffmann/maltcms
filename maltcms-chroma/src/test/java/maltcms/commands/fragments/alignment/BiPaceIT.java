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
package maltcms.commands.fragments.alignment;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;
import cross.test.IntegrationTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.SavitzkyGolayFilter;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.Worker2DFactory;
import maltcms.commands.fragments.alignment.peakCliqueAlignment.peakFactory.Peak2DMSFactory;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.math.functions.IArraySimilarity;
import maltcms.math.functions.IScalarSimilarity;
import maltcms.math.functions.ProductSimilarity;
import maltcms.math.functions.similarities.ArrayWeightedCosine2;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Nils Hoffmann
 */
@Category(IntegrationTest.class)
public class BiPaceIT extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz", "/cdf/1D/glucoseB.cdf.gz");
    /**
     *
     */
    @Test
    public void testBipaceWithTicPeakFinding() throws IOException {
        List<IFragmentCommand> commands = new ArrayList<>();
        commands.add(new DefaultVarLoader());
        commands.add(new DenseArrayProducer());
        TICPeakFinder tpf = new TICPeakFinder();
        SavitzkyGolayFilter sgf = new SavitzkyGolayFilter();
        sgf.setWindow(12);
        List<AArrayFilter> filters = new LinkedList<>();
        filters.add(sgf);
        tpf.setFilter(filters);
        LoessMinimaBaselineEstimator lmbe = new LoessMinimaBaselineEstimator();
        lmbe.setBandwidth(0.3);
        lmbe.setAccuracy(1.0E-12);
        lmbe.setRobustnessIterations(2);
        lmbe.setMinimaWindow(100);
        tpf.setBaselineEstimator(lmbe);
        tpf.setSnrWindow(50);
        tpf.setPeakSeparationWindow(10);
        tpf.setPeakThreshold(3.0d);
        commands.add(tpf);
        commands.add(new PeakCliqueAlignment());
        IWorkflow w = createWorkflow(tf.newFolder("klda äÄas"), commands, testFiles.getFiles());
        testWorkflow(w);
    }
}
