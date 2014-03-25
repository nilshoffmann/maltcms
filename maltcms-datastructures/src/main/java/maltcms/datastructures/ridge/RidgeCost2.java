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

import cross.datastructures.tuple.Tuple2D;
import java.awt.geom.Point2D;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils Hoffmann
 *
 *
 */
@ServiceProvider(service = IRidgeCost.class)
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
                    point.getSecond());
        }

        // return Math.abs(2.0 * ridgePenalty / getSize());
        return ridgePenalty;
    }

    protected double getScoreContribution(double x0, double x, double fx) {
        return Math.pow(fx, 2.0d) / (1.0d + Math.pow(x - x0, 2.0d));
    }
}
