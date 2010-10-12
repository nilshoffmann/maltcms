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
 * $Id$
 */

package maltcms.commands.fragments;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import annotations.ProvidesVariables;
import annotations.RequiresVariables;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.IWorkflowElement;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.EvalTools;
import cross.tools.FileTools;
import cross.tools.FragmentTools;
import cross.tools.StringTools;

/**
 * Load variables defined by the option default.variables. Additionally tries to
 * load vars defined in the option additional.variables (see
 * cfg/maltcmsvars.properties).
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@RequiresVariables(names = {})
@ProvidesVariables(names = { "var.mass_values", "var.intensity_values",
        "var.scan_index", "var.scan_acquisition_time", "var.total_intensity" })
public class DefaultVarLoader extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this.getClass());
	private int nthreads = 5;

	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		this.log.debug("Running DefaultVarLoader on {} FileFragments!", t
		        .size());
		// ExecutorService es = Executors.newFixedThreadPool(Math.min(nthreads,
		// t
		// .size()));
		// log.info("Running {} threads",Math.min(nthreads, t.size()));
		final IWorkflowElement iwe = this;
		final TupleND<IFileFragment> ret = new TupleND<IFileFragment>();
		for (final IFileFragment f : t) {
			// Runnable r = new Runnable() {
			//
			// @Override
			// public void run() {
			EvalTools.notNull(f, this);
			final String filename = StringTools.removeFileExt(f.getName());
			final IFileFragment iff = FileFragmentFactory.getInstance().create(
			        FileTools.prependDefaultDirs(filename + ".cdf", iwe
			                .getClass(), getIWorkflow().getStartupDate()));
			iff.addSourceFile(f);
			FragmentTools.loadDefaultVars(iff);
			FragmentTools.loadAdditionalVars(iff);
			iff.save();
			final DefaultWorkflowResult dwr = new DefaultWorkflowResult(
			        new File(iff.getAbsolutePath()), iwe, getWorkflowSlot());
			getIWorkflow().append(dwr);
			ret.add(iff);
			// }
			//
			// };
			// es.submit(r);
		}
		// es.shutdown();
		// try {
		// es.awaitTermination(10, TimeUnit.HOURS);
		// } catch (InterruptedException e) {
		// log.error(e.getLocalizedMessage());
		// }
		EvalTools.notNull(ret, this);
		this.log.debug("Returning {} FileFragments!", ret.size());
		return ret;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.nthreads = cfg.getInt("maltcms.pipelinethreads", 5);
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
