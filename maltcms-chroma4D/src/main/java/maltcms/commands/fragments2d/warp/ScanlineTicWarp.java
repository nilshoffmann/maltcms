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
package maltcms.commands.fragments2d.warp;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import java.util.List;
import lombok.Data;

import maltcms.commands.distances.dtw.ADynamicTimeWarp;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 * Implementation of Pairwise Dynamic-Time-Warping for time-series data. This
 * class will use a scanline as a vector of double(intensities).
 *
 * @author Mathias Wilhelm
 * 
 */

@Data
@RequiresVariables(names = {"var.second_column_scan_index",
    "var.total_intensity"})
@RequiresOptionalVariables(names = {"var.v_total_intensity"})
@ProvidesVariables(names = {"var.warp_path_i", "var.warp_path_j"})
public class ScanlineTicWarp extends ADynamicTimeWarp {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ScanlineTicWarp.class);

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensity = "total_intensity";
    @Configurable(name = "var.second_column_scan_index",
            value = "second_column_scan_index")
    private String secondColumnScanIndexVar = "second_column_scan_index";

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.totalIntensity = cfg.getString(this.getClass().getName()
                + ".total_intensity", "total_intensity");
        this.secondColumnScanIndexVar = cfg.getString(
                "var.second_column_scan_index", "second_column_scan_index");
    }

    /** {@inheritDoc} */
    @Override
    public Tuple2D<List<Array>, List<Array>> createTuple(
            final Tuple2D<IFileFragment, IFileFragment> t) {
        IVariableFragment var1 = t.getFirst().getChild(this.totalIntensity);
        IVariableFragment index1 = t.getFirst().getChild(this.secondColumnScanIndexVar);
        var1.setIndex(index1);
        final List<Array> ref = t.getFirst().getChild(this.totalIntensity).
                getIndexedArray();
        IVariableFragment var2 = t.getSecond().getChild(this.totalIntensity);
        IVariableFragment index2 = t.getSecond().getChild(this.secondColumnScanIndexVar);
        var2.setIndex(index2);
        final List<Array> query = t.getSecond().getChild(this.totalIntensity).
                getIndexedArray();

        // FIXME
        // check if the last array has the same size as the first one
        // this may happen, if #total_intensities%scans_per_modulation != 0
        if (ref.get(ref.size() - 1).getSize() != ref.get(0).getSize()) {
            log.error("Removing last array in ref ({}!={})", ref.get(
                    ref.size() - 1).getSize(), ref.get(0).getSize());
            ref.remove(ref.size() - 1);
        }
        if (query.get(query.size() - 1).getSize() != query.get(0).getSize()) {
            log.error("Removing last array in query ({}!={})", query.get(
                    query.size() - 1).getSize(), query.get(0).getSize());
            query.remove(query.size() - 1);
        }

        final Tuple2D<List<Array>, List<Array>> tuple = new Tuple2D<>(
                ref, query);

        this.ref_num_scans = ref.size();
        this.query_num_scans = query.size();

        return tuple;
    }
}
