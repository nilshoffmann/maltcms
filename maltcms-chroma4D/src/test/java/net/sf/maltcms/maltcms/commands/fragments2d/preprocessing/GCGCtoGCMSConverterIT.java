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
package net.sf.maltcms.maltcms.commands.fragments2d.preprocessing;

import cross.annotations.AnnotationInspector;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.exception.ResourceNotAvailableException;
import cross.vocabulary.CvResolver;
import cross.vocabulary.ICvResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments2d.preprocessing.Default2DVarLoader;
import maltcms.commands.fragments2d.preprocessing.GCGCToGCMSConverter;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j

public class GCGCtoGCMSConverterIT extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/2D/090306_37_FAME_Standard_1.cdf.gz");

    @Test
    public void testGCGCtoGCMSConverter() throws IOException {
        setLogLevelFor(Default2DVarLoader.class, Level.ALL);
        Default2DVarLoader d = new Default2DVarLoader();
        d.setEstimateModulationTime(false);
        d.setModulationTime(5.0d);
        d.setScanRate(100.0);
        GCGCToGCMSConverter e = new GCGCToGCMSConverter();
        e.setSnrthreshold(5);
        List<IFragmentCommand> list = new ArrayList<>();
        list.add(d);
        list.add(e);
        IWorkflow w = createWorkflow(list, testFiles.getFiles());
        testWorkflow(w);
    }

    @Override
    public TupleND<IFileFragment> testWorkflow(IWorkflow w) {
        try {
            w.getConfiguration().setProperty("output.overwrite", true);
            TupleND<IFileFragment> t = w.call();
            w.save();
            setLogLevelFor(ICvResolver.class, Level.ALL);
            CvResolver cvResolver = new CvResolver();
            //only check the results of the last pipeline element.
            //GCGCtoGCMSConverter does not reference its sourcefile 
            //to avoid confusion/mixing of 1D and 2D chromatogram aspects
            for (IFileFragment ff : t) {
                for (String variable : AnnotationInspector.getProvidedVariables(w.getCommandSequence().getCommands().get(w.getCommandSequence().getCommands().size() - 1))) {
                    log.info("Evaluating results for variable {}, resolved: {}", variable, cvResolver.translate(variable));
                    try {
                        IVariableFragment var = ff.getChild(cvResolver.translate(variable));
                        Array a = var.getArray();
                        Assert.assertNotNull(a);
                    } catch (ResourceNotAvailableException rnae) {
                        Assert.fail(rnae.getLocalizedMessage());
                    }
                }
            }

            return t;
        } catch (Exception e) {
            copyToInspectionDir(w, e);
            log.error("Caught exception while running workflow:", e);
            throw new RuntimeException(e);
        }
    }
}
