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
package maltcms.datastructures.peak.normalization;

import cross.datastructures.cache.CacheFactory;
import cross.datastructures.cache.ICacheDelegate;
import cross.datastructures.cache.ICacheElementProvider;
import cross.datastructures.fragments.IFileFragment;
import lombok.Data;
import maltcms.datastructures.peak.Peak1D;
import maltcms.tools.ArrayTools;

/**
 *
 * @author nilshoffmann
 */
@Data
public class TicAreaNormalizer implements IPeakNormalizer {

    private String ticVariableName = "total_intensity";
    private ICacheDelegate<IFileFragment, Double> fragmentToArea = CacheFactory.createAutoRetrievalCache("TicAreaNormalizerCache", new ICacheElementProvider<IFileFragment, Double>() {
        @Override
        public Double provide(IFileFragment key) {
            return ArrayTools.integrate(key.getChild(ticVariableName).getArray());
        }
    });

    @Override
    public double getNormalizationFactor(IFileFragment fragment, Peak1D peak) {
        return 1.0d / fragmentToArea.get(fragment).doubleValue();
    }

    @Override
    public String getNormalizationName() {
        return "normalization to sum of total intensity";
    }
}
