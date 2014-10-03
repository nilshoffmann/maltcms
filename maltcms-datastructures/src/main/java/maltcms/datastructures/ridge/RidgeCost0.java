/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package maltcms.datastructures.ridge;

import java.awt.geom.Point2D;

import org.openide.util.lookup.ServiceProvider;

import cross.datastructures.tuple.Tuple2D;

/**
 * <p>RidgeCost0 class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
@ServiceProvider(service = IRidgeCost.class)
public class RidgeCost0 implements IRidgeCost {

    /*
     * (non-Javadoc)
     * 
     * @see
     * maltcms.datastructures.ridge.IRidgeCost#getCost(maltcms.datastructures
     * .ridge.Ridge)
     */
    /** {@inheritDoc} */
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
