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
 *  $Id$
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
public class RidgeCost1 implements IRidgeCost {

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
			ridgePenalty += Math.pow(dt, 2.0);
			t = t1;
		}
		return ridgePenalty;
		// return Math.abs(2.0 * ridgePenalty / r.getSize());
	}

}
