/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments2d.peakfinding.bbh;

/**
 * <p>Reliability class.</p>
 *
 * @author Nils Hoffmann
 * 
 * @since 1.3.2
 */
public class Reliability {

    double min = 0, max = 0, rel = 0;

    /**
     * <p>Constructor for Reliability.</p>
     *
     * @param rel a double.
     */
    public Reliability(double rel) {
        this.min = rel;
        this.max = rel;
        this.rel = rel;
    }

    /**
     * <p>Getter for the field <code>min</code>.</p>
     *
     * @return a double.
     */
    public double getMin() {
        return min;
    }

    /**
     * <p>Getter for the field <code>max</code>.</p>
     *
     * @return a double.
     */
    public double getMax() {
        return max;
    }

    /**
     * <p>getReliability.</p>
     *
     * @return a double.
     */
    public double getReliability() {
        return rel;
    }

    /**
     * <p>addRel.</p>
     *
     * @param newRel a double.
     */
    public void addRel(double newRel) {
        if (newRel < this.min) {
            this.min = newRel;
        }
        if (newRel > this.max) {
            this.max = newRel;
        }
        this.rel *= newRel;
    }
}
