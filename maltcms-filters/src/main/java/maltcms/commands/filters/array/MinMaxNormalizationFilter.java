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
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 * Normalize all values of an array given a normalization string.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class MinMaxNormalizationFilter extends AArrayFilter {

    @Configurable
    private double min = 0;
    @Configurable
    private double max = 1;

    public MinMaxNormalizationFilter() {
        super();
    }

    public MinMaxNormalizationFilter(final double min, final double max) {
        this();
        this.min = min;
        this.max = max;
    }

    @Override
    public Array apply(final Array a) {
        // final Array[] b = super.apply(a);
        final AdditionFilter af = new AdditionFilter(-this.min);
        // shift by minimum
        Array c = af.apply(a);
        // normalize by max-min
        final MultiplicationFilter mf = new MultiplicationFilter(
            1.0d / (this.max - this.min));
        c = mf.apply(c);
        return c;
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
    }

    @Override
    public MinMaxNormalizationFilter copy() {
        return new MinMaxNormalizationFilter(min, max);
    }
}
