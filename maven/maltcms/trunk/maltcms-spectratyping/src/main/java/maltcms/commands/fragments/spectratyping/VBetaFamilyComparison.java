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
package maltcms.commands.fragments.spectratyping;

import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class VBetaFamilyComparison extends AFragmentCommand {
    private String controlFile = null;
    private List<int[]> intervals = Collections.emptyList();
    private double minPercentage = 0.3;

    @Override
    public String getDescription() {
        return "Compares VBetaFamily distributions against a reference control family";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        
        //histogram binning of peaks in window
        
        
        //hamming distance of histograms between control and sample per bar
        
        //test for equal distribution
        
        
//        throw new UnsupportedOperationException("Not supported yet.");
        return in;
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.STATISTICS;
    }
}
