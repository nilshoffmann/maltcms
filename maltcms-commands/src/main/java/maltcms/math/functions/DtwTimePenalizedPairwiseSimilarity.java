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
package maltcms.math.functions;

import lombok.Data;
import maltcms.commands.distances.IDtwSimilarityFunction;
import maltcms.math.functions.similarities.ArrayCorr;
import maltcms.math.functions.similarities.GaussianDifferenceSimilarity;
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
