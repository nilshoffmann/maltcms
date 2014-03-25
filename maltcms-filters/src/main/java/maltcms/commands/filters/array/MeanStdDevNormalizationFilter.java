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

import cross.annotations.Configurable;
import lombok.Data;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 * Normalize all values of an array given a normalization string.
 *
 * @author Nils Hoffmann
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class MeanStdDevNormalizationFilter extends AArrayFilter {

    @Configurable
    private double mean = 0;
    @Configurable
    private double stddev = 1;

    public MeanStdDevNormalizationFilter() {
        super();
    }

    public MeanStdDevNormalizationFilter(final double mean, final double stddev) {
        this();
        this.mean = mean;
        this.stddev = stddev;
    }

    @Override
    public Array apply(final Array a) {
        // final Array[] b = super.apply(a);
        final AdditionFilter af = new AdditionFilter(-this.mean);
        // shift by mean
        Array c = af.apply(a);
        // normalize by stddev
        final MultiplicationFilter mf = new MultiplicationFilter(
                1.0d / (this.stddev));
        c = mf.apply(c);
        return c;
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
    }

    @Override
    public MeanStdDevNormalizationFilter copy() {
        return new MeanStdDevNormalizationFilter(mean, stddev);
    }
}
