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
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    public CliqueTable(TupleND<IFileFragment> fragments, List<Clique<IBipacePeak>> l) {
        arr = new ArrayBoolean.D2(l.size(), fragments.size());
        placeMap = new LinkedHashMap<String, Integer>();
        int j = 0;
        for (IFileFragment f : fragments) {
            placeMap.put(f.getName(), j++);
        }
        int i = 0;
        for (Clique<IBipacePeak> c : l) {
            for (IBipacePeak p : c.getPeakList()) {
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

    public List<Clique<IBipacePeak>> getCommonCliques(IFileFragment a, IFileFragment b,
            List<Clique<IBipacePeak>> cliques) {
        List<Clique<IBipacePeak>> commonCliques = new ArrayList<Clique<IBipacePeak>>();
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
