package maltcms.commands.fragments2d.preprocessing;

import cross.annotations.Configurable;
import maltcms.datastructures.ms.Chromatogram2D;

import org.apache.commons.configuration.Configuration;

import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.StatsMap;
import cross.datastructures.Vars;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.MathTools;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.tools.ArrayTools;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;

public class GCGCToGCMSConverter extends AFragmentCommand {

    @Configurable(value="5",type=double.class)
    private double snrthreshold = 5;

    @Override
    public String getDescription() {
        return "GCxGC-MS data to GC-MS data by simple scan-wise summation.";
    }

    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> ret = new TupleND<IFileFragment>();
        for (IFileFragment ff : t) {
//			final int scanRate = ff.getChild(this.scanRateVar).getArray()
//			        .getInt(Index.scalarIndexImmutable);
//			final int modulationTime = ff.getChild(this.modulationTimeVar)
//			        .getArray().getInt(Index.scalarIndexImmutable);
//			final int scansPerModulation = scanRate * modulationTime;
//			this.log.debug("SPM: {}", scansPerModulation);

            IFileFragment retF = new FileFragment(getIWorkflow().getOutputDirectory(this), ff.getName());
//            retF.addSourceFile(ff);
            ArrayStatsScanner ass = new ArrayStatsScanner();
            Chromatogram2D c2 = new Chromatogram2D(ff);
            ArrayInt.D1 tic = new ArrayInt.D1(c2.getNumberOfModulations());
            ArrayDouble.D1 sat = new ArrayDouble.D1(c2.getNumberOfModulations());
            List<Tuple2D<Array, Array>> milist = new ArrayList<Tuple2D<Array, Array>>(c2.getNumberOfModulations());
            ArrayInt.D1 scanIndex = new ArrayInt.D1(c2.getNumberOfModulations());
            int offset = 0;
            System.out.println("Loading scans");
            Array osat = ff.getChild("scan_acquisition_time").getArray();
            double minMass = Double.POSITIVE_INFINITY, maxMass = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < c2.getNumberOfModulations(); k++) {
                System.out.println("Modulation: " + (k + 1) + " of " + c2.getNumberOfModulations());
                int ticv = 0;
                LinkedHashMap<Double, Integer> modulationMS = new LinkedHashMap<Double, Integer>(1000);
                double modtime = osat.getDouble(k * c2.getNumberOfScansPerModulation());
                for (int i = 0; i < c2.getNumberOfScansPerModulation(); i++) {
                    Tuple2D<Array, Array> ms = c2.getScanLineImpl().getSparseMassSpectra(k, i);

                    if (i == 0) {
                        sat.set(k, modtime);
                    }
                    Array masses = ms.getFirst();
                    Array intensities = ms.getSecond();
                    for (int j = 0; j < masses.getShape()[0]; j++) {
                        Integer intens = intensities.getInt(j);
                        double mass = masses.getDouble(j);
                        minMass = Math.min(mass, minMass);
                        maxMass = Math.max(mass, maxMass);
                        
                        if (modulationMS.containsKey(mass)) {
                            modulationMS.put(mass, modulationMS.get(masses.getDouble(j)) + intens);
                        } else {
                            modulationMS.put(mass, intens);
                        }
                    }
                }
                Array massValues = new ArrayDouble.D1(modulationMS.keySet().size());
                Array intenValues = new ArrayInt.D1(modulationMS.keySet().size());
                scanIndex.set(k, offset);
                int i = 0;
                for (Double d : modulationMS.keySet()) {
                    massValues.setDouble(i, d);
                    intenValues.setInt(i, modulationMS.get(d));
                    i++;
                }
                double median = MathTools.median((int[]) intenValues.getStorage());
                StatsMap sm = ass.apply(new Array[]{intenValues})[0];
                double stdev = Math.sqrt(sm.get(Vars.Variance.name()));
                double snr = - (20.0d * Math.log10(median / stdev));
                System.out.println("Median: "+median+" stdev: "+stdev+" snr: "+snr);
                for (int l = 0; l < intenValues.getShape()[0]; l++) {

                    if (snr < (this.snrthreshold)) {
                        intenValues.setInt(l, 0);
                    }else{
                        intenValues.setInt(l, (int)Math.max(0, intenValues.getInt(l)-median));
                    }
                    ticv += intenValues.getInt(l);
                }
                offset += i;
                tic.set(k, ticv);
                milist.add(new Tuple2D<Array, Array>(massValues, intenValues));
            }

            Array massVals = new ArrayDouble.D1(offset);
            Array intenVals = new ArrayInt.D1(offset);
            offset = 0;
            System.out.println("Setting mass and intensity values");
            for (Tuple2D<Array, Array> tpl : milist) {
                Array.arraycopy(tpl.getFirst(), 0, massVals, offset, tpl.getFirst().getShape()[0]);
                Array.arraycopy(tpl.getSecond(), 0, intenVals, offset, tpl.getSecond().getShape()[0]);
                offset += tpl.getFirst().getShape()[0];
            }

            IVariableFragment satvar = new VariableFragment(retF, "scan_acquisition_time");
            satvar.setArray(sat);
            IVariableFragment ticvar = new VariableFragment(retF, "total_intensity");
            ticvar.setArray(tic);
            IVariableFragment msvar = new VariableFragment(retF, "mass_values");
            msvar.setArray(massVals);
            IVariableFragment intvar = new VariableFragment(retF, "intensity_values");
            intvar.setArray(intenVals);
            IVariableFragment scanIndexVar = new VariableFragment(retF, "scan_index");
            scanIndexVar.setArray(scanIndex);
            IVariableFragment massRangeMinVar = new VariableFragment(retF, "mass_range_min");
            ArrayDouble.D1 mrminva = new ArrayDouble.D1(tic.getShape()[0]);
            ArrayTools.fill(mrminva, minMass);
            massRangeMinVar.setArray(mrminva);
            IVariableFragment massRangeMaxVar = new VariableFragment(retF, "mass_range_max");
            ArrayDouble.D1 mrmaxva = new ArrayDouble.D1(tic.getShape()[0]);
            ArrayTools.fill(mrmaxva, maxMass);
            massRangeMaxVar.setArray(mrmaxva);
            retF.save();
            ret.add(retF);
        }
        return ret;
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
        this.snrthreshold = cfg.getDouble(getClass().getName()+".snrthreshold", 5);
    }

    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}