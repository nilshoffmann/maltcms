/**
 * 
 */
package maltcms.datastructures.ridge;

import java.awt.geom.Point2D;

import cross.datastructures.tuple.Tuple2D;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
public class RidgeCost2 implements IRidgeCost {

	/**
	 * Implements a path integral over the scale space ridge, starting from the
	 * initial position x0. Then, for each subsequent scale, the scale space
	 * response is squared and divided by (1+(x-x0)^{2}). This has the effect of
	 * dampening ridges, which diverge far from the x0 over the scales. However,
	 * if a ridge first diverges and then returns back to x0, this effect is
	 * cancelled.
	 * 
	 * @return
	 */
	@Override
	public double getCost(Ridge r) {
		double ridgePenalty = 0;
		Tuple2D<Point2D, Double> previous = r.getRidgePoints().get(0);
		double x0 = previous.getFirst().getX();
		for (int i = 0; i < r.getRidgePoints().size(); i++) {
			Tuple2D<Point2D, Double> point = r.getRidgePoints().get(i);
			ridgePenalty += getScoreContribution(x0, point.getFirst().getX(),
			        point.getSecond().doubleValue());
		}

		// return Math.abs(2.0 * ridgePenalty / getSize());
		return ridgePenalty;
	}

	protected double getScoreContribution(double x0, double x, double fx) {
		return Math.pow(fx, 2.0d) / (1.0d + Math.pow(x - x0, 2.0d));
	}

}
