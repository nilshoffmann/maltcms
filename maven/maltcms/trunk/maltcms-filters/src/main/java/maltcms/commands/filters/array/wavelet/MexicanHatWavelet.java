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
 *  $Id: MexicanHatWavelet.java 426 2012-02-09 19:38:11Z nilshoffmann $
 */
package maltcms.commands.filters.array.wavelet;

public final class MexicanHatWavelet implements IWavelet {
	/**
	 * First param in params is expected to be the variance sigma. For
	 * performance reasons, no null checking is performed, so please ensure
	 * to supply at least one value in params.
	 */
    @Override
	public final double applyMotherWavelet(final double t,
	        final double... params) {
		final double tsq = Math.pow(t, 2.0d);
		final double val = (1.0d - (tsq / params[0]));
		final double expf = Math.exp((-tsq) / 2.0d * params[0]);
		final double norm = 2.0d / (Math.sqrt(3.0 * Math.sqrt(params[0])) * Math
		        .pow(Math.PI, 0.25d));
		return norm * val * expf;
	}

	private final double admissConst = 4.0 * Math.sqrt(Math.PI) / 3.0;

    @Override
	public final double getAdmissabilityConstant() {
		return admissConst;
	}
}
