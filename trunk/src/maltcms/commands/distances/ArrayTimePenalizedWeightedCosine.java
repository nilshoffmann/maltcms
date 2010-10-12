/**
 * 
 */
package maltcms.commands.distances;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.Index;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class ArrayTimePenalizedWeightedCosine implements IArrayDoubleComp {

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 2.25d;

	@Configurable
	private double rtTolerance = 50.0d;

	@Configurable
	private double rtEpsilon = 0.01d;

	@Override
	public Double apply(final int i1, final int i2, final double time1,
	        final double time2, final Array t1, final Array t2) {
		// if no time is supplied, use 1 as default -> cosine/dot product
		// similarity
		final double weight = ((time1 == -1) || (time2 == -1)) ? 1.0d
		        : Math
		                .exp(-((time1 - time2) * (time1 - time2) / (2 * this.rtTolerance * this.rtTolerance)));
		// 1 for perfect time correspondence, 0 for really bad time
		// correspondence (towards infinity)
		if (weight - this.rtEpsilon < 0) {
			return Double.NEGATIVE_INFINITY;
		}
		Index i1idx = t1.getIndex();
		Index i2idx = t2.getIndex();
		double s1 = 0, s2 = 0;
		double v = 0.0d;
		for (int i = 0; i < t1.getShape()[0]; i++) {
			s1 += (i + 1.0) * t1.getDouble(i1idx.set(i));
			s2 += (i + 1.0) * t2.getDouble(i2idx.set(i));
		}
		double w1 = 0, w2 = 0;
		for (int i = 0; i < t1.getShape()[0]; i++) {
			w1 = (i + 1.0) * t1.getDouble(i1idx.set(i)) / s1;
			w2 = (i + 1.0) * t2.getDouble(i2idx.set(i)) / s2;
			v += (w1 * w2);
		}
		return v * weight;
	}

	@Override
	public void configure(final Configuration cfg) {
		this.rtTolerance = cfg.getDouble(this.getClass().getName()
		        + ".rtTolerance", 50.0d);
		this.rtEpsilon = cfg.getDouble(
		        this.getClass().getName() + ".rtEpsilon", 0.01d);
		this.wComp = cfg.getDouble(this.getClass().getName()
		        + ".compression_weight", 1.0d);
		this.wExp = cfg.getDouble(this.getClass().getName()
		        + ".expansion_weight", 1.0d);
		this.wDiag = cfg.getDouble(this.getClass().getName()
		        + ".diagonal_weight", 2.25d);
		StringBuilder sb = new StringBuilder();
		sb.append("wComp: " + this.wComp + ", ");
		sb.append("wExp: " + this.wExp + ", ");
		sb.append("wDiag: " + this.wDiag);
		Logging.getLogger(this).info("Parameters of class {}: {}",
		        this.getClass().getName(), sb.toString());
	}

	public double getCompressionWeight() {
		return this.wComp;
	}

	public double getDiagonalWeight() {
		return this.wDiag;
	}

	public double getExpansionWeight() {
		return this.wExp;
	}

	@Override
	public boolean minimize() {
		return false;
	}
}
