package maltcms.commands.filters.array.wavelet;

/**
 * Interface for Wavelet implementations.
 * @author hoffmann
 *
 */
public interface IWavelet {
	public abstract double applyMotherWavelet(final double t,
	        double... params);

	public abstract double getAdmissabilityConstant();
}