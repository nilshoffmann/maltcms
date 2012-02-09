/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.maltcms.groovy

/**
 *
 * @author nilshoffmann
 */
class Utils {
	
    public static String toPropertyName(String s) {
        return s.substring(0,1).toLowerCase()+s.substring(1)
    }
}

