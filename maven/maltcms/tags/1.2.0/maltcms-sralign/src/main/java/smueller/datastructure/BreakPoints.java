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
package smueller.datastructure;

// Berechnen der Breakpoints, die festlegen in welchem Intervall ein Buchstabe
// zugeordnet wird

import smueller.SymbolicRepresentationAlignment;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class BreakPoints {

	private final double[] common;

	// Berechne gemeinsame Breakpoints
	public BreakPoints(final Array c, final Array d) {
		final double[] a = calcbreakpoints(c, SymbolicRepresentationAlignment
		        .getSorti().getSortedjavarray1(),
		        SymbolicRepresentationAlignment.getAlphabetgr());
		final double[] b = calcbreakpoints(d, SymbolicRepresentationAlignment
		        .getSorti().getSortedjavarray2(),
		        SymbolicRepresentationAlignment.getAlphabetgr());
		this.common = new double[a.length];
		for (int i = 0; i < this.common.length; i++) {
			this.common[i] = (a[i] + b[i]) / 2;
		}
	}

	public double[] calcbreakpoints(final Array a1, final double[] sortiert,
	        final int alphabetgr) {
		final Array a = a1.copy();
		double total = 0;
		double save = 0;
		final IndexIterator ii1 = a.getIndexIterator();
		// Gesamtintensit�t berechnen
		while (ii1.hasNext()) {
			total = total + ii1.getDoubleNext();
		}
		// Festlegen, bis zu welchem Wert aufsummiert werden muss
		total = (total / (alphabetgr));
		double save2 = total;
		final double[] bps = new double[alphabetgr + 1];
		int pos = 0;
		// Aufsummieren, bis Schwellwert erreicht, Wert zum �berschreiten der
		// Schwelle merken und als Breakpoint sichern
		for (int j = 0; j < sortiert.length; j++) {
			if (save < save2) {
				save = save + sortiert[j];
			}

			else {
				save = save + sortiert[j];
				save2 = save2 + total;
				bps[pos] = sortiert[j - 1];
				pos++;

			}

		}
		// Wenn Letzter Breakpoint auf Grund zu kleinem Wertebereich nicht mehr
		// gesetzt, dann sorge daf�r dass zumindest 1 Wert
		// den gr��ten Buchstaben bekommt
		if ((bps[alphabetgr - 2] == 0.0) && (bps[alphabetgr - 3] != 0.0)) {
			bps[alphabetgr - 2] = sortiert[sortiert.length - 1] - 0.01;
		}
		// System.out.println("--------");
		// Max und Min den Breakpoints mitgeben
		bps[alphabetgr] = Math.round(sortiert[0] * 100) / 100.00;
		bps[alphabetgr - 1] = Math.round(sortiert[sortiert.length - 1] * 100) / 100.00;

		return bps;

	}

	public double[] getCommon() {
		return this.common;
	}
}
