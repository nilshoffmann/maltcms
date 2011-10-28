/*
 * $license$
 *
 * $Id$
 */
package maltcms.commands.fragments2d.peakfinding.bbh;

import java.awt.Point;
import java.util.List;
import maltcms.datastructures.peak.Peak2D;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
public class BiPaceBidirectionalBestHit implements IBidirectionalBestHit {

    @Override
    public List<List<Point>> getBidiBestHitList(List<List<Peak2D>> peaklists) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double sim(Peak2D p1, Peak2D p2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        
    }

    @Override
    public void configure(Configuration c) {
        
    }
    
}
