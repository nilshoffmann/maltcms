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
import cross.annotations.RequiresVariables;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.exception.ResourceNotAvailableException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

import maltcms.commands.distances.dtw.ADynamicTimeWarp;
import maltcms.datastructures.caches.IScanLine;
import maltcms.datastructures.caches.ScanLineCacheFactory;
import maltcms.tools.ArrayTools;
import maltcms.tools.ArrayTools2;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 * Warps two chromatogramms according to their mean mass spectra of the scan
 * lines.
 *
 * @author Mathias Wilhelm
 * 
 */

@Data
@RequiresVariables(names = {"var.second_column_scan_index",
    "var.total_intensity", "var.mass_values", "var.intensity_values",
    "var.scan_index", "var.mass_range_min", "var.mass_range_max",
    "var.modulation_time", "var.scan_rate"})
@ProvidesVariables(names = {"var.warp_path_i", "var.warp_path_j"})
public class ScanlineMeanMSWarp extends ADynamicTimeWarp {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ScanlineMeanMSWarp.class);

    @Configurable(name = "var.meanms_1d_horizontal",
            value = "meanms_1d_horizontal")
    private String meanMSHorizontalVar = "meanms_1d_horizontal";
    @Configurable(name = "var.meanms_1d_horizontal_index",
            value = "meanms_1d_horizontal_index")
    private String meanMSHorizontalIndexVar = "meanms_1d_horizontal_index";
    @Configurable(name = "var.meanms_1d_vertical", value = "meanms_1d_vertical")
    private String meanMSVerticalVar = "meanms_1d_vertical";
    @Configurable(name = "var.meanms_1d_vertical_index",
            value = "meanms_1d_vertical_index")
    private String meanMSVerticalIndexVar = "meanms_1d_vertical_index";
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
    @Configurable(value = "true")
    private boolean horizontal = true;
    @Configurable(value = "true")
    private boolean useMean = true;
    @Configurable(value = "true")
    private boolean scale = true;
    @Configurable(value = "filter")
    private boolean filter = true;

    /** {@inheritDoc} */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.meanMSHorizontalVar = cfg.getString("var.meanms_1d_horizontal",
                "meanms_1d_horizontal");
        this.meanMSHorizontalIndexVar = cfg.getString(
                "var.meanms_1d_horizontal_index", "meanms_1d_horizontal_index");
        this.meanMSVerticalVar = cfg.getString("var.meanms_1d_vertical",
                "meanms_1d_vertical");
        this.meanMSVerticalIndexVar = cfg.getString(
                "var.meanms_1d_vertical_index", "meanms_1d_vertical_index");
        this.maxMSHorizontalVar = cfg.getString("var.maxms_1d_horizontal",
                "maxms_1d_horizontal");
        this.maxMSHorizontalIndexVar = cfg.getString(
                "var.maxms_1d_horizontal_index", "maxms_1d_horizontal_index");
        this.maxMSVerticalVar = cfg.getString("var.maxms_1d_vertical",
                "maxms_1d_vertical");
        this.maxMSVerticalIndexVar = cfg.getString(
                "var.maxms_1d_vertical_index", "maxms_1d_vertical_index");
//        this.horizontal = cfg.getBoolean(this.getClass().getName()
//                + ".horizontal", true);
//        this.useMean = cfg.getBoolean(this.getClass().getName() + ".useMean",
//                true);
//        this.scale = cfg.getBoolean(this.getClass().getName() + ".scale", true);
//        this.filter = cfg.getBoolean(this.getClass().getName() + ".filter",
//                true);
    }

    /** {@inheritDoc} */
    @Override
    public Tuple2D<List<Array>, List<Array>> createTuple(
            final Tuple2D<IFileFragment, IFileFragment> t) {

        if (this.horizontal) {
            if (this.useMean) {
                this.meanMSVar = this.meanMSHorizontalVar;
                this.meanMSIndexVar = this.meanMSHorizontalIndexVar;
            } else {
                this.meanMSVar = this.maxMSHorizontalVar;
                this.meanMSIndexVar = this.maxMSHorizontalIndexVar;
            }
        } else {
            if (this.useMean) {
                this.meanMSVar = this.meanMSVerticalVar;
                this.meanMSIndexVar = this.meanMSVerticalIndexVar;
            } else {
                this.meanMSVar = this.maxMSVerticalVar;
                this.meanMSIndexVar = this.maxMSVerticalIndexVar;
            }
        }

        log.info("Using {} with {}", this.meanMSVar, this.meanMSIndexVar);

        List<Array> ref;
        log.info("searching mean ms in ref");
        try {
            final IVariableFragment meanH = t.getFirst().getChild(
                    this.meanMSVar);
            meanH.setIndex(t.getFirst().getChild(this.meanMSIndexVar));
            ref = meanH.getIndexedArray();
        } catch (final ResourceNotAvailableException e) {
            log.info("computing mean mass spectras for reference");
            ref = getMeanMSForScanline(ScanLineCacheFactory.getScanLineCache(t.
                    getFirst()));
        }

        List<Array> query;
        log.info("searching mean ms in query");
        try {
            final IVariableFragment meanH = t.getSecond().getChild(
                    this.meanMSVar);
            meanH.setIndex(t.getSecond().getChild(this.meanMSIndexVar));
            query = meanH.getIndexedArray();
        } catch (final ResourceNotAvailableException e) {
            log.info("computing mean mass spectras for query");
            query = getMeanMSForScanline(ScanLineCacheFactory.getScanLineCache(t.
                    getSecond()));
        }

        if (this.scale) {
            log.info("Scaling");
            ref = ArrayTools2.sqrt(ref);
            query = ArrayTools2.sqrt(query);
        }

        if (this.filter) {
            log.info("Filtering");
            final List<Integer> usedMassesRef = ArrayTools2.getUsedMasses(t.
                    getFirst(), this.usedMassValuesVar);
            final List<Integer> usedMassesQuery = ArrayTools2.getUsedMasses(t.
                    getSecond(), this.usedMassValuesVar);
            ref = ArrayTools2.filterInclude(ref, usedMassesRef);
            query = ArrayTools2.filterInclude(query, usedMassesQuery);
        }

        return new Tuple2D<>(ref, query);
    }

    /**
     * Getter.
     *
     * @param slc scanline cache
     * @return list of mean mass spectras
     */
    private List<Array> getMeanMSForScanline(final IScanLine slc) {
        final List<Array> meanMS = new ArrayList<>();
        List<Array> scanline = null;
        Array sum = null;
        for (int i = 0; i < slc.getScanLineCount(); i++) {
            if (i % 100 == 0) {
                log.info("	{}", i);
            }
            scanline = slc.getScanlineMS(i);
            sum = null;
            for (final Array ms : scanline) {
                if (sum == null) {
                    sum = ms.copy();
                } else {
                    sum = ArrayTools.sum(sum, ms);
                }
            }
            meanMS.add(ArrayTools.mult(sum, scanline.size()));
        }
        return meanMS;
    }
}
