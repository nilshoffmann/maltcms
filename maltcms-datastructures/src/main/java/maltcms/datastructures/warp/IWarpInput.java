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
package maltcms.datastructures.warp;

import java.util.List;

import ucar.ma2.Array;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.Tuple2DI;

/**
 * Parameter Object encapsulating required data for Warp after alignment.
 *
 * @author Nils Hoffmann
 * 
 */
public interface IWarpInput {

    /**
     * <p>getAlgorithm.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getAlgorithm();

    /**
     * <p>getArrays.</p>
     *
     * @return a {@link cross.datastructures.tuple.Tuple2D} object.
     */
    public abstract Tuple2D<List<Array>, List<Array>> getArrays();

    /**
     * <p>getFileFragment.</p>
     *
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public abstract IFileFragment getFileFragment();

    /**
     * <p>getPath.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<Tuple2DI> getPath();

    /**
     * <p>getQueryFileFragment.</p>
     *
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public abstract IFileFragment getQueryFileFragment();

    /**
     * <p>getReferenceFileFragment.</p>
     *
     * @return a {@link cross.datastructures.fragments.IFileFragment} object.
     */
    public abstract IFileFragment getReferenceFileFragment();
}
