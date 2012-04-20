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
package smueller.alignment;

import java.util.Vector;

// Enth�lt optimales Alignment
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class OptimalAlignmentVector implements Cloneable {

	private final Vector<AlignedPairVector> align = new Vector<AlignedPairVector>();

	public OptimalAlignmentVector() {

	}

	public void addAlChars(final boolean a) {
		final AlignedPairVector ss = new AlignedPairVector(a);
		this.align.add(ss);
	}

	public void addAlChars(final char a, final char b) {
		final AlignedPairVector ss = new AlignedPairVector(a, b);
		this.align.add(ss);
	}

	public void addAlChars(final char a, final char b, final int c, final int d) {
		final AlignedPairVector ss = new AlignedPairVector(a, b, c, d);
		this.align.add(ss);
	}

	public void addALChars(final AlignedPairVector ss) {
		this.align.add(ss);
	}

	public void changeAlChars(final int a, final int b) {
		final AlignedPairVector ss = new AlignedPairVector();
		getCharPair(countAlChars() - 1);
		ss.setC(a);
		ss.setD(b);
	}

	@Override
	public Object clone() {

		final OptimalAlignmentVector cloneAlignme = new OptimalAlignmentVector();
		for (int i = 0; i < this.countAlChars(); i++) {
			cloneAlignme.addALChars((AlignedPairVector) this.getCharPair(i)
			        .clone());
			// System.out.println(11);
		}
		return cloneAlignme;

	}

	public int countAlChars() {
		return this.align.size();
	}

	public AlignedPairVector getCharPair(final int i) {
		return this.align.get(i);
	}

}
