/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.quadTree.distances;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Implementation of Perpendicular Distance of a Point onto a line.
 * This is useful for range searches along a given line/dimension.
 * 
 * @author Nils Hoffmann
 */
public class PerpendicularDistance {
	
	public double distance(Point2D p, Line2D l) {
		double m = getM(l);
		double b = getB(l);
		double z = Math.abs((m*p.getX())-p.getY()+b);
		return z/Math.sqrt(Math.pow(m, 2)+1);
	}
	
	private double getM(Line2D l) {
		return (l.getY2()-l.getY1())/(l.getX2()-l.getX1());
	}
	
	private double getB(Line2D l) {
		return l.getY1()-(getM(l)*l.getX1());
	}
}
