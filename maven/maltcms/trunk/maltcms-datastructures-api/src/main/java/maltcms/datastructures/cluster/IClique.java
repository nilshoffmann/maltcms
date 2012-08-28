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
package maltcms.datastructures.cluster;

import java.util.List;
import maltcms.datastructures.array.IFeatureVector;
import maltcms.datastructures.array.IMutableFeatureVector;

/**
 *
 * @author nilshoffmann
 */
public interface IClique<T extends IFeatureVector> {

    boolean add(T p) throws IllegalArgumentException;

    void clear();

    T getCliqueCentroid();

    List<T> getFeatureVectorList();

    long getID();

    void setCentroid(T ifv);

    int size();

    IMutableFeatureVector getArrayStatsMap();
}
