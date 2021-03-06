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
package maltcms.commands.distances;

import cross.IConfigurable;
import ucar.ma2.Array;

/**
 * Interface to define classes, comparing arrays, especially mass spectra by
 * some function and returning a double value as result.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IDtwSimilarityFunction extends IConfigurable {

    /**
     * if i1 && i2 > 0 => apply distance to indices only else apply to all
     * elements of array
     *
     * @param i1 a int.
     * @param i2 a int.
     * @param t1 a {@link ucar.ma2.Array} object.
     * @param t2 a {@link ucar.ma2.Array} object.
     * @param time1 a double.
     * @param time2 a double.
     * @return a double.
     */
    public abstract double apply(int i1, int i2, double time1, double time2, Array t1,
            Array t2);

    /**
     * <p>getCompressionWeight.</p>
     *
     * @return a double.
     */
    public double getCompressionWeight();

    /**
     * <p>getMatchWeight.</p>
     *
     * @return a double.
     */
    public double getMatchWeight();

    /**
     * <p>getExpansionWeight.</p>
     *
     * @return a double.
     */
    public double getExpansionWeight();

    /**
     * <p>setCompressionWeight.</p>
     *
     * @param d a double.
     */
    public void setCompressionWeight(double d);

    /**
     * <p>setMatchWeight.</p>
     *
     * @param d a double.
     */
    public void setMatchWeight(double d);

    /**
     * <p>setExpansionWeight.</p>
     *
     * @param d a double.
     */
    public void setExpansionWeight(double d);

    /**
     * <p>minimize.</p>
     *
     * @return a boolean.
     */
    public abstract boolean minimize();
}
