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
package maltcms.commands.fragments2d.warp;

import java.util.List;

import maltcms.commands.distances.dtw.ADynamicTimeWarp;
import maltcms.tools.ArrayTools2;


import ucar.ma2.Array;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of pairwise Dynamic-Time-Warping for time-series data. This
 * class will use a the warped horizontal scanlines as a vector of double
 * (intensities).
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@ProvidesVariables(names = {"var.warp_path_i", "var.warp_path_j"})
public class ScanlineHorizontalTicWarp extends ADynamicTimeWarp {

    private boolean scale = true;
    private int k = 1;

    /**
     *
     * @param t
     * @return
     */
    @Override
    public Tuple2D<List<Array>, List<Array>> createTuple(
            Tuple2D<IFileFragment, IFileFragment> t) {

        final IFileFragment ref = t.getFirst(), query = t.getSecond();

        final String refname = StringTools.removeFileExt(ref.getName()), queryname = StringTools.
                removeFileExt(query.getName());

        ref.getChild(refname + "_" + queryname + "-tv").setIndex(
                ref.getChild(refname + "_" + queryname + "-idx"));
        List<Array> scanlineRef = ref.getChild(
                refname + "_" + queryname + "-tv").getIndexedArray();

        query.getChild(queryname + "_" + refname + "-tv").setIndex(
                query.getChild(queryname + "_" + refname + "-idx"));
        List<Array> scanlineQuery = query.getChild(
                queryname + "_" + refname + "-tv").getIndexedArray();

        if (this.scale) {
//            for (int i = 0; i < k; i++) {
            log.info("Scaling");
            scanlineRef = ArrayTools2.sqrt(scanlineRef);
            scanlineQuery = ArrayTools2.sqrt(scanlineQuery);
//            }
        }

        return new Tuple2D<List<Array>, List<Array>>(scanlineRef, scanlineQuery);
    }
}
