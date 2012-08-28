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

import java.util.List;

/**
 * Implementation of ContinuousWaveletTransform. Allows to calculate
 * transformation and reconstruction using a given implementation of a wavelet
 * for real valued input.
 *
 * @author hoffmann
 *
 */
public final class ContinuousWaveletTransform {

    private final IWavelet w;

    public ContinuousWaveletTransform(IWavelet w) {
        this.w = w;
    }

    public final double[] apply(final double[] x, final double scale,
            final double... params) {
        final double[] arr = new double[x.length];
        final double[] lut = new double[2 * ((int) scale) + 1];
        final int minIdx = -((int) scale);
        fillLut(lut, scale, minIdx, params);
        // for every tau
        for (int tau = 0; tau < x.length; tau++) {
            final int mint = Math.max(0, (int) (tau - scale + 1));
            final int maxt = Math.min(x.length - 1, (int) (tau + scale - 1));
            //for everz t from mint to maxt
            for (int t = mint; t <= maxt; t++) {
                arr[tau] += (x[t] * lut[map(t - tau, minIdx)]);
            }
            arr[tau] /= (Math.sqrt(scale));
        }
        return arr;
    }

    public final int[] getBoundsForWavelet(final double tau, final double scale, final int length) {
        final int mint = Math.max(0, (int) (tau - scale + 1));
        final int maxt = Math.min(length - 1, (int) (tau + scale - 1));
        return new int[]{mint, maxt};
    }

    public final double[] applyInverseTransform(
            final List<double[]> scaleImages, final List<Double> scales) {
        return applyInverse(scaleImages, scales);
    }

    public final double[] applyInverse(final List<double[]> scaleImages,
            final List<Double> scales, final double... params) {
        final double[] arr = new double[scaleImages.get(0).length];

        for (int si = 0; si < scaleImages.size(); si++) {
            final double s = scales.get(si);
            final double[] lut = new double[2 * ((int) s) + 1];
            final int minIdx = -((int) s);
            fillLut(lut, s, minIdx, params);
            final double[] d = scaleImages.get(si);
            //can we optimize this loop?
            //can we decompose the sum below into
            //so that we simply remove the oldest element 
            //and add the newest one?
            for (int tau = 0; tau < arr.length; tau++) {
                final int mint = Math.max(0, (int) (tau - s + 1));
                final int maxt = Math.min(arr.length - 1, (int) (tau + s - 1));
                for (int t = mint; t <= maxt; t++) {
                    arr[tau] += (d[t] * lut[map(t - tau, minIdx)] / (s * s));
                }
            }
        }
        final double admConstSq = w.getAdmissabilityConstant()
                * w.getAdmissabilityConstant();
        for (int i = 0; i < arr.length; i++) {
            arr[i] /= (admConstSq);
        }
        return arr;
    }

    private final int map(final int delta, final int min) {
        return delta + ((-1) * min);
    }

    private final int revmap(final int idx, final int min) {
        return idx - ((-1) * min);
    }

    private final void fillLut(final double[] lut, final double scale,
            final int minIdx, final double... params) {
        for (int i = 0; i < lut.length; i++) {
            lut[i] = w.applyMotherWavelet((double) revmap(i, minIdx) / scale,
                    params);
        }
    }
}
