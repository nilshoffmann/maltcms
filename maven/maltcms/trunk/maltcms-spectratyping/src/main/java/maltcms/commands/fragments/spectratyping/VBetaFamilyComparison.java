/*
 * $license$
 *
 * $Id$
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
