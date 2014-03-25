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

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.io.File;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.preprocessing.defaultVarLoader.DefaultVarLoaderWorker;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * Load variables defined by the option default.variables.
 *
 * Additionally tries toload vars defined in the option additional.variables
 * (see cfg/maltcmsvars.properties).
 *
 * @author Nils Hoffmann
 *
 */
@ProvidesVariables(names = {"var.mass_values", "var.intensity_values",
    "var.scan_index", "var.scan_acquisition_time", "var.total_intensity"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class DefaultVarLoader extends AFragmentCommand {

    private final String description = "Loads default and additional variables as defined in the configuration.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILECONVERSION;
    @Configurable
    private List<String> defaultVariables = Collections.emptyList();
    @Configurable
    private List<String> additionalVariables = Collections.emptyList();

    @Override
    public TupleND<IFileFragment> apply(
            final TupleND<IFileFragment> inputFileFragments) {
        log.debug("Running DefaultVarLoader on {} FileFragments!",
                inputFileFragments.size());
        if (defaultVariables.isEmpty() && additionalVariables.isEmpty()) {
            return inputFileFragments;
        }
        //create a new completion service instance for this fragment command
        ICompletionService<File> ics = createCompletionService(File.class);
        for (final IFileFragment f : inputFileFragments) {
            EvalTools.notNull(f, this);
            //create work fragment (retrieves output directory for fragment command)
            IFileFragment workFragment = createWorkFragment(f);
            //create a new worker
            DefaultVarLoaderWorker worker = new DefaultVarLoaderWorker();
            worker.setFileToLoad(f.getUri());
            worker.setFileToSave(workFragment.getUri());
            worker.setDefaultVariables(defaultVariables);
            worker.setAdditionalVariables(additionalVariables);
            //submit to completion service
            ics.submit(worker);
        }
        //wait and retrieve results
        TupleND<IFileFragment> ret = postProcess(ics, inputFileFragments);
        log.debug("Returning {} FileFragments!", ret.size());
        return ret;
    }
}
