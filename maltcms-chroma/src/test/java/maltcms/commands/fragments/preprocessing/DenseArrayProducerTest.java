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
package maltcms.commands.fragments.preprocessing;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.alignment.PeakCliqueAlignment;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import maltcms.tools.MaltcmsTools;
import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
//@Category(IntegrationTest.class)
public class DenseArrayProducerTest extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz", "/cdf/1D/glucoseB.cdf.gz");

    public DenseArrayProducerTest() {
        setLogLevelFor(MaltcmsTools.class, Level.DEBUG);
        setLogLevelFor(NetcdfDataSource.class, Level.DEBUG);
    }

    /**
     * Test of apply method, of class DenseArrayProducer.
     */
    @Test
    public void testDirectApply() throws IOException {
        File outputBase = tf.newFolder();
        List<IFragmentCommand> commands = new ArrayList<>();
        commands.add(new DefaultVarLoader());
        ScanExtractor se = new ScanExtractor();
        se.setStartScan(1000);
        se.setEndScan(1500);
        commands.add(se);
        DenseArrayProducer dap = new DenseArrayProducer();
        log.info("DenseArrayProducer: {}", dap);
        commands.add(dap);
        commands.add(new PeakCliqueAlignment());
        IWorkflow w = createWorkflow(outputBase, commands, ecpf.getFiles());
        TupleND<IFileFragment> results;
        //execute workflow
        results = testWorkflow(w);
        //retrieve variables that DenseArrayProducer provides
        Collection<String> variablesToCheck = Arrays.asList(new String[]{"binned_mass_values", "binned_intensity_values", "binned_scan_index"});//AnnotationInspector.getProvidedVariables(DenseArrayProducer.class);
        for (IFileFragment f : results) {
            for (String variable : variablesToCheck) {
                log.info("Checking variable: {}", variable);
                try {
                    //get structure, no data
                    IVariableFragment v = f.getChild(variable, true);
                    //load array explicitly
                    Array a = v.getArray();
                    Assert.assertNotNull(a);
                    //remove
                    f.removeChild(v);
                    //get structure and data
//                        v = f.getChild(variable);
//                        a = v.getArray();
//                        Assert.assertNotNull(a);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail(e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Test of apply method, of class DenseArrayProducer.
     */
    @Test
    public void testApply() throws IOException {
        File outputBase = tf.newFolder();
        List<IFragmentCommand> commands = new ArrayList<>();
        commands.add(new DefaultVarLoader());
        ScanExtractor se = new ScanExtractor();
        se.setStartScan(1000);
        se.setEndScan(1500);
        commands.add(se);
        DenseArrayProducer dap = new DenseArrayProducer();
        log.info("DenseArrayProducer: {}", dap);
        commands.add(dap);
        IWorkflow w = createWorkflow(outputBase, commands, ecpf.getFiles());
        TupleND<IFileFragment> results;
        //execute workflow
        results = testWorkflow(w);
        //retrieve variables that DenseArrayProducer provides
        Collection<String> variablesToCheck = Arrays.asList(new String[]{"binned_mass_values", "binned_intensity_values", "binned_scan_index"});//AnnotationInspector.getProvidedVariables(DenseArrayProducer.class);
        for (IFileFragment f : results) {
            for (String variable : variablesToCheck) {
                log.info("Checking variable: {}", variable);
                try {
                    //get structure, no data
                    IVariableFragment v = f.getChild(variable, true);
                    //load array explicitly
                    Array a = v.getArray();
                    Assert.assertNotNull(a);
                    //remove
                    f.removeChild(v);
                    //get structure and data
//                        v = f.getChild(variable);
//                        a = v.getArray();
//                        Assert.assertNotNull(a);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail(e.getLocalizedMessage());
                }
            }
        }
    }
}
