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
package maltcms.commands.fragments2d.preprocessing;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.awt.image.RenderedImage;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.filters.array.MovingAverageFilter;
import maltcms.commands.filters.array.MovingMedianFilter;
import maltcms.commands.filters.array.TopHatFilter;
import maltcms.tools.ImageTools;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
@ProvidesVariables(names = {"var.excluded_masses",
    "var.total_intensity_filtered"})
@ServiceProvider(service = AFragmentCommand.class)
public class Data2DNormalizer extends AFragmentCommand {

    @Configurable(name = "var.total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.total_intensity_filtered")
    private String totalIntensityFilteredVar = "total_intensity_filtered";
    @Configurable(name = "var.scan_rate")
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.modulation_time")
    private String modulationTimeVar = "modulation_time";
    @Configurable(name = "var.second_column_scan_index")
    private String secondScanIndexVar = "second_column_scan_index";
    @Configurable
    private boolean applyMovingAverage = false;
    @Configurable
    private int movingAverageWindow = 3;
    @Configurable
    private boolean applyMovingMedian = true;
    @Configurable
    private int movingMedianWindow = 3;
    @Configurable
    private boolean applyTopHatFilter = true;
    @Configurable
    private int topHatFilterWindow = 5;

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "Normalizes 2D / GCxGC-MS data";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> ret = new TupleND<IFileFragment>();
        for (IFileFragment ff : t) {
            final double scanRate = ff.getChild(this.scanRateVar).getArray().getDouble(
                Index.scalarIndexImmutable);
            final double modulationTime = ff.getChild(this.modulationTimeVar).
                getArray().getDouble(Index.scalarIndexImmutable);
            final int scansPerModulation = (int) (scanRate * modulationTime);
            log.debug("SPM: {}", scansPerModulation);

            IFileFragment retF = new FileFragment(getWorkflow().
                getOutputDirectory(this), ff.getName());
            retF.addSourceFile(ff);

            IVariableFragment index = ff.getChild(this.secondScanIndexVar);
            log.debug("Index: {}", index.getArray());
            IVariableFragment ticVar = ff.getChild(this.totalIntensityVar);
            ticVar.setIndex(index);
            List<Array> intensities = ticVar.
                getIndexedArray();

            Array tic = createImage(intensities, scansPerModulation, ff, "beforeFilter");

            log.info("Filtering signal with {} modulations.", intensities.size());
            Array filteredIntensities = filterSignal(tic);

            log.info("Filtered signal with {} modulations.", intensities.size());
            IVariableFragment intensVar = new VariableFragment(retF,
                this.totalIntensityFilteredVar);
            intensVar.setArray(filteredIntensities);

            retF.save();
            ret.add(retF);

            IVariableFragment tifv = retF.getChild(this.totalIntensityFilteredVar);
            tifv.setIndex(index);
            createImage(tifv.getIndexedArray(), scansPerModulation, ff, "afterFilter");

        }
        return ret;
    }

    private Array filterSignal(Array tic) {
//        log.info("Filtering {} modulations", intensities.size());
//        Array tic = createImage(intensities, scanLineCount, scansPerModulation, f, "beforeFilter");
        if (applyMovingAverage && movingAverageWindow > -1) {
            log.info("Applying moving average filter with window size y: {}",
                (movingAverageWindow * 2) + 1);
            tic = applyMovingAverageFilter(tic, movingAverageWindow);
        }
        if (applyMovingMedian && movingMedianWindow > -1) {
            System.out.println("Using moving median filter");
            log.info("Applying moving median filter with window size y: {}",
                (movingMedianWindow * 2) + 1);
            tic = applyMovingMedianFilter(tic, movingMedianWindow);
        }
        if (applyTopHatFilter && topHatFilterWindow > -1) {
            log.info("Applying top hat filter with window size y: {}",
                (topHatFilterWindow * 2) + 1);
            tic = applyTopHatFilter(tic, topHatFilterWindow);
        }

        return tic;
    }

    private Array createImage(final List<Array> intensities, final int scansPerModulation, IFileFragment f, String name) {
        Array[] values = null;
        values = new Array[intensities.size()];
        int cnt = 0;
        for (Array a : intensities) {
            values[cnt] = a;
            cnt++;
        }
        Array tic = cross.datastructures.tools.ArrayTools.glue(intensities);
        ArrayDouble.D2 tic2d = create2DArray(intensities.size(), scansPerModulation,
            values);
        RenderedImage bi = ImageTools.makeImage2D(tic2d, 256,
            Double.NEGATIVE_INFINITY);
        ImageTools.saveImage(ImageTools.flipVertical(bi), StringTools.removeFileExt(f.getName())
            + "-" + name, "png", getWorkflow().
            getOutputDirectory(this), this);
        return tic;
    }

    private ArrayDouble.D2 create2DArray(final int scanLineCount,
        final int scansPerModulation, Array[] totalIntensity) {
        log.debug("Creating 2d array with {}x{} elements", scanLineCount,
            scansPerModulation);
        ArrayDouble.D2 tic2d = new ArrayDouble.D2(scanLineCount,
            scansPerModulation);
        for (int x = 0; x < scanLineCount; x++) {
            Array arr = totalIntensity[x];
            Index arrIdx = arr.getIndex();
            for (int y = 0; y < scansPerModulation; y++) {
                final double currentHeight = arr.getDouble(arrIdx.set(y));
                tic2d.set(x, y, currentHeight);
            }
        }
        return tic2d;
    }

    private Array applyMovingAverageFilter(Array tic, int windowy) {
        MovingAverageFilter thf = new MovingAverageFilter();
        thf.setWindow(windowy);
        return thf.apply(tic);
    }

    private Array applyMovingMedianFilter(Array tic, int windowy) {
        MovingMedianFilter mmf = new MovingMedianFilter();
        mmf.setWindow(windowy);
        return mmf.apply(tic);
    }

    private Array applyTopHatFilter(Array tic, int windowy) {
        TopHatFilter thf = new TopHatFilter();
        thf.setWindow(windowy);
        return thf.apply(tic);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.commands.fragments.AFragmentCommand#configure(org.apache.commons
     * .configuration.Configuration)
     */
    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
        this.totalIntensityVar = cfg.getString(this.getClass().getName()
            + ".totalIntensityVar", "total_intensity");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
        this.modulationTimeVar = cfg.getString("var.modulation_time",
            "modulation_time");
        this.secondScanIndexVar = cfg.getString("var.second_column_scan_index",
            "second_column_scan_index");
    }

    /**
     *
     * @return
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
