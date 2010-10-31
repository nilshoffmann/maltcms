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
 * $Id: StringTools.java 43 2009-10-16 17:22:55Z nilshoffmann $
 */

package cross.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class with methods to ease some String based operations.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class StringTools {
	/**
	 * Replaces all whitespace with hyphens
	 * 
	 * @param s
	 * @return
	 */
	public static String deBlank(final String s) {
		return StringTools.deBlank(s, "-");
	}

	/**
	 * Replaces all whitespace with replacement.
	 * 
	 * @param s
	 * @param replacement
	 * @return
	 */
	public static String deBlank(final String s, final String replacement) {
		return s.replaceAll("\\s", replacement);
	}

	/**
	 * Returns the suffix of a file.
	 * 
	 * @param s
	 * @return
	 */
	public static String getFileExtension(final String s) {
		final int lastIndexOfDot = s.lastIndexOf(".");
		return s.substring(lastIndexOfDot + 1, s.length());
	}

	/**
	 * Returns that part of a string before first occurrence of a dot.
	 * 
	 * @param deBlank
	 * @return
	 */
	public static String removeFileExt(final String s) {
		final int lastIndexOfDot = s.lastIndexOf(".");
		return s.substring(0,
		        (lastIndexOfDot < 0 ? s.length() : lastIndexOfDot));
	}

	/**
	 * Convert an untyped list of Strings to a typed one.
	 * 
	 * @param list
	 * @return
	 */
	public static ArrayList<String> toStringList(final List<?> list) {
		final ArrayList<String> al = new ArrayList<String>();
		for (final Object o : list) {
			if (o instanceof String) {
				al.add((String) o);
			}
		}
		return al;
	}
}
