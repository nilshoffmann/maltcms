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
import cross.tools.MathTools;
import lombok.Data;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 * @author Nils Hoffmann
 *
 *
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class MovingMedianFilter extends AArrayFilter {

    @Configurable
    private int window = 10;

    public MovingMedianFilter() {
        super();
    }

    public MovingMedianFilter(int window) {
        this();
        this.window = window;
    }

    @Override
    public Array apply(final Array a) {
        Array arr = super.apply(a);
        if (arr.getRank() == 1) {
            final double[] d = (double[]) arr.get1DJavaArray(double.class);
            final ArrayDouble.D1 ret = new ArrayDouble.D1(d.length);
            for (int i = 0; i < arr.getShape()[0]; i++) {
                ret.set(i, MathTools.median(d, i - (this.window), i
                    + (this.window)));
            }
            arr = ret;
        } else {
            throw new IllegalArgumentException(
                "Can only work on arrays of dimension 1");
        }
        return arr;
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.window = cfg.getInt(this.getClass().getName() + ".window", 10);
    }

    @Override
    public MovingMedianFilter copy() {
        return new MovingMedianFilter();
    }
}
