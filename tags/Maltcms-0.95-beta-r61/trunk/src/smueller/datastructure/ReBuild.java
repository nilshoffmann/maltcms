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
 * $Id$
 */

package smueller.datastructure;

import smueller.SymbolicRepresentationAlignment;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;

/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class ReBuild {

	public static Array addbaseclean(final Array a1, final double m) {
		final Array a = a1.copy();
		final IndexIterator ii = a.getIndexIterator();
		while (ii.hasNext()) {
			final double save = ((ii.getDoubleNext() + m));
			ii.setDoubleCurrent(save);
			// System.out.println(save);
		}

		return a;
	}

	public static Array strToDoubArray(final String str) {

		final int[] dimension = { ((str.length() - 1) * (SymbolicRepresentationAlignment
		        .getFenstergr())) };
		final Array rebuild = Array.factory(DataType.DOUBLE, dimension);
		final IndexIterator reb = rebuild.getIndexIterator();
		for (int i = 1; i < str.length(); i++) {
			if (str.charAt(i) == 'a') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb.setDoubleNext(SymbolicRepresentationAlignment
					        .getBpois().getCommon()[0] / 2);
				}
			} else if (str.charAt(i) == 'b') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[0] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[1]) / 2);
				}
			} else if (str.charAt(i) == 'c') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[1] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[2]) / 2);
				}
			} else if (str.charAt(i) == 'd') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[2] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[3]) / 2);
				}
			} else if (str.charAt(i) == 'e') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[3] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[4]) / 2);
				}
			} else if (str.charAt(i) == 'f') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[4] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[5]) / 2);
				}
			} else if (str.charAt(i) == 'g') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[5] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[6]) / 2);
				}
			} else if (str.charAt(i) == 'h') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[6] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[7]) / 2);
				}
			} else if (str.charAt(i) == 'i') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[7] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[8]) / 2);
				}
			} else if (str.charAt(i) == 'k') {
				for (int j = 0; j < SymbolicRepresentationAlignment
				        .getFenstergr(); j++) {
					reb
					        .setDoubleNext((SymbolicRepresentationAlignment
					                .getBpois().getCommon()[8] + SymbolicRepresentationAlignment
					                .getBpois().getCommon()[9]) / 2);
				}
			}

		}

		return rebuild;
	}

	public static Array unlog(final Array a1) {
		final Array a = a1.copy();
		final IndexIterator ii = a.getIndexIterator();
		while (ii.hasNext()) {
			final double save = Math.exp(ii.getDoubleNext());
			ii.setDoubleCurrent(save);
		}
		return a;
	}

	public static Array unscale(final Array a1, final double min,
	        final double max) {
		final Array a = a1.copy();
		final IndexIterator ii4 = a.getIndexIterator();
		final double minmax = 1 / (max - min);
		while (ii4.hasNext()) {
			final double save = (ii4.getDoubleNext() / minmax + min);
			ii4.setDoubleCurrent(save);
			// System.out.println(save);
			System.out.println("minmax" + Math.round(minmax) + "Min" + min
			        + "max" + max);
		}
		return a;
	}

}
