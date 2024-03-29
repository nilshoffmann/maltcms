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

import cross.cache.CacheType;
import cross.commands.fragments.AFragmentCommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.Fragments;
import cross.datastructures.workflow.IWorkflow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.SavitzkyGolayFilter;
import maltcms.commands.fragments.cluster.PairwiseDistanceCalculator;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.MziDtwWorkerFactory;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.commands.fragments.preprocessing.ScanExtractor;
import maltcms.commands.fragments.warp.ChromatogramWarp2;
import maltcms.math.functions.DtwPairwiseSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Nils Hoffmann
 */


public class CemappDtwMziIT extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz", "/cdf/1D/glucoseB.cdf.gz", "/cdf/1D/mannitolB.cdf.gz");

    @Test
    public void testCemappDtwMZIFull() throws IOException {
        setLogLevelFor("cross", Level.OFF);
        setLogLevelFor("maltcms", Level.OFF);
        setLogLevelFor("cross.datastructures.workflow.DefaultWorkflow", Level.INFO);
        setLogLevelFor("cross.datastructures.pipeline.CommandPipeline", Level.INFO);
        Fragments.setDefaultFragmentCacheType(CacheType.NONE);
        List<IFragmentCommand> commands = new ArrayList<>();
        ScanExtractor se = new ScanExtractor();
        se.setStartScan(1000);
        se.setEndScan(1500);
        commands.add(new DefaultVarLoader());
        commands.add(se);
        commands.add(new DenseArrayProducer());
        commands.add(createPairwiseDistanceCalculatorMZI(false, 0, true,
                1.0d));
        commands.add(new CenterStarAlignment());
        ChromatogramWarp2 cwarp2 = new ChromatogramWarp2();
        cwarp2.setIndexedVars(new LinkedList<String>());
        commands.add(cwarp2);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        testWorkflow(w);
    }

    @Test
    public void testCemappDtwMZIFullEhcache() throws IOException {
        setLogLevelFor("cross", Level.OFF);
        setLogLevelFor("maltcms", Level.OFF);
        setLogLevelFor("cross.datastructures.workflow.DefaultWorkflow", Level.INFO);
        setLogLevelFor("cross.datastructures.pipeline.CommandPipeline", Level.INFO);
        Fragments.setDefaultFragmentCacheType(CacheType.EHCACHE);
        List<IFragmentCommand> commands = new ArrayList<>();
        ScanExtractor se = new ScanExtractor();
        se.setStartScan(1000);
        se.setEndScan(1500);
        commands.add(new DefaultVarLoader());
        commands.add(se);
        commands.add(new DenseArrayProducer());
        commands.add(createPairwiseDistanceCalculatorMZI(false, 0, true,
                1.0d));
        commands.add(new CenterStarAlignment());
        ChromatogramWarp2 cwarp2 = new ChromatogramWarp2();
        cwarp2.setIndexedVars(new LinkedList<String>());
        commands.add(cwarp2);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        testWorkflow(w);
    }

    public AFragmentCommand createPairwiseDistanceCalculatorMZI(boolean useAnchors, int anchorRadius, boolean globalBand, double bandWidthPercentage) {
        PairwiseDistanceCalculator pdc = new PairwiseDistanceCalculator();
        MziDtwWorkerFactory factory = new MziDtwWorkerFactory();
        factory.setUseAnchors(useAnchors);
        factory.setAnchorRadius(anchorRadius);
        factory.setGlobalBand(globalBand);
        factory.setBandWidthPercentage(bandWidthPercentage);
        DtwPairwiseSimilarity dps = new DtwPairwiseSimilarity();
        dps.setDenseMassSpectraSimilarity(new ArrayCos());
        dps.setMatchWeight(2.25);
        factory.setSimilarity(dps);
        pdc.setWorkerFactory(factory);
        factory.setSaveDtwMatrix(false);
        factory.setSavePairwiseSimilarityMatrix(false);
        factory.setSaveLayoutImage(true);
        return pdc;
    }

    /**
     *
     */
    @Test
    public void testCemappDtwMZIConstrained() throws IOException {
        setLogLevelFor("cross", Level.OFF);
        setLogLevelFor("maltcms", Level.OFF);
        setLogLevelFor("cross.datastructures.workflow.DefaultWorkflow", Level.INFO);
        setLogLevelFor("cross.datastructures.pipeline.CommandPipeline", Level.INFO);
        Fragments.setDefaultFragmentCacheType(CacheType.NONE);
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
        lmbe.setBandwidth(0.4);
        lmbe.setAccuracy(1.0E-12);
        lmbe.setRobustnessIterations(2);
        lmbe.setMinimaWindow(50);
        tpf.setBaselineEstimator(lmbe);
        tpf.setSnrWindow(50);
        tpf.setPeakSeparationWindow(10);
        tpf.setPeakThreshold(5.0d);
        commands.add(tpf);
        commands.add(new PeakCliqueAlignment());
        commands.add(createPairwiseDistanceCalculatorMZI(true, 0, false, 0.5d));
        commands.add(new CenterStarAlignment());
        ChromatogramWarp2 cwarp2 = new ChromatogramWarp2();
        cwarp2.setIndexedVars(new LinkedList<String>());
        commands.add(cwarp2);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        testWorkflow(w);
    }

    /**
     *
     */
    @Test
    public void testCemappDtwMZIConstrainedEhcache() throws IOException {
        setLogLevelFor("cross", Level.OFF);
        setLogLevelFor("maltcms", Level.OFF);
        setLogLevelFor("cross.datastructures.workflow.DefaultWorkflow", Level.INFO);
        setLogLevelFor("cross.datastructures.pipeline.CommandPipeline", Level.INFO);
        Fragments.setDefaultFragmentCacheType(CacheType.EHCACHE);
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
        lmbe.setBandwidth(0.4);
        lmbe.setAccuracy(1.0E-12);
        lmbe.setRobustnessIterations(2);
        lmbe.setMinimaWindow(50);
        tpf.setBaselineEstimator(lmbe);
        tpf.setSnrWindow(50);
        tpf.setPeakSeparationWindow(10);
        tpf.setPeakThreshold(5.0d);
        commands.add(tpf);
        commands.add(new PeakCliqueAlignment());
        commands.add(createPairwiseDistanceCalculatorMZI(true, 0, false, 0.5d));
        commands.add(new CenterStarAlignment());
        ChromatogramWarp2 cwarp2 = new ChromatogramWarp2();
        cwarp2.setIndexedVars(new LinkedList<String>());
        commands.add(cwarp2);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        testWorkflow(w);
    }
}
