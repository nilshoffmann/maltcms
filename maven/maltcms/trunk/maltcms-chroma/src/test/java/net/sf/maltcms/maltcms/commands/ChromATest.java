/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.maltcms.commands;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import maltcms.commands.filters.array.MovingAverageFilter;
import maltcms.commands.filters.array.TopHatFilter;
import maltcms.commands.fragments.alignment.PeakCliqueAlignment;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;
import maltcms.io.ZipResourceExtractor;
import maltcms.test.AFragmentCommandTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author nilshoffmann
 */
public class ChromATest extends AFragmentCommandTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

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
        MovingAverageFilter maf = new MovingAverageFilter();
        maf.setWindow(10);
        TopHatFilter thf = new TopHatFilter();
        thf.setWindow(50);
        tpf.setFilter(Arrays.asList(maf, thf));
        tpf.setSnrWindow(10);
        tpf.setPeakThreshold(0.0d);
        commands.add(tpf);
        commands.add(new PeakCliqueAlignment());
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2));
        try {

            w.call();
            w.save();
        } catch (Exception ex) {
            Assert.fail(ex.getLocalizedMessage());
        }

    }
}
