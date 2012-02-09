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
package maltcms.commands.distances;

import ucar.ma2.Array;
import cross.IConfigurable;

/**
 * Interface to define classes, comparing arrays, especially mass spectra by
 * some function and returning a double value as result.
 * 
 * @author nilshoffmann
 * 
 */
public interface IDtwSimilarityFunction extends IConfigurable {

    /**
     * if i1 && i2 > 0 => apply distance to indices only else apply to all
     * elements of array
     * 
     * @param i1
     * @param i2
     * @param t1
     * @param t2
     * @return
     */
    public abstract double apply(int i1, int i2, double time1, double time2, Array t1,
            Array t2);

    public double getCompressionWeight();

    public double getMatchWeight();

    public double getExpansionWeight();
    
    public void setCompressionWeight(double d);
    
    public void setMatchWeight(double d);
    
    public void setExpansionWeight(double d);

    public abstract boolean minimize();
}