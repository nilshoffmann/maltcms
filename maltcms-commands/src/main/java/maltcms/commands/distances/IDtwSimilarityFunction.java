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
package maltcms.commands.distances;

import cross.IConfigurable;
import ucar.ma2.Array;

/**
 * Interface to define classes, comparing arrays, especially mass spectra by
 * some function and returning a double value as result.
 *
 * @author nilshoffmann
 *
 */
public interface IDtwSimilarityFunction extends IConfigurable {

    /**
     * if i1 && i2 > 0 => apply distance to indices only else apply to all
     * elements of array
     *
     * @param i1
     * @param i2
     * @param t1
     * @param t2
     * @return
     */
    public abstract double apply(int i1, int i2, double time1, double time2, Array t1,
        Array t2);

    public double getCompressionWeight();

    public double getMatchWeight();

    public double getExpansionWeight();

    public void setCompressionWeight(double d);

    public void setMatchWeight(double d);

    public void setExpansionWeight(double d);

    public abstract boolean minimize();
}
