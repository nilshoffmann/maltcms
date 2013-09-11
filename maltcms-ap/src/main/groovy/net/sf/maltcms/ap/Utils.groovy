/*
 * Maltcms, modular application toolkit for chromatography mass-spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.ap

/**
 *
 * @author Nils Hoffmann
 */
class Utils {
    def isDouble = { val ->
        try {
            Double.parseDouble(val)
            true
        } catch (NumberFormatException e) {
            false
        }
    }

    def isInteger = { val ->
        try {
            Integer.parseInt(val)
            true
        } catch (NumberFormatException e) {
            false
        }
    }

    def convString = { val,defaultVal ->
        if(val == null || val.isEmpty()) {
            return defaultVal
        }
        return val
    }

    def convDouble = { val,defaultVal ->
        if(val == null) {
            return defaultVal
        }else if(val.equals("NaN")) {
            return Double.NaN
        }else if(val.equals("-Infinity")) {
            return Double.NEGATIVE_INFINITY
        }else if(val.equals("Infinity")) {
            return Double.POSITIVE_INFINITY
        }else if(isDouble(val)) {
            return Double.parseDouble(val)
        }
        return 0
    }

    def convInteger = { val,defaultVal ->
        if(val == null) {
            return defaultVal
        }else if(isInteger(val)) {
            return Integer.parseInt(val)
        }
        return 0
    }

    def convBoolean = { val,defaultVal ->
        if(val == null) {
            return defaultVal
        }else {
            return Boolean.parseBoolean(val)
        }
    }
}

