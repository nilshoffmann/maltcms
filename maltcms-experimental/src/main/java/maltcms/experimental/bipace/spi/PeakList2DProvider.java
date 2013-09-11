/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
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

    private Map<String, PeakList<Peak2D>> map;

    public PeakList2DProvider(List<Tuple2D<IFileFragment, List<Peak2D>>> peaks) {
        map = new HashMap<String, PeakList<Peak2D>>(peaks.size());
        int i = 0;
        for (Tuple2D<IFileFragment, List<Peak2D>> t : peaks) {
            PeakList<Peak2D> pl = new PeakList<Peak2D>();
            pl.setFragment(t.getFirst());
            pl.setPeaks(t.getSecond());
            pl.setIndex(i++);
            map.put(t.getFirst().getUri().toString(), pl);
        }
    }

    @Override
    public PeakList<Peak2D> getPeaks(IFileFragment fragment) {
        return map.get(fragment.getUri().toString());
    }
}
