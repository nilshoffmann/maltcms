/**
 * 
 */
package maltcms.commands.filters.array;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.tools.MathTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class MovingAverageFilter extends AArrayFilter {

	@Configurable
	private int window = 10;

	public MovingAverageFilter() {
		super();
	}

	public void setWindow(int window) {
		this.window = window;
	}

	@Override
	public Array apply(final Array a) {
		Array arr = super.apply(a);
		if (arr.getRank() == 1) {
			final double[] d = (double[]) arr.get1DJavaArray(double.class);
			arr = Array.factory(MathTools.weightedAverage(this.window, d));
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

}