/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.api;

import cross.datastructures.fragments.IFileFragment;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.PeakList;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public interface IPeakListProvider<T extends Peak> {
    
    public PeakList<T> getPeaks(IFileFragment fragment);
    
}
