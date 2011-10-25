/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.maltcms.commands.fragments2d.preprocessing;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import maltcms.commands.fragments2d.preprocessing.Default2DVarLoader;
import maltcms.io.ZipResourceExtractor;
import maltcms.test.AFragmentCommandTest;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author nilshoffmann
 */
public class Default2DVarLoaderTest extends AFragmentCommandTest {

    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    @Test
    public void testPeakFinder() {
        File dataFolder = tf.newFolder("chroma4DTestData");
        File outputBase = tf.newFolder("chroma4DTestOut");
        File inputFile = ZipResourceExtractor.extract(
                "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz", dataFolder);


        Default2DVarLoader d2vl = new Default2DVarLoader();
        d2vl.setEstimateModulationTime(false);
        d2vl.setModulationTime(5.0d);

        List<IFragmentCommand> l = new LinkedList<IFragmentCommand>();
        l.add(d2vl);

        IWorkflow w = createWorkflow(outputBase, l, Arrays.asList(inputFile));
//        System.out.println(
//                "Running " + w.getCommandSequence().getCommands().size() + " commands on " + w.
//                getCommandSequence().
//                getInput().size() + " input files.");
        try {
            w.call();
            w.save();
        } catch (Exception ex) {
            Logger.getLogger(Default2DVarLoaderTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void cleanUp() {
//        tf.delete();
    }

    public static void main(String[] args) {
        Default2DVarLoaderTest test = new Default2DVarLoaderTest();
        test.testPeakFinder();
    }
}
