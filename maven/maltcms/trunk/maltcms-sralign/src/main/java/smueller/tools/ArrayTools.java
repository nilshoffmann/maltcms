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

import ucar.ma2.Array;
import ucar.ma2.MAMath;

// Ein paar Tools f�r Arrays
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class ArrayTools {

	public static double calcmax(final Array a) {
		return MAMath.getMaximum(a);
	}

	public static double calcmin(final Array a) {
		return MAMath.getMinimum(a);

	}

	public static int countChar(final String s, final char c) {
		return s.replaceAll("[^" + c + "]", "").length();
	}

}
