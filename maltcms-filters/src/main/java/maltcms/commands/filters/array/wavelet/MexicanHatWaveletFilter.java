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
package maltcms.commands.filters.array.wavelet;

import cross.annotations.Configurable;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.AArrayFilter;
import org.apache.commons.configuration.Configuration;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 * Implementation of MexicanHatWavelet CWT Filter on top of AArrayFilter.
 *
 * @author Nils Hoffmann
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

    /**
     * <p>Constructor for MexicanHatWaveletFilter.</p>
     */
    public MexicanHatWaveletFilter() {
        super();
        w = new MexicanHatWavelet();
        cwt = new ContinuousWaveletTransform(w);
    }

    /**
     * <p>Constructor for MexicanHatWaveletFilter.</p>
     *
     * @param scale a double.
     * @param variance a double.
     * @since 1.3.2
     */
    public MexicanHatWaveletFilter(double scale, double variance) {
        this();
        this.scale = scale;
        this.variance = variance;
    }

    /**
     * <p>Setter for the field <code>scale</code>.</p>
     *
     * @param scale a double.
     */
    public void setScale(final double scale) {
        this.scale = scale;
    }

    /**
     * <p>Getter for the field <code>variance</code>.</p>
     *
     * @return a double.
     */
    public double getVariance() {
        return variance;
    }

    /**
     * <p>Setter for the field <code>variance</code>.</p>
     *
     * @param variance a double.
     */
    public void setVariance(double variance) {
        this.variance = variance;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.scale = cfg.getDouble(this.getClass().getName() + ".scale", 2.0);
        this.variance = cfg.getDouble(this.getClass().getName() + ".variance",
                0.63628);
    }

    /**
     * <p>getContinuousWaveletTransform.</p>
     *
     * @return a {@link maltcms.commands.filters.array.wavelet.ContinuousWaveletTransform} object.
     */
    public ContinuousWaveletTransform getContinuousWaveletTransform() {
        return this.cwt;
    }

    /** {@inheritDoc} */
    @Override
    public MexicanHatWaveletFilter copy() {
        return new MexicanHatWaveletFilter(scale, variance);
    }
}
