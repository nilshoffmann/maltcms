/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: ArrayRankCorr.java 129 2010-06-25 11:57:02Z nilshoffmann $
 */
package maltcms.math.functions.similarities;

import java.util.WeakHashMap;

import org.apache.commons.math.stat.correlation.SpearmansCorrelation;

import ucar.ma2.Array;
import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import org.openide.util.lookup.ServiceProvider;

/**
 * Calculates Spearman's rank correlation as similarity between arrays.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
@Data
@ServiceProvider(service = IArraySimilarity.class)
public class ArrayRankCorr implements IArraySimilarity {

    private boolean returnCoeffDetermination = false;
    private final WeakHashMap<Array, double[]> arrayCache = new WeakHashMap<Array, double[]>();
    private final SpearmansCorrelation sc = new SpearmansCorrelation();

    @Override
    public double apply(final Array t1, final Array t2) {
        // SpearmansCorrelation sc = new SpearmansCorrelation();
        double[] t1a = null, t2a = null;
        if (arrayCache.containsKey(t1)) {
            t1a = arrayCache.get(t1);
        } else {
            t1a = (double[]) t1.get1DJavaArray(double.class);
            arrayCache.put(t1, t1a);
        }
        if (arrayCache.containsKey(t2)) {
            t2a = arrayCache.get(t2);
        } else {
            t2a = (double[]) t2.get1DJavaArray(double.class);
            arrayCache.put(t2, t2a);
        }
        double pcv = sc.correlation(t1a, t2a);
        if (this.returnCoeffDetermination) {
            return pcv * pcv;
        }
        return pcv;
    }

}