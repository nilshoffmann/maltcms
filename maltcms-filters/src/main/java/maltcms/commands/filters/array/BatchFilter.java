/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.filters.array;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 *
 * @author Nils Hoffmann
 */
public class BatchFilter {

    /**
     * Creates a deep copy of the filters in the argument list.
     *
     * @param filters a list of filters to be copied
     * @return the list of copied filters
     *
     * @since 1.3.1
     */
    public static List<AArrayFilter> copy(List<AArrayFilter> filters) {
        List<AArrayFilter> copies = new ArrayList<AArrayFilter>(filters.size());
        for (AArrayFilter filter : filters) {
            copies.add((AArrayFilter) filter.copy());
        }
        return copies;
    }

    /**
     * Applies filters in argument list to the provided array.
     * The array is returned unchanged, if the filters list is empty.
     *
     * @param a       the array to be filtered
     * @param filters the filters to be applied
     * @return the filtered array
     */
    public static Array applyFilters(Array a, List<AArrayFilter> filters) {
        Array b = a;
        for (AArrayFilter filter : filters) {
            LoggerFactory.getLogger(BatchFilter.class).info(
                "Applying Filter: {}", filter.getClass().getName());
            b = filter.apply(b);
        }
        return b;
    }
}
