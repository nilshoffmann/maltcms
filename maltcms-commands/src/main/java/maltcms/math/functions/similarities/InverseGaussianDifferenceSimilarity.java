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
package maltcms.math.functions.similarities;

import lombok.Data;
import maltcms.math.functions.IScalarSimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service = IScalarSimilarity.class)
public class InverseGaussianDifferenceSimilarity implements
        IScalarSimilarity {

//    private double tolerance = 5.0d;
    private double threshold = 0.0d;
    private double lambda = 1.0d;
    private boolean normalize = false;

    /**
     * Calculates the scalar
     *
     * @param time1
     * @param time2
     * @return
     */
    @Override
    public double apply(double time1, double time2) {
        // if no time is supplied, use 1 as default -> cosine/dot product
        // similarity
        final double weight = ((time1 == -1) || (time2 == -1)) ? 1.0d
                : Math.exp(
                (-(lambda * (time1 - time2) * (time1 - time2)) / (2.0d * time2 * time2 * time1)));
        // 1 for perfect time correspondence, 0 for really bad time
        // correspondence (towards infinity)
        if (weight - this.threshold < 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if(normalize) {
            return weight*Math.sqrt(lambda/2.0d*Math.PI*Math.pow(time1,3.0d));
        }
        return weight;
    }

	
	@Override
	public IScalarSimilarity copy() {
		InverseGaussianDifferenceSimilarity gds = new InverseGaussianDifferenceSimilarity();
		gds.setLambda(getLambda());
		gds.setNormalize(isNormalize());
		gds.setThreshold(getThreshold());
		return gds;
	}
}
