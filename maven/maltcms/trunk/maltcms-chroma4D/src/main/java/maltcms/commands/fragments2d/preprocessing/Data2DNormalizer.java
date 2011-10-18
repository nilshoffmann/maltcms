package maltcms.commands.fragments2d.preprocessing;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;

import maltcms.commands.filters.array.MovingAverageFilter;
import maltcms.commands.filters.array.MovingMedianFilter;
import maltcms.commands.filters.array.TopHatFilter;
import maltcms.tools.ImageTools;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

@Slf4j
@Data
@ProvidesVariables(names = {"var.excluded_masses",
    "var.total_intensity_filtered"})
@ServiceProvider(service=AFragmentCommand.class)
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
    @Configurable
    private boolean multiplyWithTic = false;

    @Override
    public String getDescription() {
        return "Normalizes 2D / GCxGC-MS data";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> ret = new TupleND<IFileFragment>();
        for (IFileFragment ff : t) {
            final int scanRate = ff.getChild(this.scanRateVar).getArray().getInt(
                    Index.scalarIndexImmutable);
            final int modulationTime = ff.getChild(this.modulationTimeVar).
                    getArray().getInt(Index.scalarIndexImmutable);
            final int scansPerModulation = scanRate * modulationTime;
            log.debug("SPM: {}", scansPerModulation);

            IFileFragment retF = new FileFragment(getWorkflow().
                    getOutputDirectory(this), ff.getName());
            retF.addSourceFile(ff);

            IVariableFragment index = ff.getChild(this.secondScanIndexVar);
            log.debug("Index: {}", index.getArray());
            ff.getChild(this.totalIntensityVar).setIndex(index);
            List<Array> intensities = ff.getChild(this.totalIntensityVar).
                    getIndexedArray();
            final int scanLineCount = intensities.size();
            log.info("Filtering signal with {} modulations.", intensities.size());
            intensities = filterSignal(ff, scanLineCount, scansPerModulation,
                    intensities);
            log.info("Filtered signal with {} modulations.", intensities.size());

            if (this.multiplyWithTic) {
                List<Array> intensitiesB = ff.getChild(this.totalIntensityVar).
                        getIndexedArray();
                IndexIterator iter1, iter2;
                for (int i = 0; i < intensities.size(); i++) {
                    iter1 = intensitiesB.get(i).getIndexIterator();
                    iter2 = intensities.get(i).getIndexIterator();
                    while (iter1.hasNext() && iter2.hasNext()) {
                        iter2.setDoubleCurrent(iter2.getDoubleNext()
                                * Math.sqrt(iter1.getDoubleNext()));
                    }
                }
            }

            IVariableFragment intensVar = new VariableFragment(retF,
                    this.totalIntensityFilteredVar);
            intensVar.setIndexedArray(intensities);

            retF.save();
            ret.add(retF);
        }
        return ret;
    }

    private List<Array> filterSignal(IFileFragment f, final int scanLineCount,
            final int scansPerModulation, final List<Array> intensities) {
        log.info("Filtering {} modulations", intensities.size());
        Array[] values = null;
        values = new Array[intensities.size()];
        int cnt = 0;
        for (Array a : intensities) {
            values[cnt] = a;
            cnt++;
        }

        Array tic = cross.datastructures.tools.ArrayTools.glue(intensities);

        ArrayDouble.D2 tic2d = create2DArray(scanLineCount, scansPerModulation,
                values);
        RenderedImage bi = ImageTools.makeImage2D(tic2d, 256,
                Double.NEGATIVE_INFINITY);
        ImageTools.saveImage(ImageTools.flipVertical(bi), f.getName()
                + "-TIC2D-beforeFilters", "png", getWorkflow().
                getOutputDirectory(this), this);
        if (applyMovingAverage && movingAverageWindow > -1) {
            log.info("Applying moving average filter with window size y: {}",
                    (movingAverageWindow * 2) + 1);
            tic = applyMovingAverageFilter(scanLineCount, scansPerModulation,
                    tic, movingAverageWindow);
        }
        if (applyMovingMedian && movingMedianWindow > -1) {
            System.out.println("Using moving median filter");
            log.info("Applying moving median filter with window size y: {}",
                    (movingMedianWindow * 2) + 1);
            tic = applyMovingMedianFilter(scanLineCount, scansPerModulation,
                    tic, movingMedianWindow);
        }
        if (applyTopHatFilter && topHatFilterWindow > -1) {
            log.info("Applying top hat filter with window size y: {}",
                    (topHatFilterWindow * 2) + 1);
            tic = applyTopHatFilter(scanLineCount, scansPerModulation, tic,
                    topHatFilterWindow);
        }
        tic2d = create2DArray(scanLineCount, scansPerModulation, tic);
        bi = ImageTools.makeImage2D(tic2d, 256, Double.NEGATIVE_INFINITY);
        ImageTools.saveImage(ImageTools.flipVertical(bi), f.getName()
                + "-TIC2D-afterAllFilters", "png", getWorkflow().
                getOutputDirectory(this), this);
        return createReturnArray(scanLineCount, scansPerModulation, tic2d);
    }

    private ArrayDouble.D2 create2DArray(final int scanLineCount,
            final int scansPerModulation, Array totalIntensity) {
        log.debug("Creating 2d array with {}x{} elements", scanLineCount,
                scansPerModulation);
        ArrayDouble.D2 tic2d = new ArrayDouble.D2(scanLineCount,
                scansPerModulation);
        for (int x = 0; x < scanLineCount; x++) {
            try {
                Array arr = totalIntensity.section(new int[]{x
                            * scansPerModulation},
                        new int[]{scansPerModulation});
                Index arrIdx = arr.getIndex();
                for (int y = 0; y < scansPerModulation; y++) {
                    final double currentHeight = arr.getDouble(arrIdx.set(y));
                    tic2d.set(x, y, currentHeight);
                }
            } catch (InvalidRangeException ire) {
                log.warn(ire.getLocalizedMessage());
            }
        }
        return tic2d;
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

    private Array applyMovingAverageFilter(final int scanLineCount,
            final int scansPerModulation, Array tic, int windowy) {
        MovingAverageFilter thf = new MovingAverageFilter();
        thf.setWindow(windowy);
        return thf.apply(tic);
    }

    private Array applyMovingMedianFilter(final int scanLineCount,
            final int scansPerModulation, Array tic, int windowy) {
        MovingMedianFilter mmf = new MovingMedianFilter();
        mmf.setWindow(windowy);
        return mmf.apply(tic);
    }

    private Array applyTopHatFilter(final int scanLineCount,
            final int scansPerModulation, Array tic, int windowy) {
        TopHatFilter thf = new TopHatFilter();
        thf.setWindow(windowy);
        return thf.apply(tic);
    }

    private List<Array> createReturnArray(final int scanLineCount,
            final int scansPerModulation, ArrayDouble.D2 tic2d) {
        List<Array> retintensities = new ArrayList<Array>(scanLineCount);
        for (int x = 0; x < scanLineCount; x++) {
            ArrayDouble.D1 arr = new ArrayDouble.D1(scansPerModulation);
            for (int y = 0; y < scansPerModulation; y++) {
                arr.set(y, tic2d.get(x, y));
            }
            retintensities.add(arr);
        }
        log.debug("Number of modulations in return array: {}", retintensities.
                size());
        return retintensities;
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
        this.applyMovingAverage = cfg.getBoolean(this.getClass().getName()
                + ".applyMovingAverage", false);
        this.movingAverageWindow = cfg.getInt(this.getClass().getName()
                + ".movingAverageWindow", 3);
        this.applyMovingMedian = cfg.getBoolean(this.getClass().getName()
                + ".applyMovingMedian", true);
        this.movingMedianWindow = cfg.getInt(this.getClass().getName()
                + ".movingMedianWindow", 3);
        this.applyTopHatFilter = cfg.getBoolean(this.getClass().getName()
                + ".applyTopHatFilter", true);
        this.topHatFilterWindow = cfg.getInt(this.getClass().getName()
                + ".topHatFilterWindow", 5);
        this.multiplyWithTic = cfg.getBoolean(this.getClass().getName()
                + ".multiplyWithTic", false);
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
