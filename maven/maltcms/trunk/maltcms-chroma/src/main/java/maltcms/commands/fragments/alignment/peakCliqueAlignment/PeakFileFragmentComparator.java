/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.commands.fragments.alignment.peakCliqueAlignment;

import maltcms.experimental.bipace.peakCliqueAlignment.PeakComparator;
import java.util.Comparator;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author nils
 */
public class PeakFileFragmentComparator implements Comparator<Peak> {

    @Override
    public int compare(final Peak o1, final Peak o2) {
        final int i = new PeakComparator().compare(o1, o2);
        if (i == 0) {
            return o1.getAssociation().getName().compareTo(o2.getAssociation().getName());
        }
        return i;
    }
}
