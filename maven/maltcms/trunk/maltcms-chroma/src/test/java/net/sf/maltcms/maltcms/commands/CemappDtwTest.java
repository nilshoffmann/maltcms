/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package net.sf.maltcms.maltcms.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import maltcms.commands.fragments.alignment.PeakCliqueAlignment;
import maltcms.commands.fragments.cluster.PairwiseDistanceCalculator;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.MziDtwWorkerFactory;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.TicDtwWorkerFactory;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import cross.io.misc.ZipResourceExtractor;
import maltcms.math.functions.DtwPairwiseSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.math.functions.similarities.ArrayLp;
import maltcms.test.AFragmentCommandTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cross.commands.fragments.AFragmentCommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;
import java.util.LinkedList;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.AArrayFilter;
import maltcms.commands.filters.array.SavitzkyGolayFilter;
import maltcms.commands.fragments.peakfinding.ticPeakFinder.LoessMinimaBaselineEstimator;
import maltcms.test.SetupLogging;

/**
 *
 * @author nilshoffmann
 */
@Slf4j
public class CemappDtwTest extends AFragmentCommandTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    @Test
    public void testCemappDtwMZIFull() {
        File dataFolder = tf.newFolder("testCemappDtwFull");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
//        File inputFile2 = ZipResourceExtractor.extract(
//                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
//        File inputFile3 = ZipResourceExtractor.extract(
//                "/cdf/1D/mannitolA.cdf.gz", dataFolder);
        File inputFile4 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("testCemappDtwFullTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        commands.add(new DefaultVarLoader());
        commands.add(new DenseArrayProducer());
        commands.add(createPairwiseDistanceCalculatorMZI(false,0,true,
                1.0d));
        
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile4));
        try {

            w.call();
            w.save();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getLocalizedMessage());
        }

    }
//    
    @Test
    public void testCemappDtwTICFull() {
        File dataFolder = tf.newFolder("testCemappDtwFull");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
//        File inputFile2 = ZipResourceExtractor.extract(
//                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
//        File inputFile3 = ZipResourceExtractor.extract(
//                "/cdf/1D/mannitolA.cdf.gz", dataFolder);
        File inputFile4 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("testCemappDtwFullTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        commands.add(new DefaultVarLoader());
        commands.add(new DenseArrayProducer());
        commands.add(createPairwiseDistanceCalculatorTIC(false,0,true,
                1.0d));
        
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile4));
        try {

            w.call();
            w.save();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getLocalizedMessage());
        }

    }

    public AFragmentCommand createPairwiseDistanceCalculatorTIC(boolean useAnchors, int anchorRadius, boolean globalBand, double bandWidthPercentage) {
        PairwiseDistanceCalculator pdc = new PairwiseDistanceCalculator();
        TicDtwWorkerFactory factory = new TicDtwWorkerFactory();
        factory.setUseAnchors(useAnchors);
        factory.setAnchorRadius(anchorRadius);
        factory.setGlobalBand(globalBand);
        factory.setBandWidthPercentage(bandWidthPercentage);
        DtwPairwiseSimilarity dps = new DtwPairwiseSimilarity();
        dps.setDenseMassSpectraSimilarity(new ArrayLp());
        factory.setSimilarity(dps);
        pdc.setWorkerFactory(factory);
        return pdc;
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
        return pdc;
    }

    @Test
    public void testCemappDtwTICConstrained() {
        File dataFolder = tf.newFolder("testCemappDtwConstrained");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        File inputFile2 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
        File inputFile3 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolA.cdf.gz", dataFolder);
        File inputFile4 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("testCemappDtwConstrainedTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        commands.add(new DefaultVarLoader());
        commands.add(new DenseArrayProducer());
        TICPeakFinder tpf = new TICPeakFinder();
//        SavitzkyGolayFilter sgf = new SavitzkyGolayFilter();
//        sgf.setWindow(12);
//        List<AArrayFilter> filters = new LinkedList<AArrayFilter>();
//        filters.add(sgf);
//        tpf.setFilter(filters);
//        LoessMinimaBaselineEstimator lmbe = new LoessMinimaBaselineEstimator();
//        lmbe.setBandwidth(0.3);
//        lmbe.setAccuracy(1.0E-12);
//        lmbe.setRobustnessIterations(2);
//        lmbe.setMinimaWindow(100);
//        tpf.setBaselineEstimator(lmbe);
//        tpf.setSnrWindow(50);
//        tpf.setPeakSeparationWindow(10);
//        tpf.setPeakThreshold(3.0d);
        commands.add(tpf);
        commands.add(new PeakCliqueAlignment());
        commands.add(createPairwiseDistanceCalculatorTIC(true,0,true,0.2d));
        
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2, inputFile3, inputFile4));
        try {

            w.call();
            w.save();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getLocalizedMessage());
        }

    }
//    
    @Test
    public void testCemappDtwMZIConstrained() {
        File dataFolder = tf.newFolder("testCemappDtwConstrained");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        File inputFile2 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
        File inputFile3 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolA.cdf.gz", dataFolder);
        File inputFile4 = ZipResourceExtractor.extract(
                "/cdf/1D/mannitolB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("testCemappDtwConstrainedTestOut");
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
        commands.add(createPairwiseDistanceCalculatorMZI(true,0,false,0.5d));
        
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2, inputFile3, inputFile4));
        try {

            w.call();
            w.save();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getLocalizedMessage());
        }

    }
}
