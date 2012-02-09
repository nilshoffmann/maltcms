/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross.datastructures.workflow;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.pipeline.CommandPipeline;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cross.datastructures.tuple.TupleND;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Rule;
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
            log.info("Adding input file: {}",file.getAbsolutePath());
            files.add(new FileFragment(file));
        }
        cp.setInput(new TupleND<IFileFragment>(files));
        cp.setCheckCommandDependencies(checkCommandDependencies);

        return cp;
    }

    public DefaultWorkflow getDefaultWorkflow() {
        log.info("Building test command sequence");
        CommandPipeline cp = getDefaultCommandSequence(
                getDefaultFragmentCommands(),getDefaultTestFiles());
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
            for(IFileFragment ff:tpl) {
                log.info("Workflow created fragment: {}",ff.getAbsolutePath());
            }
        } catch (Exception e) {
            fail();
        }
    }
    
}
