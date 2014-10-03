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
import maltcms.datastructures.array.IArrayD2Double;
import ucar.ma2.ArrayByte;

/**
 * Interface for classes performing a recurring operation, e.g. dynamic
 * programming.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IRecurrence extends IConfigurable {

    /**
     * <p>eval.</p>
     *
     * @param row a int.
     * @param column a int.
     * @param cumDistMatrix a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param dij a double.
     * @param predecessors a {@link ucar.ma2.ArrayByte.D2} object.
     * @return a double.
     */
    public abstract double eval(int row, int column,
            IArrayD2Double cumDistMatrix, double dij, ArrayByte.D2 predecessors);

    /**
     * <p>eval.</p>
     *
     * @param row a int.
     * @param column a int.
     * @param previousRow a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param currentRow a {@link maltcms.datastructures.array.IArrayD2Double} object.
     * @param dij a double.
     * @return a double.
     */
    public abstract double eval(int row, int column,
            IArrayD2Double previousRow, IArrayD2Double currentRow, double dij);

    /**
     * <p>set.</p>
     *
     * @param compression_weight a double.
     * @param expansion_weight a double.
     * @param diagonal_weight a double.
     */
    public abstract void set(double compression_weight,
            double expansion_weight, double diagonal_weight);

    /**
     * <p>setMinimizing.</p>
     *
     * @param b a boolean.
     */
    public abstract void setMinimizing(boolean b);

    /**
     * <p>getGlobalGapPenalty.</p>
     *
     * @return a double.
     */
    public abstract double getGlobalGapPenalty();
}
