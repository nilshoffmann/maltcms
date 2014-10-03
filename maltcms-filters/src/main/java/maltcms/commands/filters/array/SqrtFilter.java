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

import lombok.Data;
import maltcms.commands.filters.AElementFilter;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Will take the square root of all elements.
 *
 * @author Mathias Wilhelm
 * 
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class SqrtFilter extends AArrayFilter {

    private AElementFilter aef = null;

    /**
     * Default constructor.
     */
    public SqrtFilter() {
        super();
        this.aef = new AElementFilter() {
            @Override
            public Double apply(final Double d) {
                return Math.sqrt(d);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Array apply(final Array a) {
        final Array arr = super.apply(a);
        final IndexIterator ii = arr.getIndexIteratorFast();
        while (ii.hasNext()) {
            ii.setDoubleCurrent(this.aef.apply(ii.getDoubleNext()));
        }
        return arr;
    }

    /** {@inheritDoc} */
    @Override
    public SqrtFilter copy() {
        return new SqrtFilter();
    }
}
