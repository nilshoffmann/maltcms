/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import maltcms.datastructures.peak.Peak;
import maltcms.experimental.bipace.datastructures.api.Clique;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class BiPaceResult<T extends Peak> {
    
    private Map<T,Clique<T>> peakToClique = new HashMap<T,Clique<T>>();
    private List<Clique<T>> cliques;
    private Set<PeakList> unmatchedPeaks;
    
}
