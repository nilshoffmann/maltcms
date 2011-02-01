package maltcms.commands.filters.array.wavelet;

import java.util.List;

/**
 * Implementation of ContinuousWaveletTransform.
 * Allows to calculate transformation and reconstruction
 * using a given implementation of a wavelet for real 
 * valued input.
 * @author hoffmann
 *
 */
public final class ContinuousWaveletTransform {

	private final IWavelet w;
	
	public ContinuousWaveletTransform(IWavelet w) {
		this.w = w;
	}
	
	public final double[] apply(final double[] x, final double scale, final double...params) {
		final double[] arr = new double[x.length];
		final double[] lut = new double[2 * ((int)scale) + 1];
		final int minIdx = -((int)scale);
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

	public final double[] applyInverseTransform(
	        final List<double[]> scaleImages, final List<Double> scales) {
		return applyInverse(scaleImages, scales);
	}

	public final double[] applyInverse(final List<double[]> scaleImages,
	        final List<Double> scales, final double...params) {
		final double[] arr = new double[scaleImages.get(0).length];

		for (int si = 0; si < scaleImages.size(); si++) {
			final double s = scales.get(si);
			final double[] lut = new double[2 * ((int)s) + 1];
			final int minIdx = -((int)s);
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