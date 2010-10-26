package maltcms.commands.distances;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.Logging;
import cross.annotations.Configurable;

public class ArrayBhattacharryya implements IArrayDoubleComp {

	@Configurable(name = "expansion_weight")
	private double wExp = 1.0d;
	@Configurable(name = "compression_weight")
	private double wComp = 1.0d;
	@Configurable(name = "diagonal_weight")
	private double wDiag = 2.25d;

	@Override
	public Double apply(int i1, int i2, double time1, double time2, Array t1,
			Array t2) {
		if ((t1.getRank() == 1) && (t2.getRank() == 1)) {
			IndexIterator iter1 = t1.getIndexIterator();
			double s1 = 0, s2 = 0;
			while (iter1.hasNext()) {
				s1 += iter1.getDoubleNext();
			}
			IndexIterator iter2 = t2.getIndexIterator();
			while (iter2.hasNext()) {
				s2 += iter2.getDoubleNext();
			}
			iter1 = t1.getIndexIterator();
			iter2 = t2.getIndexIterator();
			double sum = 0;
			while (iter1.hasNext() && iter2.hasNext()) {
				sum += Math.sqrt((iter1.getDoubleNext() / s1)
						* (iter2.getDoubleNext() / s2));
			}
			final double ret = Math.sqrt(1 - sum);
			if (ret > 0.0d && ret <= 1.0d) {
				return ret;
			}
			return 0.0d;
		}
		throw new IllegalArgumentException("Arrays shapes are incompatible! "
				+ t1.getShape()[0] + " != " + t2.getShape()[0]);
	}

	@Override
	public void configure(final Configuration cfg) {
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