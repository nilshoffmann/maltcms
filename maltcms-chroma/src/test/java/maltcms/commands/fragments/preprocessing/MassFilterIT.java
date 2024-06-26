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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import maltcms.io.andims.NetcdfDataSource;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import maltcms.tools.MaltcmsTools;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Nils Hoffmann
 */


public class MassFilterIT extends AFragmentCommandTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MassFilterIT.class);

    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz");

    public MassFilterIT() {
        setLogLevelFor(MaltcmsTools.class, Level.DEBUG);
        setLogLevelFor(NetcdfDataSource.class, Level.DEBUG);
    }

    /**
     * Test of apply method, of class DenseArrayProducer.
     */
    @Test
    public void testDirectApply() throws IOException {
        List<IFragmentCommand> commands = new ArrayList<>();
        ScanExtractor se = new ScanExtractor();
        se.setStartScan(1000);
        se.setEndScan(1500);
        commands.add(se);
        MassFilter mf = new MassFilter();
        mf.setExcludeMasses(Arrays.asList(new String[]{"73.0","74.0","75.0"}));
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        TupleND<IFileFragment> results;
        //execute workflow
        results = testWorkflow(w);
        //retrieve variables that DenseArrayProducer provides
        Collection<String> variablesToCheck = Arrays.asList(new String[]{"mass_values"});//AnnotationInspector.getProvidedVariables(DenseArrayProducer.class);
        for (IFileFragment f : results) {
            for (String variable : variablesToCheck) {
                log.info("Checking variable: {}", variable);
                try {
                    //get structure, no data
                    IVariableFragment v = f.getChild(variable, true);
                    //load array explicitly
                    double[] d = (double[])v.getArray().get1DJavaArray(double.class);
                    int idx = Arrays.binarySearch(d, 73.0);
                    Assert.assertTrue(idx<0);
                    //remove
                    f.removeChild(v);
                } catch (Exception e) {
                    log.warn(e.getLocalizedMessage());
                    Assert.fail(e.getLocalizedMessage());
                }
            }
        }
    }
}
