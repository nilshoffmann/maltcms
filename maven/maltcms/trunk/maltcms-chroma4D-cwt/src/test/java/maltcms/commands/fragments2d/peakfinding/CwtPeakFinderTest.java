/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.commands.fragments2d.peakfinding;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import maltcms.commands.fragments2d.preprocessing.Default2DVarLoader;
import maltcms.io.ZipResourceExtractor;
import maltcms.test.AFragmentCommandTest;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author nilshoffmann
 */
public class CwtPeakFinderTest extends AFragmentCommandTest {

//    @Rule
//    public TemporaryFolder tf = new TemporaryFolder();
//
//    @Test
//    public void testPeakFinder() {
//
//        File inputFile = ZipResourceExtractor.extract(
//                "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz", tf.newFolder(
//                "cdf2D"));
//
//        Default2DVarLoader d2vl = new Default2DVarLoader();
//        d2vl.setEstimateModulationTime(true);
//
//        CwtPeakFinder cpf = new CwtPeakFinder();
//
//        List<IFragmentCommand> l = new LinkedList<IFragmentCommand>();
//        l.add(d2vl);
//        l.add(cpf);
//        IWorkflow w = new DefaultWorkflow();
//        TupleND<IFileFragment> tmp = new TupleND<IFileFragment>(new FileFragment(inputFile.
//                getAbsoluteFile()));
//        CommandPipeline cp = new CommandPipeline();
//        cp.setCommands(l);
//        cp.setInput(tmp);
//        w.setCommandSequence(cp);
//        System.out.println(
//                "Running " + w.getCommandSequence().getCommands().size() + " commands on " + cp.
//                getInput().size() + " input files.");
//
//        for (IFragmentCommand cmd : l) {
//            cmd.setWorkflow(w);
//            tmp = cmd.apply(tmp);
//        }
////        w.save();
//    }
//
//    @After
//    public void cleanUp() {
//        tf.delete();
//    }
//
//    public static void main(String[] args) {
//        CwtPeakFinderTest test = new CwtPeakFinderTest();
//        test.testPeakFinder();
//    }
}
