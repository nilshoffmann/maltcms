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

import maltcms.datastructures.array.IArrayD2Double;
import cross.IConfigurable;
import ucar.ma2.ArrayByte;

/**
 * Interface for classes performing a recurring operation, e.g. dynamic
 * programming.
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 *
 */
public interface IRecurrence extends IConfigurable {

    public abstract double eval(int row, int column,
            IArrayD2Double cumDistMatrix, double dij, ArrayByte.D2 predecessors);

    public abstract double eval(int row, int column,
            IArrayD2Double previousRow, IArrayD2Double currentRow, double dij);

    public abstract void set(double compression_weight,
            double expansion_weight, double diagonal_weight);

    public abstract void setMinimizing(boolean b);

    public abstract double getGlobalGapPenalty();
}
