/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.commands.fragments.preprocessing;

import cross.annotations.AnnotationInspector;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.io.misc.ZipResourceExtractor;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.test.AFragmentCommandTest;
import maltcms.tools.MaltcmsTools;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class DenseArrayProducerTest extends AFragmentCommandTest {
    
    /**
     *
     */
    @Rule
    public TemporaryFolder tf = new TemporaryFolder();

    public DenseArrayProducerTest() {
        setLogLevelFor(MaltcmsTools.class, Level.DEBUG);
        setLogLevelFor(NetcdfDataSource.class, Level.DEBUG);
    }

    /**
     * Test of apply method, of class DenseArrayProducer.
     */
    @Test
    public void testApply() {
        File dataFolder = tf.newFolder("chromaTestData");
        File inputFile1 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseA.cdf.gz", dataFolder);
        File inputFile2 = ZipResourceExtractor.extract(
                "/cdf/1D/glucoseB.cdf.gz", dataFolder);
        File outputBase = tf.newFolder("chromaTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        commands.add(new DefaultVarLoader());
        DenseArrayProducer dap = new DenseArrayProducer();
        log.info("DenseArrayProducer: {}",dap);
        commands.add(dap);
        IWorkflow w = createWorkflow(outputBase, commands, Arrays.asList(
                inputFile1, inputFile2));
        TupleND<IFileFragment> results;
        try {
            //execute workflow
            results = w.call();
            w.save();
            //retrieve variables that DenseArrayProducer provides
            Collection<String> variablesToCheck = Arrays.asList(new String[]{"binned_mass_values","binned_intensity_values","binned_scan_index"});//AnnotationInspector.getProvidedVariables(DenseArrayProducer.class);
            for(IFileFragment f:results) {
                for(String variable:variablesToCheck) {
                    log.info("Checking variable: {}",variable);
                    try {
                        //get structure, no data
                        IVariableFragment v = f.getChild(variable, true);
                        Array a = v.getArray();
                        Assert.assertNotNull(a);
                        //remove
                        f.removeChild(v);
                        //get structure and data
//                        v = f.getChild(variable);
//                        a = v.getArray();
//                        Assert.assertNotNull(a);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Assert.fail(e.getLocalizedMessage());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getLocalizedMessage());
        }
    }

    
}
