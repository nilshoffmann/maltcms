/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: DefaultVarLoader.java 115 2010-04-23 15:42:15Z nilshoffmann $
 */
package maltcms.commands.fragments.preprocessing;

import java.io.File;


import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.datastructures.tools.EvalTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.preprocessing.defaultVarLoader.DefaultVarLoaderWorker;
import net.sf.maltcms.execution.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * Load variables defined by the option default.variables. Additionally tries to
 * load vars defined in the option additional.variables (see
 * cfg/maltcmsvars.properties).
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@ProvidesVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.scan_acquisition_time", "var.total_intensity"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class DefaultVarLoader extends AFragmentCommand {

    @Override
    public String toString() {
        return getClass().getName();
    }

    @Override
    public TupleND<IFileFragment> apply(
            final TupleND<IFileFragment> inputFileFragments) {
        log.debug("Running DefaultVarLoader on {} FileFragments!",
                inputFileFragments.size());
        //create a new completion service instance for this fragment command
        ICompletionService<File> ics = createCompletionService(File.class);
        for (final IFileFragment f : inputFileFragments) {
            EvalTools.notNull(f, this);
            //create work fragment (retrieves output directory for fragment command)
            IFileFragment workFragment = createWorkFragment(f);
            //create a new worker
            DefaultVarLoaderWorker worker = new DefaultVarLoaderWorker();
            worker.setFileToLoad(new File(f.getAbsolutePath()));
            worker.setFileToSave(new File(workFragment.getAbsolutePath()));
            //submit to completion service
            ics.submit(worker);
        }
        //wait and retrieve results
        TupleND<IFileFragment> ret = postProcess(ics, inputFileFragments);
        log.debug("Returning {} FileFragments!", ret.size());
        return ret;
    }

    @Override
    public String getDescription() {
        return "Loads default and additional variables as defined in the configuration.";
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILECONVERSION;
    }
}
