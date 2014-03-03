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
package maltcms.ui.charts;

import cross.Factory;
import cross.IConfigurable;
import java.util.Arrays;
import java.util.List;
import maltcms.commands.filters.array.NormalizationFilter;
import org.apache.commons.configuration.Configuration;
import org.jfree.chart.plot.Plot;
import ucar.ma2.Array;

/**
 * Abstract base class for Charts of Type T extending Plot of JFreeChart.
 *
 * @author Nils Hoffmann
 *
 * @param <T extends Plot>
 */
public abstract class AChart<T extends Plot> implements IConfigurable {

    private double yaxis_min = -1;
    private double yaxis_max = -1;

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    @Override
    public void configure(final Configuration cfg) {
    }

    public abstract T create();

    public abstract String getTitle();

    /**
     * @return the yaxis_max
     */
    public double getYaxis_max() {
        return this.yaxis_max;
    }

    /**
     * @return the yaxis_min
     */
    public double getYaxis_min() {
        return this.yaxis_min;
    }

    public List<Array> normalize(final List<Array> c,
        final String normalization, final boolean normalize_global) {
        final NormalizationFilter nf = new NormalizationFilter(normalization,
            false, normalize_global);
        nf.configure(Factory.getInstance().getConfiguration());
        final Array[] as = nf.apply(c.toArray(new Array[]{}));
        return Arrays.asList(as);
    }

    public abstract void setTitle(String s);

    /**
     * @param yaxis_max the yaxis_max to set
     */
    public void setYaxis_max(final double yaxis_max) {
        this.yaxis_max = yaxis_max;
    }

    /**
     * @param yaxis_min the yaxis_min to set
     */
    public void setYaxis_min(final double yaxis_min) {
        this.yaxis_min = yaxis_min;
    }
}
