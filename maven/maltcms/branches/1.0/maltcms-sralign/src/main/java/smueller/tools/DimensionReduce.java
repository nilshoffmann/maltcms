/*
 * Copyright (C) 2008, 2009 Soeren Mueller,Nils Hoffmann Nils.Hoffmann A T
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
 * $Id: DimensionReduce.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package smueller.tools;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.tools.MathTools;

// Reduktion der Daten durch Ueberfuehrung in die PAA Repraesentation
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class DimensionReduce {

	public static Array paa(final Array a1, final int windowsize) {
		final Array a = a1.copy();
		final int[] dimension = { ((int) a.getSize() / windowsize) + 1 };
		final Array reduced = Array.factory(a.getElementType(), dimension);
		final IndexIterator ii4 = a.getIndexIterator();
		final IndexIterator red = reduced.getIndexIterator();
		double save = 0;
		int counter;
		final double[] window = new double[windowsize];

		// Setzt Fenster der Gr��e w �ber Daten, und findet Median darin.
		// Dieser
		// ist neuer Wert im reduzierten Array
		while (ii4.hasNext()) {
			for (counter = 0; counter < windowsize; counter++) {
				if (ii4.hasNext()) {
					window[counter] = ii4.getDoubleNext();
				} else {
					counter = windowsize;
				}
			}
			save = MathTools.median(window);
			counter = 0;
			red.setDoubleNext(save);
			save = 0;
		}

		return reduced;
	}
}
