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
package cross.datastructures.workflow;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tuple.TupleND;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.ArrayDouble;

@Slf4j
public class DefaultWorkflowTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public File inputDirectory;
    public File outputDirectory;
    public String[] fileExtensions = new String[]{"cdf", "nc"};
    public boolean checkCommandDependencies = true;
    public DefaultWorkflow dw;

    @Before
    public void setUp() throws Exception {
        inputDirectory = folder.newFolder("input");
        outputDirectory = folder.newFolder("output");
        dw = getDefaultWorkflow();
    }

    @After
    public void tearDown() throws Exception {
//       folder.
    }

    public List<File> getDefaultTestFiles() {
        File[] files = new File[4];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(inputDirectory, "testFile" + i + ".cdf");
            FileFragment ff = new FileFragment(files[i]);
            VariableFragment testData = new VariableFragment(ff, "variable0");
            testData.setArray(new ArrayDouble.D2(100, 10));
            ff.save();
        }
        return Arrays.asList(files);
    }

    public List<IFragmentCommand> getDefaultFragmentCommands() {
        List<IFragmentCommand> cmds = new ArrayList<IFragmentCommand>();
        cmds.add(new FragmentCommandMockA());
        cmds.add(new FragmentCommandMockB());
        cmds.add(new FragmentCommandMockC());
        return cmds;
    }

    public CommandPipeline getDefaultCommandSequence(
        List<IFragmentCommand> commands, List<File> inputFiles) {

        CommandPipeline cp = new CommandPipeline();
        cp.setCommands(commands);
        List<IFileFragment> files = new ArrayList<IFileFragment>();
        for (File file : inputFiles) {
            log.info("Adding input file: {}", file.getAbsolutePath());
            files.add(new FileFragment(file));
        }
        cp.setInput(new TupleND<IFileFragment>(files));
        cp.setCheckCommandDependencies(checkCommandDependencies);

        return cp;
    }

    public DefaultWorkflow getDefaultWorkflow() {
        log.info("Building test command sequence");
        CommandPipeline cp = getDefaultCommandSequence(
            getDefaultFragmentCommands(), getDefaultTestFiles());
        log.info("Creating workflow");
        DefaultWorkflow dw = new DefaultWorkflow();
        dw.setExecuteLocal(true);
        dw.setCommandSequence(cp);
        return dw;
    }

    @Test
    public void testWorkflow() {
        log.info("Testing workflow!");
        try {
            TupleND<IFileFragment> tpl = dw.call();
            for (IFileFragment ff : tpl) {
                log.info("Workflow created fragment: {}", ff.getUri());
            }
        } catch (Exception e) {
            fail();
        }
    }
}
