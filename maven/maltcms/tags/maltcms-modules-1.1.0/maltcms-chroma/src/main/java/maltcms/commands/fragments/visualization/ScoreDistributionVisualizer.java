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
package maltcms.commands.fragments.visualization;

import hep.aida.ref.Histogram1D;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


import ucar.ma2.Array;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.DefaultWorkflowResult;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ResourceNotAvailableException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = {"pairwise_distance"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class ScoreDistributionVisualizer extends AFragmentCommand {

    @Override
    public String toString() {
        return getClass().getName();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        for (final IFileFragment iff : t) {
            try {
                final IVariableFragment pwd = iff.getChild("pairwise_distance");
                final Array arr = pwd.getArray();
                final double[] dbl = (double[]) arr.copyTo1DJavaArray();
                final Histogram1D h = new Histogram1D(iff.getName(), dbl);
                final hep.aida.ref.Converter c = new hep.aida.ref.Converter();
                final String s = c.toString(h);
                final File f = new File(
                        getWorkflow().getOutputDirectory(this), "histogram_"
                        + iff.getName());
                try {
                    final BufferedWriter sw = new BufferedWriter(
                            new FileWriter(f));
                    sw.write(s);
                    sw.flush();
                    sw.close();
                } catch (final FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (final IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                final DefaultWorkflowResult dwr = new DefaultWorkflowResult(f,
                        this, WorkflowSlot.STATISTICS, iff);
                getWorkflow().append(dwr);
            } catch (final ResourceNotAvailableException rnae) {
                log.warn("Could not load variable {} from file {}",
                        "pairwise_distance", iff);
            }
        }
        return t;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Generates a histogram plot of score distributions from variable pairwise_distance";
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.VISUALIZATION;
    }
}