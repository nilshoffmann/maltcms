/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.spi;

import cross.datastructures.fragments.IFileFragment;
import cross.exception.NotImplementedException;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.PeakList;
import maltcms.experimental.bipace.api.IPeakListProvider;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class PeakList1DProvider implements IPeakListProvider<Peak>{

    @Override
    public PeakList<Peak> getPeaks(IFileFragment fragment) {
        throw new NotImplementedException();
    }
    
}
