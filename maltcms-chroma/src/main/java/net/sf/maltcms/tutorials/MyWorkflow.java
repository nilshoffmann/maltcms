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
package net.sf.maltcms.tutorials;

import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.pipeline.CommandPipeline;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.workflow.DefaultWorkflow;
import cross.datastructures.workflow.IWorkflow;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import maltcms.commands.fragments.alignment.PeakCliqueAlignment;
import maltcms.commands.fragments.peakfinding.TICPeakFinder;
import maltcms.commands.fragments.preprocessing.DefaultVarLoader;
import maltcms.commands.fragments.preprocessing.DenseArrayProducer;

public class MyWorkflow {

    public static IWorkflow createWorkflow(
        List<IFragmentCommand> commands, List<File> inputFiles) {
        CommandPipeline cp = new CommandPipeline();
        cp.setCommands(commands);
        cp.setInput(FragmentTools.immutable(inputFiles));
        DefaultWorkflow dw = new DefaultWorkflow();
        dw.setCommandSequence(cp);
        System.out.println("Workflow using commands " + dw.getCommandSequence().getCommands());
        System.out.println("Workflow using inputFiles " + dw.getCommandSequence().getInput());
        System.out.println("Workflow using outputDirectory " + dw.getOutputDirectory());
        return dw;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        //Download the test files from
        //http://sf.net/projects/maltcms/files/maltcms/example-data/
        List<File> inputFiles = Arrays.asList(
            new File("glucoseA.cdf"),
            new File("glucoseB.cdf"),
            new File("mannitolA.cdf"),
            new File("mannitolB.cdf"));
        List<IFragmentCommand> cmds = Arrays.asList(new IFragmentCommand[]{
            new DefaultVarLoader(),
            new DenseArrayProducer(),
            new TICPeakFinder(),
            new PeakCliqueAlignment()});
        IWorkflow w = createWorkflow(
            cmds,
            inputFiles);
        try {
            w.call();
            w.save();
        } catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }
}
