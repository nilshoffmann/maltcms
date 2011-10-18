/**
 * 
 */
package maltcms.commands.filters.array;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.exception.NotImplementedException;
import lombok.Data;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@Data
@ServiceProvider(service = AArrayFilter.class)
public class SavitzkyGolayFilter extends AArrayFilter {

    @Configurable
    private int window = 10;

    public SavitzkyGolayFilter() {
        super();
    }

    @Override
    public Array apply(final Array a) {
        throw new NotImplementedException();
//		Array arr = super.apply(a);
//		if (arr.getRank() == 1) {
//			final double[] d = (double[]) arr.get1DJavaArray(double.class);
//			final double[] th = MathTools.topHat(this.window, d);
//			arr = Array.factory(th);
//		} else {
//			throw new IllegalArgumentException(
//			        "Can only work on arrays of dimension 1");
//		}
//		return arr;
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.window = cfg.getInt(this.getClass().getName() + ".window", 10);
    }
}
