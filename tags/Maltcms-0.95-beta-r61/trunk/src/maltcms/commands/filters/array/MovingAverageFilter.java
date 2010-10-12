/**
 * 
 */
package maltcms.commands.filters.array;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.tools.MathTools;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class MovingAverageFilter extends AArrayFilter {

	private int window = 10;

	@Override
	public Array[] apply(Array[] a) {
		Array[] b = super.apply(a);
		Array[] arrays = new Array[b.length];
		int k = 0;
		for (Array arr : b) {
			if (arr.getRank() == 1) {
				double[] d = (double[]) arr.get1DJavaArray(double.class);
				ArrayDouble.D1 ret = new ArrayDouble.D1(d.length);
				for (int i = 0; i < arr.getShape()[0]; i++) {
					ret.set(i, MathTools.average(d, i - (window / 2), i
					        + (window / 2)));
				}
				arrays[k++] = ret;
			} else {
				throw new IllegalArgumentException(
				        "Can only work on arrays of dimension 1");
			}
		}
		return arrays;
	}

	@Override
	public void configure(Configuration cfg) {
		super.configure(cfg);
		this.window = cfg.getInt(this.getClass() + ".window", 10);
	}

}
