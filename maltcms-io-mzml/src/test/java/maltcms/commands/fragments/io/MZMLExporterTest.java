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
package maltcms.commands.fragments.io;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.preprocessing.ScanExtractor;
import maltcms.io.andims.NetcdfDataSource;
import maltcms.io.xml.mzML.MZMLDataSource;
import maltcms.io.xml.mzML.MZMLValidator;
import maltcms.io.xml.mzML.MZMLValidator.ValidationResult;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.apache.log4j.Level;
import org.junit.Rule;
import org.junit.Test;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class MZMLExporterTest extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles ecpf = new ExtractClassPathFiles(tf,
        "/cdf/1D/glucoseA.cdf.gz", "/cdf/1D/glucoseB.cdf.gz");

    public MZMLExporterTest() {
        setLogLevelFor(NetcdfDataSource.class, Level.DEBUG);
        setLogLevelFor(MZMLDataSource.class, Level.DEBUG);
        setLogLevelFor(MZMLExporter.class, Level.INFO);
        setLogLevelFor(MZMLExporterWorker.class, Level.INFO);
    }

    /**
     * Test of apply method, of class DenseArrayProducer.
     */
    @Test
    public void testApply() throws Exception {
        System.err.println("testApply");
        File outputBase = tf.newFolder("mzmlExporterTestOut");
        List<IFragmentCommand> commands = new ArrayList<IFragmentCommand>();
        ScanExtractor scanExtractor = new ScanExtractor();
        scanExtractor.setStartScan(10);
        scanExtractor.setEndScan(100);
        commands.add(scanExtractor);
        MZMLExporter mzmlExporter = new MZMLExporter();
        mzmlExporter.setValidate(true);
        commands.add(mzmlExporter);
        System.err.println("testApply creating workflow");
        IWorkflow w = createWorkflow(outputBase, commands, ecpf.getFiles());
        TupleND<IFileFragment> results;
        System.err.println("Test workflow");
//        //execute workflow
        results = testWorkflow(w);
        Collection<String> variablesToCheck = Arrays.asList(new String[]{"mass_values", "intensity_values", "scan_index", "total_intensity", "scan_acquisition_time"});//AnnotationInspector.getProvidedVariables(DenseArrayProducer.class);
        Assert.assertTrue("Expected more than " + results.size() + " results for input files " + ecpf.getFiles(), results.size() > 0);
        int i = 0;
        for (IFileFragment f : results) {
            File processedCdfFile = new File(f.getUri());
            Assert.assertTrue(processedCdfFile.exists() && processedCdfFile.isFile());
            File mzMLFile = new File(processedCdfFile.getParentFile(), StringTools.removeFileExt(f.getName()) + ".mzml");
            System.err.println("mzML file: " + mzMLFile.getAbsolutePath());
            Assert.assertTrue(mzMLFile.exists() && mzMLFile.isFile());
            MZMLValidator validator = new MZMLValidator();
            ValidationResult result = validator.validateMzML(mzMLFile);
            Assert.assertTrue("MzML file did not pass validation!", result.isValid());
            FileFragment ff = new FileFragment(mzMLFile);
            File scanexDir = w.getOutputDirectory(scanExtractor);
            IFileFragment originalFileFragment = new FileFragment(new File(scanexDir, StringTools.removeFileExt(f.getName()) + ".cdf"));
            for (String variable : variablesToCheck) {
                log.info("Checking variable: {}", variable);
                //get structure, no data
                IVariableFragment v = ff.getChild(variable, true);
                //load array explicitly
                Array a = v.getArray();
                Assert.assertNotNull("Array for variable " + variable + " must not be null!", a);
                IVariableFragment v1 = originalFileFragment.getChild(variable, true);
//					v1.setRange(new Range(10,100,1));
                Array a1 = v1.getArray();
                Assert.assertNotNull(a1);
                Assert.assertEquals("Checking variable " + variable, a.getElementType(), a1.getElementType());
                Assert.assertEquals("Checking variable " + variable, a.getShape()[0], a1.getShape()[0]);
//                ArrayTools.checkFullArrayEquality(a, a1);
            }
        }
    }
}
