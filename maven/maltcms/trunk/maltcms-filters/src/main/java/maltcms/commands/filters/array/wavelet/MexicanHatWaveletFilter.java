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
package maltcms.commands.filters.array.wavelet;

import maltcms.commands.filters.array.AArrayFilter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MexicanHatWavelet CWT Filter on top of AArrayFilter.
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 */
@Slf4j
public class MexicanHatWaveletFilter extends AArrayFilter {

    @Configurable(value = "1", type = double.class)
    private double scale = 1;
    //number from Percival and Walden's Wavelet Methods for Time Series Analysis
    @Configurable(value = "0.63628", type = double.class)
    private double variance = 0.63628;
    private final IWavelet w;
    private final ContinuousWaveletTransform cwt;

    public MexicanHatWaveletFilter() {
        super();
        w = new MexicanHatWavelet();
        cwt = new ContinuousWaveletTransform(w);
    }

    public void setScale(final double scale) {
        this.scale = scale;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    @Override
    public Array apply(final Array a) {
        Array arr = super.apply(a);

        if (arr.getRank() == 1) {
            return Array.factory(cwt.apply((double[]) arr
                    .get1DJavaArray(double.class), this.scale, this.variance));
        } else {
            throw new IllegalArgumentException(getClass().getSimpleName()
                    + " can only be applied to one dimensional arrays!");
        }
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.scale = cfg.getDouble(this.getClass().getName() + ".scale", 2.0);
        this.variance = cfg.getDouble(this.getClass().getName() + ".variance",
                0.63628);
    }

    public ContinuousWaveletTransform getContinuousWaveletTransform() {
        return this.cwt;
    }
}
