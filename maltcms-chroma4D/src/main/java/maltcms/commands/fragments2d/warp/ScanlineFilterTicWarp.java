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

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresOptionalVariables;
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of Pairwise Dynamic-Time-Warping for time-series data. This
 * class will use a scanline as a vector of double(intensities).
 *
 * @author Mathias Wilhelm
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.second_column_scan_index",
    "var.total_intensity"})
@RequiresOptionalVariables(names = {"var.v_total_intensity"})
@ProvidesVariables(names = {"var.warp_path_i", "var.warp_path_j"})
public class ScanlineFilterTicWarp extends ADynamicTimeWarp {
    // FIXME extends ScanlineTicWarp und dann filter drauf anwenden. Dann kann
    // filter auch gewählt werden

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensity = "total_intensity";
    @Configurable(name = "var.second_column_scan_index",
    value = "second_column_scan_index")
    private String secondColumnScanIndexVar = "second_column_scan_index";
    @Configurable(value = "false")
    private final boolean transpose = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.totalIntensity = cfg.getString(this.getClass().getName()
                + ".total_intensity", "total_intensity");
        this.secondColumnScanIndexVar = cfg.getString(
                "var.second_column_scan_index", "second_column_scan_index");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple2D<List<Array>, List<Array>> createTuple(
            final Tuple2D<IFileFragment, IFileFragment> t) {

        this.setExtension("");

        IVariableFragment ticVar1 = t.getFirst().getChild(this.totalIntensity);
		IVariableFragment scsiv1 = t.getFirst().getChild(this.secondColumnScanIndexVar);
		ticVar1.setIndex(scsiv1);
        List<Array> ref = t.getFirst().getChild(this.totalIntensity).
                getIndexedArray();
        IVariableFragment ticVar2 = t.getSecond().getChild(this.totalIntensity);
		IVariableFragment scsiv2 = t.getSecond().getChild(this.secondColumnScanIndexVar);
		ticVar2.setIndex(scsiv2);
        List<Array> query = t.getSecond().getChild(this.totalIntensity).
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

        if (this.transpose) {
            while (ref.size() != query.size()) {
                if (ref.size() > query.size()) {
                    ref.remove(ref.size() - 1);
                } else {
                    query.remove(query.size() - 1);
                }
            }

            ref = ArrayTools2.transpose(ref);
            query = ArrayTools2.transpose(query);
        }

        final Tuple2D<List<Array>, List<Array>> tuple = new Tuple2D<List<Array>, List<Array>>(
                ArrayTools2.sqrt(ref), ArrayTools2.sqrt(query));

        this.ref_num_scans = ref.size();
        this.query_num_scans = query.size();

        return tuple;
    }
}
