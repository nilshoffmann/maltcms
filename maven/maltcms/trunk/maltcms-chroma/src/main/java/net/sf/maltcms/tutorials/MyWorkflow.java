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

    public static void main(String[] args) {
        //Download the test files from 
        //http://sf.net/projects/maltcms/files/maltcms/example-data/
        List<File> inputFiles = Arrays.asList(
                new File("glucoseA.cdf"),
                new File("glucoseB.cdf"),
                new File("mannitolA.cdf"),
                new File("mannitolB.cdf")
        );
        List<IFragmentCommand> cmds = Arrays.asList(new IFragmentCommand[]{
            new DefaultVarLoader(),
            new DenseArrayProducer(),
            new TICPeakFinder(),
            new PeakCliqueAlignment()}
        );
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
