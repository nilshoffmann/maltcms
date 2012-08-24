/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
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
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
@ProvidesVariables(names = {"var.warp_path_i", "var.warp_path_j"})
public class ScanlineHorizontalTicWarp extends ADynamicTimeWarp {

    private boolean scale = true;
    private int k = 1;

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
