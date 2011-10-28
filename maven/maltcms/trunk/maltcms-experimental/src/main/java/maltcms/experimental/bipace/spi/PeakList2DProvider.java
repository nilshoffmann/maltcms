/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.spi;

import maltcms.experimental.bipace.datastructures.spi.Peak2D;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import maltcms.experimental.bipace.PeakList;
import maltcms.experimental.bipace.api.IPeakListProvider;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class PeakList2DProvider implements IPeakListProvider<Peak2D> {

    private Map<String,PeakList<Peak2D>> map;
    
    public PeakList2DProvider(List<Tuple2D<IFileFragment,List<Peak2D>>> peaks) {
        map = new HashMap<String,PeakList<Peak2D>>(peaks.size());
        int i = 0;
        for(Tuple2D<IFileFragment,List<Peak2D>> t:peaks){
            PeakList<Peak2D> pl = new PeakList<Peak2D>();
            pl.setFragment(t.getFirst());
            pl.setPeaks(t.getSecond());
            pl.setIndex(i++);
            map.put(t.getFirst().getAbsolutePath(),pl);
        }
    }
    
    @Override
    public PeakList<Peak2D> getPeaks(IFileFragment fragment) {
        return map.get(fragment.getAbsolutePath());
    }
    
}
