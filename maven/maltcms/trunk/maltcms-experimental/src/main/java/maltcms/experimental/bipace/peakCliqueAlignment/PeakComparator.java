/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.experimental.bipace.peakCliqueAlignment;

import java.util.Comparator;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author nils
 */
public class PeakComparator
        implements Comparator<Peak> {

    @Override
    public int compare(final Peak o1, final Peak o2) {
        if (o1.getScanIndex() == o2.getScanIndex()) {
            return 0;
        } else if (o1.getScanIndex() < o2.getScanIndex()) {
            return -1;
        }
        return 1;
    }
}
