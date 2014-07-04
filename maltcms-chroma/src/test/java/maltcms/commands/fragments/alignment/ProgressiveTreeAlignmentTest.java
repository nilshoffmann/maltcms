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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.SavitzkyGolayFilter;
import maltcms.commands.fragments.cluster.PairwiseDistanceCalculator;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.commands.fragments.preprocessing.ScanExtractor;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ZipResourceExtractor;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */
//@Category(IntegrationTest.class)
public class ProgressiveTreeAlignmentTest extends AFragmentCommandTest {

    @Before
    public void configureLogging() {
        setLogLevelFor("maltcms", Level.OFF);
        setLogLevelFor("cross", Level.OFF);
        setLogLevelFor("net.sf.maltcms", Level.OFF);
        setLogLevelFor("maltcms.commands.fragments.alignment.PairwiseDistanceCalculator", Level.INFO);
        setLogLevelFor("maltcms.commands.fragments.alignment.ProgressiveTreeAlignment", Level.INFO);
    }

    /**
     *
     */
    @Test
    public void testProgressiveTreeAlignment() throws IOException {
        File dataFolder = tf.newFolder("chromaTest Data ö");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        File inputFile2 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
        File inputFile3 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolA.cdf.gz", dataFolder);
        File inputFile4 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("chromaTest Out ü");
        List<IFragmentCommand> commands = new ArrayList<>();
        commands.add(new DefaultVarLoader());
        ScanExtractor se = new ScanExtractor();
        se.setStartScan(1600);
        se.setEndScan(2100);
        commands.add(se);
        commands.add(new DenseArrayProducer());
        TICPeakFinder tpf = new TICPeakFinder();
        SavitzkyGolayFilter sgf = new SavitzkyGolayFilter();
        sgf.setWindow(12);
        List<AArrayFilter> filters = new LinkedList<>();
        filters.add(sgf);
        tpf.setFilter(filters);
        LoessMinimaBaselineEstimator lmbe = new LoessMinimaBaselineEstimator();
        lmbe.setMinimaWindow(50);
        lmbe.setBandwidth(0.3);
        lmbe.setAccuracy(1.0E-12);
        lmbe.setRobustnessIterations(2);
//        lmbe.setMinimaWindow(50);
        tpf.setBaselineEstimator(lmbe);
        tpf.setSnrWindow(50);
        tpf.setPeakSeparationWindow(10);
        tpf.setPeakThreshold(3.0d);
        commands.add(tpf);
        commands.add(new PeakCliqueAlignment());
        commands.add(new PairwiseDistanceCalculator());
        commands.add(new ProgressiveTreeAlignment());
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2, inputFile3, inputFile4));
        testWorkflow(w);
    }

}
