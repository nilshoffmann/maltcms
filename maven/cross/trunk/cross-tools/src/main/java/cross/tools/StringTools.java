/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class with methods to ease some String based operations.
 *
 * @author Nils Hoffmann
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
     * Returns the suffix of a file or s if no dot '.' is contained 
	 * in s.
     *
     * @param s
     * @return
     */
    public static String getFileExtension(final String s) {
        final int lastIndexOfDot = s.lastIndexOf(".");
		if(lastIndexOfDot==-1) {
			return s;
		}
        return s.substring(lastIndexOfDot + 1, s.length());
    }

    /**
     * Returns that part of a string before first occurrence of a dot, 
	 * if a dot is contained in s, otherwise, s is returned.
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
