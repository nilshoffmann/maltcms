/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: ScanlineFilterTicWarp.java 160 2010-08-31 19:55:58Z nilshoffmann $
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
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.Tuple2D;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of Pairwise Dynamic-Time-Warping for time-series data. This
 * class will use a scanline as a vector of double(intensities).
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
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

    @Override
    public String toString() {
        return getClass().getName();
    }

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

        t.getFirst().getChild(this.totalIntensity).setIndex(
                t.getFirst().getChild(this.secondColumnScanIndexVar));
        List<Array> ref = t.getFirst().getChild(this.totalIntensity).
                getIndexedArray();
        t.getSecond().getChild(this.totalIntensity).setIndex(
                t.getSecond().getChild(this.secondColumnScanIndexVar));
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
