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
 * Warps two chromatogramms according to their mean mass spectra of the scan
 * lines.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.second_column_scan_index",
    "var.total_intensity", "var.mass_values", "var.intensity_values",
    "var.scan_index", "var.mass_range_min", "var.mass_range_max",
    "var.modulation_time", "var.scan_rate"})
@ProvidesVariables(names = {"var.warp_path_i", "var.warp_path_j"})
public class ScanlineMaxMSWarp extends ADynamicTimeWarp {

    @Configurable(name = "var.maxms_1d_horizontal",
    value = "maxms_1d_horizontal")
    private String maxMSHorizontalVar = "maxms_1d_horizontal";
    @Configurable(name = "var.maxms_1d_horizontal_index",
    value = "maxms_1d_horizontal_index")
    private String maxMSHorizontalIndexVar = "maxms_1d_horizontal_index";
    @Configurable(name = "var.maxms_1d_vertical", value = "maxms_1d_vertical")
    private String maxMSVerticalVar = "maxms_1d_vertical";
    @Configurable(name = "var.maxms_1d_vertical_index",
    value = "maxms_1d_vertical_index")
    private String maxMSVerticalIndexVar = "maxms_1d_vertical_index";
    @Configurable(name = "var.used_mass_values", value = "used_mass_values")
    private String usedMassValuesVar = "used_mass_values";
    private String meanMSVar = "meanms_1d_horizontal";
    private String meanMSIndexVar = "meanms_1d_horizontal_index";
    @Configurable(value = "false")
    private boolean horizontal = false;
    @Configurable(value = "true")
    private boolean scale = true;
    @Configurable(value = "true")
    private boolean filter = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.maxMSHorizontalVar = cfg.getString("var.maxms_1d_horizontal",
                "maxms_1d_horizontal");
        this.maxMSHorizontalIndexVar = cfg.getString(
                "var.maxms_1d_horizontal_index", "maxms_1d_horizontal_index");
        this.maxMSVerticalVar = cfg.getString("var.maxms_1d_vertical",
                "maxms_1d_vertical");
        this.maxMSVerticalIndexVar = cfg.getString(
                "var.maxms_1d_vertical_index", "maxms_1d_vertical_index");
        this.horizontal = cfg.getBoolean(this.getClass().getName()
                + ".horizontal", false);
        this.scale = cfg.getBoolean(this.getClass().getName() + ".scale", true);
        this.filter = cfg.getBoolean(this.getClass().getName() + ".filter",
                true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple2D<List<Array>, List<Array>> createTuple(
            final Tuple2D<IFileFragment, IFileFragment> t) {

        if (this.horizontal) {
            this.meanMSVar = this.maxMSHorizontalVar;
            this.meanMSIndexVar = this.maxMSHorizontalIndexVar;
        } else {
            this.meanMSVar = this.maxMSVerticalVar;
            this.meanMSIndexVar = this.maxMSVerticalIndexVar;
        }

        log.info("Using {} with {}", this.meanMSVar, this.meanMSIndexVar);

        List<Array> ref = null;
        log.info("searching max ms in ref");
        final IVariableFragment meanHr = t.getFirst().getChild(this.meanMSVar);
        meanHr.setIndex(t.getFirst().getChild(this.meanMSIndexVar));
        ref = meanHr.getIndexedArray();

        List<Array> query = null;
        log.info("searching max ms in query");
        final IVariableFragment meanHq = t.getSecond().getChild(this.meanMSVar);
        meanHq.setIndex(t.getSecond().getChild(this.meanMSIndexVar));
        query = meanHq.getIndexedArray();

        if (this.scale) {
            ref = ArrayTools2.sqrt(ref);
            query = ArrayTools2.sqrt(query);
        }

        if (this.filter) {
            final List<Integer> usedMassesRef = ArrayTools2.getUsedMasses(t.
                    getFirst(), this.usedMassValuesVar);
            final List<Integer> usedMassesQuery = ArrayTools2.getUsedMasses(t.
                    getSecond(), this.usedMassValuesVar);
            ref = ArrayTools2.filterInclude(ref, usedMassesRef);
            query = ArrayTools2.filterInclude(query, usedMassesQuery);
        }

        return new Tuple2D<List<Array>, List<Array>>(ref, query);
    }
}
