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
package maltcms.commands.fragments.visualization;

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import hep.aida.ref.Histogram1D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 * Visualizes value distribution of a matrix.
 *
 * @author Nils Hoffmann
 * 
 */
@RequiresVariables(names = {"var.pairwise_distance"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class ScoreDistributionVisualizer extends AFragmentCommand {

    private final String description = "Generates a histogram plot of score distributions from variable pairwise_distance";
    private final WorkflowSlot workflowSlot = WorkflowSlot.VISUALIZATION;
    @Configurable(name = "var.pairwise_distance")
    private String pairwiseDistanceVariable = "pairwise_distance";

    /** {@inheritDoc} */
    @Override
    public void configure(Configuration config) {
        this.pairwiseDistanceVariable = config.getString("var.pairwise_distance", "pairwise_distance");
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        for (final IFileFragment iff : t) {
            try {
                final IVariableFragment pwd = iff.getChild(pairwiseDistanceVariable);
                final Array arr = pwd.getArray();
                final double[] dbl = (double[]) arr.copyTo1DJavaArray();
                final Histogram1D h = new Histogram1D(iff.getName(), dbl);
                final hep.aida.ref.Converter c = new hep.aida.ref.Converter();
                final String s = c.toString(h);
                final File f = new File(
                        getWorkflow().getOutputDirectory(this), "histogram_"
                        + iff.getName());
                try {
                    try (BufferedWriter sw = new BufferedWriter(
                            new FileWriter(f))) {
                        sw.write(s);
                        sw.flush();
                    }
                } catch (final FileNotFoundException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final IOException e) {
                    log.error(e.getLocalizedMessage());
                }
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                        this, WorkflowSlot.STATISTICS, iff);
                getWorkflow().append(dwr);
            } catch (final ResourceNotAvailableException rnae) {
                log.warn("Could not load variable {} from file {}",
                        "pairwise_distance", iff);
            } finally {
                iff.clearArrays();
            }
        }
        return t;
    }
}
