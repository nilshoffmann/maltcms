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
package maltcms.commands.filters.array;

import cross.annotations.Configurable;
import lombok.Data;
import maltcms.commands.filters.AElementFilter;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Add a given double to elements of an array.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class AdditionFilter extends AArrayFilter {

    private AElementFilter aef = null;
    @Configurable
    private double add = 0.0d;

    public AdditionFilter() {
        super();
    }

    public AdditionFilter(final double add) {
        this();
        this.aef = new AElementFilter() {
            @Override
            public Double apply(final Double d) {
                return d + add;
            }
        };
    }

    /*
     * (non-Javadoc)
     *
     * @see maltcms.ucar.ma2.ArrayFilter#filter(maltcms.ucar.ma2.Array)
     */
    @Override
    public Array apply(final Array a) {
        final Array b = super.apply(a);
        final IndexIterator ii = b.getIndexIteratorFast();
        double next = 0.0d;
        while (ii.hasNext()) {
            next = ii.getDoubleNext();
            ii.setDoubleCurrent(this.aef.apply(next));
        }
        return b;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.add = cfg.getDouble(this.getClass().getName() + ".add");
        this.aef = new AElementFilter() {
            @Override
            public Double apply(Double t) {
                return Double.valueOf(t.doubleValue() + add);
            }
        };
    }

    @Override
    public AdditionFilter copy() {
        return new AdditionFilter(add);
    }
}
