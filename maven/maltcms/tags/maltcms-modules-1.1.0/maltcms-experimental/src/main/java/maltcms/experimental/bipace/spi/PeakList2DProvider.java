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
