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
package maltcms.commands.fragments.alignment;

import maltcms.commands.fragments.cluster.IClusteringAlgorithm;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import cross.Factory;
import cross.Logging;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;

/**
 * Implementation of progressive, tree based multiple alignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ProgressiveTreeAlignment extends AFragmentCommand {

	private final Logger log = Logging.getLogger(this);

	@Configurable(value = "maltcms.commands.fragments.cluster.UPGMAAlgorithm")
	private String guideTreeClass = "maltcms.commands.fragments.cluster.UPGMAAlgorithm";

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.commands.ICommand#apply(java.lang.Object)
	 */
	@Override
	public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
		// First step:
		// calculate all pairwise distance
		IFileFragment pwd = MaltcmsTools.getPairwiseDistanceFragment(t);
		// Second step:
		// Build a tree, based on a clustering algorithm
		// For each pair of leaves, align the sequences and create a consensus,
		// then proceed and align sequences to consensus sequences etc.
		final AFragmentCommand ica = Factory.getInstance().getObjectFactory()
		        .instantiate(this.guideTreeClass, AFragmentCommand.class);
		ica.setIWorkflow(getIWorkflow());
		((IClusteringAlgorithm) ica).init(pwd, t);
		final TupleND<IFileFragment> tple = ica.apply(t);
		return tple;
		// Third
		// return t;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.guideTreeClass = cfg.getString(getClass().getName()
		        + ".guideTreeClass",
		        "maltcms.commands.fragments.cluster.CompleteLinkageAlgorithm");
	}

	@Override
	public String getDescription() {
		return "Creates a multiple alignment by following a guide tree, based on pairwise simlarities or distances. Creates meta-chromatogram as root of the tree and realigns all other chromatograms to that reference.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
	 */
	@Override
	public WorkflowSlot getWorkflowSlot() {
		return WorkflowSlot.ALIGNMENT;
	}

}
