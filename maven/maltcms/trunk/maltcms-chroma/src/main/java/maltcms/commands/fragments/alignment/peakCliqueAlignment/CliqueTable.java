/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    private HashMap<IFileFragment, Integer> placeMap = null;

    public CliqueTable(TupleND<IFileFragment> fragments, List<Clique> l) {
        arr = new ArrayBoolean.D2(l.size(), fragments.size());
        placeMap = new LinkedHashMap<IFileFragment, Integer>();
        int j = 0;
        for (IFileFragment f : fragments) {
            placeMap.put(f, j++);
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
        int j = placeMap.get(iff);

        for (int i = 0; i < arr.getShape()[0]; i++) {
            sum += (arr.get(i, j) ? 1 : 0);
        }
        return sum;
    }

    public List<Clique> getCommonCliques(IFileFragment a, IFileFragment b,
            List<Clique> cliques) {
        List<Clique> commonCliques = new ArrayList<Clique>();
        int k = placeMap.get(a);
        int l = placeMap.get(b);
        for (int i = 0; i < arr.getShape()[0]; i++) {
            if (arr.get(i, k) && arr.get(i, l)) {
                commonCliques.add(cliques.get(i));
            }
        }
        return commonCliques;
    }
}
