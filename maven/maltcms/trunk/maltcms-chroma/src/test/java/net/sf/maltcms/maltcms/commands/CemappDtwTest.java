/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.maltcms.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import maltcms.commands.filters.array.MovingAverageFilter;
import maltcms.commands.filters.array.TopHatFilter;
import maltcms.commands.fragments.alignment.PeakCliqueAlignment;
import maltcms.commands.fragments.cluster.PairwiseDistanceCalculator;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.MziDtwWorkerFactory;
import maltcms.commands.fragments.cluster.pairwiseDistanceCalculator.TicDtwWorkerFactory;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.io.ZipResourceExtractor;
import maltcms.math.functions.DtwPairwiseSimilarity;
import maltcms.math.functions.similarities.ArrayCos;
import maltcms.math.functions.similarities.ArrayLp;
import maltcms.test.AFragmentCommandTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cross.commands.fragments.AFragmentCommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.workflow.IWorkflow;

/**
 *
 * @author nilshoffmann
 */
public class CemappDtwTest extends AFragmentCommandTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();
    
    public ApplicationContext getContext() {
        ApplicationContext sac = new ClassPathXmlApplicationContext("cfg/xml/test/cemappdtwtest.xml");
        return sac;
    }

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

//    @Test
//    public void testCemappDtwTICConstrained() {
//        File dataFolder = tf.newFolder("testCemappDtwConstrained");
//        File inputFile1 = ZipResourceExtractor.extract(
//                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
//        File inputFile2 = ZipResourceExtractor.extract(
//                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
//        File inputFile3 = ZipResourceExtractor.extract(
//                "/cdf/1D/mannitolA.cdf.gz", dataFolder);
//        File inputFile4 = ZipResourceExtractor.extract(
//                "/cdf/1D/mannitolB.cdf.gz", dataFolder);
//        File outputBase = tf.newFolder("testCemappDtwConstrainedTestOut");
//        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
//        commands.add(new DefaultVarLoader());
//        commands.add(new DenseArrayProducer());
//        TICPeakFinder tpf = new TICPeakFinder();
//        MovingAverageFilter maf = new MovingAverageFilter();
//        maf.setWindow(10);
//        TopHatFilter thf = new TopHatFilter();
//        thf.setWindow(50);
//        tpf.setFilter(Arrays.asList(maf, thf));
//        tpf.setSnrWindow(10);
//        tpf.setPeakThreshold(0.0d);
//        commands.add(tpf);
//        commands.add(new PeakCliqueAlignment());
//        commands.add(createPairwiseDistanceCalculatorTIC(true,0,true,0.2d));
//        
//        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
//                inputFile1, inputFile2, inputFile3, inputFile4));
//        try {
//
//            w.call();
//            w.save();
//        } catch (Exception ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//    }
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
        MovingAverageFilter maf = new MovingAverageFilter();
        maf.setWindow(10);
        TopHatFilter thf = new TopHatFilter();
        thf.setWindow(50);
        tpf.setFilter(Arrays.asList(maf, thf));
        tpf.setSnrWindow(10);
        tpf.setPeakThreshold(0.0d);
        commands.add(tpf);
        commands.add(new PeakCliqueAlignment());
        commands.add(createPairwiseDistanceCalculatorMZI(true,0,false,0.5d));
        
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2, inputFile3, inputFile4));
        try {

            w.call();
            w.save();
        } catch (Exception ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

    }
}
