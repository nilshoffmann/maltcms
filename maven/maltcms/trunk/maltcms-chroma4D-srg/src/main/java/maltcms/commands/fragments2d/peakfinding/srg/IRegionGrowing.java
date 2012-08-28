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
package maltcms.commands.fragments2d.peakfinding.srg;

import java.awt.Point;
import java.util.List;

import ucar.ma2.ArrayDouble;

import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.peak.PeakArea2D;
import cross.IConfigurable;
import cross.datastructures.fragments.IFileFragment;

public interface IRegionGrowing extends IConfigurable {

    List<PeakArea2D> getAreasFor(List<Point> seeds, IFileFragment ff, IScanLine slc);

    double getMinDist();

    List<ArrayDouble.D1> getIntensities();
}
