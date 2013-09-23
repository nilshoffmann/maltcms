/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package smueller.tools;

import ucar.ma2.Array;
import ucar.ma2.MAMath;

// Ein paar Tools f�r Arrays
/**
 * @author Soeren Mueller, smueller@cebitec.uni-bielefeld.de
 */
public class ArrayTools {

    /**
     *
     * @param a
     * @return
     */
    public static double calcmax(final Array a) {
        return MAMath.getMaximum(a);
    }

    /**
     *
     * @param a
     * @return
     */
    public static double calcmin(final Array a) {
        return MAMath.getMinimum(a);

    }

    /**
     *
     * @param s
     * @param c
     * @return
     */
    public static int countChar(final String s, final char c) {
        return s.replaceAll("[^" + c + "]", "").length();
    }
}