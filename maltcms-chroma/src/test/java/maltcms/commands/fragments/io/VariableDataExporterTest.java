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
package maltcms.commands.fragments.io;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.workflow.IWorkflow;
import cross.datastructures.workflow.IWorkflowFileResult;
import cross.datastructures.workflow.IWorkflowResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.peakfinding.TICPeakFinderIT;
import maltcms.io.csv.CSVReader;
import maltcms.io.csv.CSVWriter;
import maltcms.test.AFragmentCommandTest;
import maltcms.test.ExtractClassPathFiles;
import maltcms.test.ZipResourceExtractor;
import maltcms.tools.ArrayTools;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class VariableDataExporterTest extends AFragmentCommandTest {

    @Rule
    public ExtractClassPathFiles testFiles = new ExtractClassPathFiles(tf,
            "/cdf/1D/glucoseA.cdf.gz");

    /**
     *
     */
    @Test
    public void testVariableDataExporter() throws IOException {
        List<IFragmentCommand> commands = new ArrayList<>();
        VariableDataExporter vde = new VariableDataExporter();
        vde.setVarNames(Arrays.asList("var.total_intensity"));
        commands.add(vde);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        testWorkflow(w);
        List<IWorkflowResult> results = w.getResultsFor(new CSVWriter());
        Assert.assertEquals(1, results.size());
        IWorkflowResult workflowResult = results.get(0);
        FileFragment testFragment = new FileFragment(testFiles.getFiles().get(0));

        CSVReader reader = new CSVReader();
        reader.setFieldSeparator("\t");
        reader.setFirstLineHeaders(false);
        reader.setSkipCommentLines(true);
        try (BufferedReader is = new BufferedReader(new FileReader(((IWorkflowFileResult) workflowResult).getFile()))) {
            String line;
            int lineCounter = 0;
            ArrayDouble.D1 array = new ArrayDouble.D1(testFragment.getChild("total_intensity").getDimensions()[0].getLength());
            while ((line = is.readLine()) != null) {
                log.info("Parsing line {}", line);
                if (!line.isEmpty()) {
                    array.setDouble(lineCounter, Double.parseDouble(line));
                }
                lineCounter++;
            }
            Assert.assertEquals(testFragment.getChild("total_intensity").getDimensions()[0].getLength(), lineCounter - 1);
            Array difference = ArrayTools.diff(array, testFragment.getChild("total_intensity").getArray());
            Assert.assertEquals(0, ArrayTools.integrate(difference), 0.0001);
        }
    }

    @Test
    public void testVariableDataExporterWithNamespacedVariable() throws IOException {
        List<IFragmentCommand> commands = new ArrayList<>();
        VariableDataExporter vde = new VariableDataExporter();
        vde.setVarNames(Arrays.asList("var.total_intensity"));
        commands.add(vde);
        IWorkflow w = createWorkflow(commands, testFiles.getFiles());
        testWorkflow(w);
        List<IWorkflowResult> results = w.getResultsFor(new CSVWriter());
        Assert.assertEquals(1, results.size());
        IWorkflowResult workflowResult = results.get(0);
        FileFragment testFragment = new FileFragment(testFiles.getFiles().get(0));

        CSVReader reader = new CSVReader();
        reader.setFieldSeparator("\t");
        reader.setFirstLineHeaders(false);
        reader.setSkipCommentLines(true);
        try (BufferedReader is = new BufferedReader(new FileReader(((IWorkflowFileResult) workflowResult).getFile()))) {
            String line;
            int lineCounter = 0;
            ArrayDouble.D1 array = new ArrayDouble.D1(testFragment.getChild("total_intensity").getDimensions()[0].getLength());
            while ((line = is.readLine()) != null) {
                log.info("Parsing line {}", line);
                if (!line.isEmpty()) {
                    array.setDouble(lineCounter, Double.parseDouble(line));
                }
                lineCounter++;
            }
            Assert.assertEquals(testFragment.getChild("total_intensity").getDimensions()[0].getLength(), lineCounter - 1);
            Array difference = ArrayTools.diff(array, testFragment.getChild("total_intensity").getArray());
            Assert.assertEquals(0, ArrayTools.integrate(difference), 0.0001);
        }
    }
}
