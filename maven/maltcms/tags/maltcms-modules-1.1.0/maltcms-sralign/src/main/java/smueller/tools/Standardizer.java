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
package smueller.tools;

import smueller.SymbolicRepresentationAlignment;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.tools.MathTools;

// Statische Methode, die zur Standardisiereung be�tigt werden
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class Standardizer {

	private double median;
	private double min;
	private double max;

	// Abziehen des Median, teilen durch Standardabweichung f�r alle
	// Arraywerte,
	// addiert minimum um im pos. wertebereich zu bleiben
	public Array cleanbase(final Array a1, final double median1,
	        final double deviation) {
		final Array a = a1.copy();
		final IndexIterator ii4 = a.getIndexIterator();
		final IndexIterator ii5 = a.getIndexIterator();
		while (ii4.hasNext()) {

			final double save = ((ii4.getDoubleNext() - median1));
			ii4.setDoubleCurrent(save);
		}
		final double min2 = smueller.tools.ArrayTools.calcmin(a);
		if (min2 < 0) {
			while (ii5.hasNext()) {
				final double save = ii5.getDoubleNext() + Math.abs(min2);
				ii5.setDoubleCurrent(save);
			}
		}

		return a;
	}

	public Array cleanbaseline(final Array a1) {
		final Array a = a1.copy();
		// Globale Medianberechnung, verbraucht viel zus�tzl. Speicher, weil
		// in
		// Java Array kopiert werden muss.
		SymbolicRepresentationAlignment.getSorti().setSortedjavarray1(
		        (double[]) a.copyTo1DJavaArray());
		this.median = MathTools.median(SymbolicRepresentationAlignment
		        .getSorti().getSortedjavarray1());
		SymbolicRepresentationAlignment.getSorti().setSortedjavarray1(null);
		final double standarddev = standardDeviation(a, this.median);
		Array b = a;
		b = cleanbase(a, this.median, standarddev);
		return b;
	}

	public double getMax() {
		return this.max;
	}

	public double getMedian() {
		return this.median;
	}

	public double getMin() {
		return this.min;
	}

	// zieht logarithmus
	public Array logData(final Array a1) {
		final Array a = a1.copy();
		final IndexIterator ii4 = a.getIndexIterator();
		while (ii4.hasNext()) {
			final double save = (Math.log(ii4.getDoubleNext()));
			ii4.setDoubleCurrent(save);
			// System.out.println("Wert " + " :" + save);
		}
		return a;
	}

	// Berechnung des globalen Mittelwertes
	public double mean(final Array a) {
		final IndexIterator ii1 = a.getIndexIterator();
		double i = 0;
		while (ii1.hasNext()) {
			i = i + ii1.getDoubleNext();
		}
		i = i / a.getSize();
		return i;
	}

	// setzt alle negativen Arraywerte auf 0
	public Array nullData(final Array a1) {
		final Array a = a1.copy();
		final IndexIterator ii4 = a.getIndexIterator();
		while (ii4.hasNext()) {
			final double save = ii4.getDoubleNext();
			if (save > 0) {
				ii4.setDoubleCurrent(save);
			} else {
				ii4.setDoubleCurrent(0);
				// System.out.println("Wert " + " :" + save);
			}
		}
		return a;
	}

	// Standardisierung, skalieren auf Wertebereich 0-1
	public Array scale(final Array a1) {
		final Array a = a1.copy();
		final IndexIterator ii4 = a.getIndexIterator();
		this.max = smueller.tools.ArrayTools.calcmax(a);
		this.min = smueller.tools.ArrayTools.calcmin(a);

		int counter = 0;
		final double minmax = 1 / (this.max - this.min);
		System.out.println("Larifari" + Math.round(minmax) + "Min" + this.min
		        + "max" + this.max);
		while (ii4.hasNext()) {
			final double save = ((ii4.getDoubleNext() - this.min) * minmax);
			ii4.setDoubleCurrent(save);
			counter++;
			// System.out.println("Wert " + counter + " :" + save);

		}
		return a;
	}

	// Berechnung der Standardabweichung
	public double standardDeviation(final Array a, final double meanvalue) {
		final IndexIterator ii2 = a.getIndexIterator();
		double i = 0;
		double counter = 0;
		while (ii2.hasNext()) {
			final double calc = ii2.getDoubleNext();
			i = i + ((calc - meanvalue) * (calc - meanvalue));
			counter++;
		}

		counter = 1 / (counter - 1);
		i = (i * counter);
		i = Math.sqrt(i);
		return i;
	}

}
