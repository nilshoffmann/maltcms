package maltcms.commands.filters.array.wavelet;

public final class MexicanHatWavelet implements IWavelet {
	/**
	 * First param in params is expected to be the variance sigma. For
	 * performance reasons, no null checking is performed, so please ensure
	 * to supply at least one value in params.
	 */
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

	public final double getAdmissabilityConstant() {
		return admissConst;
	}
}
