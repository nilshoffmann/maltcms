package maltcms.commands.fragments.preprocessing.denseArrayProducer;

import cross.Factory;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.ImmutableFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FragmentTools;
import cross.datastructures.tuple.Tuple2D;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools;
import maltcms.tools.MaltcmsTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.MAVector;

/**
 *
 * @author nils
 */
@Slf4j
@Data
public class DenseArrayProducerWorker implements Callable<File>, Serializable {

    private String massValues = "mass_values";
    private String intensityValues = "intensity_values";
    private String scanIndex = "scan_index";
    private String totalIntensity = "total_intensity";
    private String binnedIntensityValues = "binned_intensity_values";
    private String binnedMassValues = "binned_mass_values";
    private String binnedScanIndex = "binned_scan_index";
    private boolean normalizeScans = false;
    private List<Double> maskedMasses = new LinkedList<Double>();
    private boolean invertMaskedMasses = false;
    private boolean normalizeMeanVariance = false;
    private double massBinResolution = 1.0d;
    private double minMass = 0;
    private double maxMass = 1000;
    private File fileToLoad;
    private File fileToSave;

    @Override
    public File call() throws Exception {
        EvalTools.notNull(fileToLoad, this);
        EvalTools.notNull(fileToSave, this);
        IFileFragment input = new ImmutableFileFragment(fileToLoad);
        //create a new working fragment
        IFileFragment output = new FileFragment(fileToSave);
        output.addSourceFile(input);

        log.info("Loading scans for file {}", output.getName());
        MaltcmsTools.prepareDenseArraysMZI(input, output,
                scanIndex, massValues, intensityValues,
                binnedScanIndex, binnedMassValues,
                binnedIntensityValues, minMass, maxMass,null);

        log.debug("Loaded scans for file {}, stored in {}", fileToLoad, fileToSave);
        log.debug("Source Files of f={} : {}", output, output.getSourceFiles());
        final int bins = MaltcmsTools.getNumberOfIntegerMassBins(minMass, maxMass, massBinResolution);
        maskMasses(output, minMass, maxMass, massBinResolution, bins, maskedMasses, invertMaskedMasses, binnedIntensityValues, binnedScanIndex, totalIntensity);
        normalizeScans(output, normalizeScans, normalizeMeanVariance, binnedIntensityValues, binnedScanIndex);
        //save working fragment
        output.save();
        return new File(output.getAbsolutePath());
    }

    /**
     * @param minMass
     * @param maxMass
     * @param f
     * @param bins
     */
    protected void maskMasses(final IFileFragment f, final double minMass, final double maxMass, final double massBinResolution,
            final int bins, final List<Double> maskedMasses, final boolean invertMaskedMasses, final String binnedIntensityVariable, final String binnedScanIndexVariable, final String totalIntensityVariable) {
        // set masked masschannels to zero intensity
        if ((maskedMasses != null) && !maskedMasses.isEmpty()) {
            log.info("Filtering masked masses!");
            final ArrayDouble.D1 selector = new ArrayDouble.D1(bins);
            if (invertMaskedMasses) {
                ArrayTools.fill(selector, 1.0d);
                for (final Double integ : maskedMasses) {
                    log.info("Retaining mass {} at index {}", integ,
                            MaltcmsTools.binMZ(integ, minMass, maxMass, massBinResolution));

                    selector.set(MaltcmsTools.binMZ(integ, minMass,
                            maxMass, massBinResolution), 0.0d);
                    // - (int) (Math.floor(minmax.getFirst())), 0.0d);
                }
            } else {
                for (final Double integ : maskedMasses) {
                    log.info("Filtering mass {} at index {}", integ,
                            MaltcmsTools.binMZ(integ, minMass, maxMass, massBinResolution));
                    selector.set(MaltcmsTools.binMZ(integ, minMass,
                            maxMass, massBinResolution), 1.0d);
                    // - (int) (Math.floor(minmax.getFirst())), 1.0d);
                }
            }
            final IVariableFragment ivf = f.getChild(binnedIntensityVariable);
            final IVariableFragment sidx = f.getChild(binnedScanIndexVariable);
            // since we remove intensities in bins, we should also adjust the
            // TIC
            // in this case, we shadow previous definitions
            final IVariableFragment total_intens = f.hasChild(totalIntensityVariable) ? f.getChild(totalIntensityVariable) : new VariableFragment(f,
                    totalIntensityVariable);
            final Array tan = Array.factory(DataType.DOUBLE, sidx.getArray().getShape());
            final Index tanidx = tan.getIndex();
            ivf.setIndex(sidx);
            final List<Array> intens = ivf.getIndexedArray();
            // Over all scans
            int scan = 0;
            final ArrayList<Array> filtered = new ArrayList<Array>(intens.size());
            for (final Array a : intens) {
                // System.out.println("Before: " + a.toString());
                final Index aidx = a.getIndex();
                EvalTools.eqI(1, a.getRank(), this);
                // double accum = 0;
                for (int i = 0; i < a.getShape()[0]; i++) {
                    if (selector.get(i) == 1.0d) {
                        // log.info("Selector index {} = 1.0",i);
                        aidx.set(i);
                        a.setDouble(aidx, 0);
                    }

                }
                tanidx.set(scan);
                tan.setDouble(tanidx, ArrayTools.integrate(a));
                filtered.add(a);
                // System.out.println("After: " + a.toString());
                scan++;
            }
            ivf.setIndexedArray(filtered);
            total_intens.setArray(tan);
        }
    }

    /**
     * @param f
     */
    protected void normalizeScans(final IFileFragment f, final boolean normalizeScans, boolean normalizeMeanVariance, String binnedIntensityVariable, String binnedScanIndexVariable) {
        if (normalizeScans || normalizeMeanVariance) {
            final IVariableFragment ivf = f.getChild(binnedIntensityVariable);
            final IVariableFragment sidx = f.getChild(binnedScanIndexVariable);
            ivf.setIndex(sidx);
            final List<Array> intens = ivf.getIndexedArray();

            final List<Array> normIntens = new ArrayList<Array>();
            if (normalizeMeanVariance) {
                log.info("Normalizing by subtracting mean and dividing by variance!");
                final List<Array> tilted = ArrayTools.tilt(intens);
                final ArrayStatsScanner ass = Factory.getInstance().getObjectFactory().instantiate(ArrayStatsScanner.class);
                StatsMap[] sm = ass.apply(tilted.toArray(new Array[]{}));
                ArrayDouble.D1 mean = new ArrayDouble.D1(tilted.size());
                ArrayDouble.D1 var = new ArrayDouble.D1(tilted.size());
                for (int i = 0; i < tilted.size(); i++) {
                    mean.set(i, -sm[i].get(Vars.Mean.name()));
                    var.set(i, sm[i].get(Vars.Variance.name()));
                }
                for (final Array a : intens) {
                    final MAVector ma = new MAVector(a);
                    final double norm = ma.norm();
                    if (norm == 0.0) {
                        normIntens.add(a);
                    } else {
                        normIntens.add(ArrayTools.div(ArrayTools.diff(a, mean),
                                var));
                    }

                }
            } else if (normalizeScans) {
                log.info("Normalizing scans to length 1");
                for (final Array a : intens) {
                    final MAVector ma = new MAVector(a);
                    final double norm = ma.norm();
                    log.debug("Norm: {}", norm);
                    if (norm == 0.0) {
                        normIntens.add(a);
                    } else {
                        normIntens.add(ArrayTools.mult(a, 1.0d / norm));
                    }
                }
            }
            ivf.setIndexedArray(normIntens);
        }
    }
}