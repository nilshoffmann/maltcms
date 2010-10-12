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

import java.util.ArrayList;

import maltcms.commands.distances.ListDistanceFunction;
import maltcms.commands.fragments.PairwiseDistanceCalculator;
import maltcms.commands.fragments.cluster.IClusteringAlgorithm;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.ArrayInt;
import cross.Factory;
import cross.Logging;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragmentFactory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.FileTools;

/**
 * Implementation of progressive, tree based multiple alignment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ProgressiveTreeAlignment extends AFragmentCommand {

	private String pwdistMName;
	private String pwdistNames;
	private ListDistanceFunction ldf;
	private AFragmentCommand warp;
	private String minDistComp;
	private IClusteringAlgorithm ica;
	private boolean minimizeDist;

	private final Logger log = Logging.getLogger(this);
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
		IFileFragment pwd = MaltcmsTools.getPairwiseDistanceFragment();
		TupleND<IFileFragment> files = null;
		if (pwd == null) {
			final PairwiseDistanceCalculator pwdc = Factory.getInstance()
			        .instantiate(PairwiseDistanceCalculator.class);
			files = pwdc.apply(t);
			pwd = MaltcmsTools.getPairwiseDistanceFragment();
		}
		// final ArrayDouble.D2 pwdist = (ArrayDouble.D2) pwd.getChild(
		// this.pwdistMName).getArray();
		// final ArrayChar.D2 names1 = (ArrayChar.D2) pwd.getChild(
		// this.pwdistNames).getArray();
		final ArrayInt.D0 minimizeDistA = (ArrayInt.D0) pwd.getChild(
		        this.minDistComp).getArray();
		if (minimizeDistA.get() == 0) {
			this.minimizeDist = false;
		} else {
			this.minimizeDist = true;
		}
		// Second step:
		// Build a tree, based on a clustering algorithm
		// For each pair of leaves, align the sequences and create a consensus,
		// then proceed and align sequences to consensus sequences etc.
		final AFragmentCommand ica = Factory.getInstance().instantiate(
		        this.guideTreeClass, AFragmentCommand.class);
		ica.setIWorkflow(getIWorkflow());
		((IClusteringAlgorithm) ica).init(pwd, t);
		TupleND<IFileFragment> tple = ica.apply(t);
		ArrayList<IFileFragment> al = new ArrayList<IFileFragment>();
		for (IFileFragment iff : tple) {
			IFileFragment iff2 = FileFragmentFactory.getInstance().create(
			        FileTools.prependDefaultDirs(iff.getName(),
			                this.getClass(), getIWorkflow().getStartupDate()));
			iff2.addSourceFile(iff);
			iff2.save();
			log.debug("Created work file {}", iff2);
			al.add(iff2);
		}
		return new TupleND<IFileFragment>(al);
		// Third
		// return t;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.pwdistMName = cfg.getString("var.pairwise_distance_matrix",
		        "pairwise_distance_matrix");
		this.pwdistNames = cfg.getString("var.pairwise_distance_names",
		        "pairwise_distance_names");
		this.ldf = Factory.getInstance().instantiate(
		        cfg.getString("guide.tree.distance",
		                "maltcms.commands.distances.MZIDynamicTimeWarp"),
		        ListDistanceFunction.class);
		this.minDistComp = cfg.getString("var.minimizing_array_comp",
		        "minimizing_array_comp");
		this.guideTreeClass = cfg.getString("guide.tree.class",
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
