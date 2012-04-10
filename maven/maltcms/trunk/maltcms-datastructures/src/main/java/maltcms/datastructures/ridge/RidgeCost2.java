/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id: RidgeCost2.java 426 2012-02-09 19:38:11Z nilshoffmann $
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
