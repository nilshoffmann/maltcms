/**
 * 
 */
package maltcms.datastructures.ridge;

import java.awt.geom.Point2D;

import org.openide.util.lookup.ServiceProvider;

import cross.datastructures.tuple.Tuple2D;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@ServiceProvider(service=IRidgeCost.class)
public class RidgeCost0 implements IRidgeCost {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * maltcms.datastructures.ridge.IRidgeCost#getCost(maltcms.datastructures
	 * .ridge.Ridge)
	 */
	@Override
	public double getCost(Ridge r) {
		double ridgePenalty = 0;
		Tuple2D<Point2D, Double> previous = r.getRidgePoints().get(0);
		int t = 0, t1 = 0;
		for (int i = 1; i < r.getRidgePoints().size(); i++) {
			Tuple2D<Point2D, Double> point = r.getRidgePoints().get(i);
			double delta = point.getFirst().getX() - previous.getFirst().getX();
			if (delta > 0) {// extending right
				t1 = 1;
			} else if (delta < 0) {// extending left
				t1 = -1;
			} else {// extending above
				t1 = 0;
			}

			// same direction
			int dt = t1 - t;
			ridgePenalty += dt;
			t = t1;
		}
//		return ridgePenalty;
		return Math.abs(2.0 * ridgePenalty / r.getSize());
	}

}
