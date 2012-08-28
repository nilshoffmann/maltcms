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
package maltcms.math.functions;

import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;
import maltcms.math.functions.similarities.ArrayCorr;
import lombok.Data;
import maltcms.commands.distances.IDtwSimilarityFunction;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service = IDtwSimilarityFunction.class)
public class DtwTimePenalizedPairwiseSimilarity implements IDtwSimilarityFunction {

    private double expansionWeight = 1.0;
    private double matchWeight = 1.0;
    private double compressionWeight = 1.0;
    private IScalarSimilarity retentionTimeSimilarity = new GaussianDifferenceSimilarity();
    private IArraySimilarity denseMassSpectraSimilarity = new ArrayCorr();

    @Override
    public double apply(int i1, int i2, double time1, double time2, Array t1,
            Array t2) {
        final double rtScore = retentionTimeSimilarity.apply(time1, time2);
        if (rtScore == Double.NEGATIVE_INFINITY || rtScore == Double.NaN) {
            return Double.NEGATIVE_INFINITY;
        }
        double score = denseMassSpectraSimilarity.apply(t1, t2);
        return rtScore * score;
    }

    @Override
    public boolean minimize() {
        return false;
    }

    @Override
    public void configure(Configuration cfg) {
    }
}
