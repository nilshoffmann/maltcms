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
package net.sf.maltcms.maltcms.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.test.ZipResourceExtractor;
import maltcms.test.AFragmentCommandTest;

import org.junit.Test;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;
import java.util.LinkedList;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.SavitzkyGolayFilter;
import maltcms.commands.fragments.alignment.PeakCliqueAlignment;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import cross.test.IntegrationTest;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Nils Hoffmann
 */
@Category(IntegrationTest.class)
public class BiPaceTest extends AFragmentCommandTest {
    
    /**
     *
     */
    @Test
    public void testChromA() {
        File dataFolder = tf.newFolder("chromaTestData");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        File inputFile2 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("chromaTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        commands.add(new DefaultVarLoader());
        commands.add(new DenseArrayProducer());
        TICPeakFinder tpf = new TICPeakFinder();
        SavitzkyGolayFilter sgf = new SavitzkyGolayFilter();
        sgf.setWindow(12);
        List<AArrayFilter> filters = new LinkedList<AArrayFilter>();
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
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2));
        testWorkflow(w);
    }
}
