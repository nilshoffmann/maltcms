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
 * $Id: IArrayComp.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */
package maltcms.math.functions;

import ucar.ma2.Array;

/**
 * Interface to define classes, comparing arrays, especially mass spectra by
 * some function and returning a generic type as result.
 * 
 * The implemented similarity function should have the following properties:
 * The maximal function value must be greater than the minimal 
 * function value, assuming that a similarity between
 * two scalars is maximal iff both entities are identical/equal
 * and minimal iff they are completely unrelated and that increasing similarity
 * is reflected by a greater value of the function.
 * Note that -Inf is reserved for special cases, where the 
 * similarity is not determinable or was not calculated due to an unmet threshold
 * criterion.
 * 
 * @author nilshoffmann
 * 
 */
public interface IArraySimilarity {

    /**
     * 
     * @param t1
     * @param t2
     * @return
     */
    public abstract double apply(Array t1,
            Array t2);
    
}
