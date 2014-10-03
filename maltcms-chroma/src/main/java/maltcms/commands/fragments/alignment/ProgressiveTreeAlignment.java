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
package maltcms.commands.fragments.alignment;

import maltcms.commands.fragments.cluster.IClusteringAlgorithm;
import maltcms.tools.MaltcmsTools;

import org.apache.commons.configuration.Configuration;

import cross.Factory;
import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of progressive, tree based multiple alignment.
 *
 * @author Nils Hoffmann
 * 
 */
@RequiresVariables(names = {"var.pairwise_distance_matrix"})
@Data
@Slf4j
@ServiceProvider(service = AFragmentCommand.class)
public class ProgressiveTreeAlignment extends AFragmentCommand {

    @Configurable(value = "maltcms.commands.fragments.cluster.UPGMAAlgorithm")
    private String guideTreeClass = "maltcms.commands.fragments.cluster.UPGMAAlgorithm";

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        // First step:
        // calculate all pairwise distance
        IFileFragment pwd = MaltcmsTools.getPairwiseDistanceFragment(t);
        // Second step:
        // Build a tree, based on a clustering algorithm
        // For each pair of leaves, align the sequences and create a consensus,
        // then proceed and align sequences to consensus sequences etc.
        AFragmentCommand ica = Factory.getInstance().getObjectFactory().instantiate(this.guideTreeClass, AFragmentCommand.class);
        initSubCommand(ica);
        ((IClusteringAlgorithm) ica).init(pwd, t);
        final TupleND<IFileFragment> tple = ica.apply(t);
        return tple;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
//        this.guideTreeClass = cfg.getString(getClass().getName()
//                + ".guideTreeClass",
//                "maltcms.commands.fragments.cluster.CompleteLinkageAlgorithm");
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Creates a multiple alignment by following a guide tree, based on pairwise simlarities or distances. Creates meta-chromatogram as root of the tree and realigns all other chromatograms to that reference.";
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /** {@inheritDoc} */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.ALIGNMENT;
    }
}
