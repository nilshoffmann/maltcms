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
import java.util.Random;
import lombok.Data;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * Add additive gaussian noise to array values with given mean and std
 * deviation.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class AddRandomNoise extends AArrayFilter {

    private Random rg = null;
    @Configurable
    private double mean = 0.0d;
    @Configurable
    private double stddev = 1.0d;

    public AddRandomNoise() {
        super();
        this.rg = new Random();
    }

    public AddRandomNoise(final double mean1, final double stddev1) {
        this();
        this.mean = mean1;
        this.stddev = stddev1;
    }

    @Override
    public Array apply(final Array a) {
        final Array b = super.apply(a);
        final IndexIterator ii = b.getIndexIteratorFast();
        while (ii.hasNext()) {
            final double v = ii.getDoubleNext();
            ii.setDoubleCurrent(v + (this.stddev * this.rg.nextGaussian())
                + this.mean);
        }
        return b;
    }

    @Override
    public void configure(final Configuration cfg) {
        this.mean = cfg.getDouble(this.getClass().getName() + ".mean");
        this.stddev = cfg.getDouble(this.getClass().getName() + ".stddev");
    }

    @Override
    public AddRandomNoise copy() {
        return new AddRandomNoise(mean, stddev);
    }
}
