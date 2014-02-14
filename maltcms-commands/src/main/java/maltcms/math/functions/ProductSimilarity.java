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

import java.util.Arrays;
import lombok.Data;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
@ServiceProvider(service = IScalarArraySimilarity.class)
public class ProductSimilarity implements IScalarArraySimilarity {

    private IScalarSimilarity[] scalarSimilarities = new IScalarSimilarity[0];
    private IArraySimilarity[] arraySimilarities = new IArraySimilarity[0];

    @Override
    public double apply(double[] s1, double[] s2, Array a1, Array a2) {
        double val = 1.0d;
        for (int i = 0; i < scalarSimilarities.length; i++) {
            double v = scalarSimilarities[i].apply(s1[i], s2[i]);
            if (Double.isInfinite(v) || Double.isNaN(v)) {
                return Double.NEGATIVE_INFINITY;
            }
            val *= v;
        }
        for (int i = 0; i < arraySimilarities.length; i++) {
            val *= arraySimilarities[i].apply(a1, a2);
        }
        return val;
    }

    @Override
    public IScalarArraySimilarity copy() {
        ProductSimilarity psim = new ProductSimilarity();
        IScalarSimilarity[] isas = new IScalarSimilarity[getScalarSimilarities().length];
        IArraySimilarity[] ias = new IArraySimilarity[getArraySimilarities().length];
        for (int i = 0; i < isas.length; i++) {
            isas[i] = getScalarSimilarities()[i].copy();
        }
        for (int i = 0; i < ias.length; i++) {
            ias[i] = getArraySimilarities()[i].copy();
        }
        psim.setArraySimilarities(ias);
        psim.setScalarSimilarities(isas);
        return psim;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Arrays.deepHashCode(this.scalarSimilarities);
        hash = 53 * hash + Arrays.deepHashCode(this.arraySimilarities);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProductSimilarity other = (ProductSimilarity) obj;
        if (!Arrays.deepEquals(this.scalarSimilarities, other.scalarSimilarities)) {
            return false;
        }
        if (!Arrays.deepEquals(this.arraySimilarities, other.arraySimilarities)) {
            return false;
        }
        return true;
    }

}
