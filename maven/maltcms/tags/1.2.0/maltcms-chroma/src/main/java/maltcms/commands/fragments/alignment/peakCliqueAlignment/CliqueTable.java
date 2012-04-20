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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import maltcms.datastructures.peak.Clique;
import maltcms.datastructures.peak.Peak;
import ucar.ma2.ArrayBoolean;

/**
 *
 * @author nils
 */
/**
 *
 * CliqueTable allows for easy retrieval of common clique information.
 */
public class CliqueTable {

    private ArrayBoolean.D2 arr = null;
    private HashMap<String, Integer> placeMap = null;

    public CliqueTable(TupleND<IFileFragment> fragments, List<Clique> l) {
        arr = new ArrayBoolean.D2(l.size(), fragments.size());
        placeMap = new LinkedHashMap<String, Integer>();
        int j = 0;
        for (IFileFragment f : fragments) {
            placeMap.put(f.getName(), j++);
        }
        int i = 0;
        for (Clique c : l) {
            for (Peak p : c.getPeakList()) {
                arr.set(i, placeMap.get(p.getAssociation()), true);
            }
            i++;
        }
    }

    public int getNumberOfPeaksWithinCliques(IFileFragment iff) {
        int sum = 0;
        int j = placeMap.get(iff.getName());

        for (int i = 0; i < arr.getShape()[0]; i++) {
            sum += (arr.get(i, j) ? 1 : 0);
        }
        return sum;
    }

    public List<Clique> getCommonCliques(IFileFragment a, IFileFragment b,
            List<Clique> cliques) {
        List<Clique> commonCliques = new ArrayList<Clique>();
        int k = placeMap.get(a.getName());
        int l = placeMap.get(b.getName());
        for (int i = 0; i < arr.getShape()[0]; i++) {
            if (arr.get(i, k) && arr.get(i, l)) {
                commonCliques.add(cliques.get(i));
            }
        }
        return commonCliques;
    }
}
