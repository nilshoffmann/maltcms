/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
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
 * $Id: ArrayDotMap.java 160 2010-08-31 19:55:58Z nilshoffmann $
 */
package maltcms.math.functions.similarities;

import lombok.Data;
import maltcms.math.functions.IArraySimilarity;
import maltcms.tools.ArrayTools;
import maltcms.tools.ArrayTools2;


import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.IndexIterator;

/**
 * Calculates the dotmap, but instead of the weighting by mz bin this class uses
 * the standard deviation.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Data
//@ServiceProvider(service = IArraySimilarity.class)
public class ArrayDotMap implements IArraySimilarity {

    private ArrayDouble.D1 std;
    private IArraySimilarity score = new ArrayCos();

    /**
     * {@inheritDoc}
     */
    @Override
    public double apply(final Array t1, final Array t2) {

        IndexIterator iter1 = t1.getIndexIterator();
        IndexIterator iter2 = t2.getIndexIterator();
        IndexIterator iter3 = this.std.getIndexIterator();
        double sum1 = 0.0d;
        double sum2 = 0.0d;
        double lstd = 0.0d;
        double c = 0.0d;
        while (iter1.hasNext() && iter2.hasNext() && iter3.hasNext()) {
            lstd = iter3.getDoubleNext();
            sum1 += Math.sqrt(iter1.getDoubleNext()) * lstd * c;
            sum2 += Math.sqrt(iter2.getDoubleNext()) * lstd * c;
            c++;
        }

        final Array t1s = ArrayTools.mult(ArrayTools2.sqrt(t1), 1.0d / sum1);
        final Array t2s = ArrayTools.mult(ArrayTools2.sqrt(t2), 1.0d / sum2);

        iter1 = t1s.getIndexIterator();
        iter2 = t2s.getIndexIterator();
        iter3 = this.std.getIndexIterator();
        double s1, s2, sum = 0.0d;
        c = 0;
        while (iter1.hasNext() && iter2.hasNext() && iter3.hasNext()) {
            lstd = iter3.getDoubleNext();
            s1 = iter1.getDoubleNext() * lstd * c;
            s2 = iter2.getDoubleNext() * lstd * c;
            sum += s1 * s2;
            c++;
        }

        return sum + this.score.apply(t1, t2);
    }

    /**
     * Setter.
     * 
     * @param stdArray
     *            variance for each mass bin
     */
    public void setStdArray(final ArrayDouble.D1 stdArray) {
        this.std = (ArrayDouble.D1) stdArray.copy();
    }
}
