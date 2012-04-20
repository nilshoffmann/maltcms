/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package maltcms.commands.filters.array.wavelet;

import maltcms.commands.filters.array.AArrayFilter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * Implementation of MexicanHatWavelet CWT Filter 
 * on top of AArrayFilter.
 * 
 */
public class MexicanHatWaveletFilter extends AArrayFilter {

	@Configurable(value="1",type = double.class)
	private double scale = 1;

	//number from Percival and Walden's Wavelet Methods for Time Series Analysis
	@Configurable(value="0.63628",type = double.class)
	private double variance = 0.63628;

	private final IWavelet w;
	
	private final ContinuousWaveletTransform cwt;

	private Logger log = Logging.getLogger(this);

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
			        .get1DJavaArray(double.class), this.scale,this.variance));
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
