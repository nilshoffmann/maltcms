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
package maltcms.commands;

import java.util.List;
import maltcms.datastructures.array.IMutableFeatureVector;
import maltcms.datastructures.feature.DefaultFeatureVector;
import ucar.ma2.Array;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class ArrayStatsMap implements IMutableFeatureVector {

    /**
     *
     */
    private static final long serialVersionUID = -8374451195942782968L;
    private DefaultFeatureVector featureVector = new DefaultFeatureVector();

    @Override
    public String toString() {
        return featureVector.toString();
    }

    @Override
    public List<String> getFeatureNames() {
        return featureVector.getFeatureNames();
    }

    @Override
    public Array getFeature(String name) {
        return featureVector.getFeature(name);
    }

    @Override
    public void addFeature(String name, Array a) {
        featureVector.addFeature(name, a);
    }
}
