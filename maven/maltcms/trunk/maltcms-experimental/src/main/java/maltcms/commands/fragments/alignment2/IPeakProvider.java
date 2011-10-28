/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments.alignment2;

import cross.datastructures.fragments.IFileFragment;
import java.util.List;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IPeakProvider<T extends Peak> {
    
    public List<T> getPeaks(IFileFragment fragment);
    
//    public void initializePeaks(TupleND<IFileFragment> t, HashMap<String, List<T>> fragmentToPeaksfragmentToPeaks, HashMap<String, Integer> columnMap);
    
}
