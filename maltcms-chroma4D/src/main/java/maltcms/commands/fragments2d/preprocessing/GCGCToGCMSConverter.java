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
package maltcms.commands.fragments2d.preprocessing;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
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
import lombok.Data;

import maltcms.commands.scanners.ArrayStatsScanner;
import maltcms.datastructures.ms.Chromatogram2D;
import maltcms.tools.ArrayTools;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * Reduces a GCxGC-MS chromatogram to its one-dimensional representation by
 * summing over modulations.
 *
 * @author Nils Hoffmann
 *
 */
@ProvidesVariables(names = {
    "var.scan_acquisition_time",
    "var.total_intensity",
    "var.mass_values",
    "var.intensity_values",
    "var.scan_index",
    "var.mass_range_min",
    "var.mass_range_max"}
)
@RequiresVariables(names = {
    "var.scan_acquisition_time",
    "var.total_intensity",
    "var.mass_values",
    "var.intensity_values",
    "var.scan_index",
    "var.mass_range_min",
    "var.mass_range_max",
    "var.modulation_time",
    "var.scan_rate"}
)

@Data
@ServiceProvider(service = AFragmentCommand.class)
public class GCGCToGCMSConverter extends AFragmentCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GCGCToGCMSConverter.class);

    @Configurable(value = "5", description = "The signal-to-noise threshold."
            + "Intensities below this value will be set to 0.")
    private double snrthreshold = 5;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "GCxGC-MS data to GC-MS data by simple scan-wise summation.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> ret = new TupleND<>();
        for (IFileFragment ff : t) {
            IFileFragment retF = new FileFragment(getWorkflow().getOutputDirectory(this), ff.getName());
            ArrayStatsScanner ass = new ArrayStatsScanner();
            Chromatogram2D c2 = new Chromatogram2D(ff);
            ArrayInt.D1 tic = new ArrayInt.D1(c2.getNumberOfModulations());
            ArrayDouble.D1 sat = new ArrayDouble.D1(c2.getNumberOfModulations());
            List<Tuple2D<Array, Array>> milist = new ArrayList<>(c2.getNumberOfModulations());
            ArrayInt.D1 scanIndex = new ArrayInt.D1(c2.getNumberOfModulations());
            int offset = 0;
            log.info("Loading scans");
            Array osat = ff.getChild("scan_acquisition_time").getArray();
            double minMass = Double.POSITIVE_INFINITY, maxMass = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < c2.getNumberOfModulations(); k++) {
                log.info("Modulation: " + (k + 1) + " of " + c2.getNumberOfModulations());
                int ticv = 0;
                LinkedHashMap<Double, Integer> modulationMS = new LinkedHashMap<>(1000);
                double modtime = osat.getDouble(k * c2.getNumberOfScansPerModulation());
                for (int i = 0; i < c2.getNumberOfScansPerModulation(); i++) {
                    if (i == 0) {
                        sat.set(k, modtime);
                    }
                    Tuple2D<Array, Array> ms = c2.getScanLineImpl().getSparseMassSpectrum(k, i);
                    if (ms == null) {
                        log.warn("No mass spectrum at point {},{}", k, i);
                    } else {
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
                if (intenValues.getShape()[0] > 0) {
                    double median = MathTools.median((int[]) intenValues.getStorage());
                    StatsMap sm = ass.apply(new Array[]{intenValues})[0];
                    double stdev = Math.sqrt(sm.get(Vars.Variance.name()));
                    double snr = -(20.0d * Math.log10(median / stdev));
                    log.info("Median: " + median + " stdev: " + stdev + " snr: " + snr);
                    for (int l = 0; l < intenValues.getShape()[0]; l++) {

                        if (snr < (this.snrthreshold)) {
                            intenValues.setInt(l, 0);
                        } else {
                            intenValues.setInt(l, (int) Math.max(0, intenValues.getInt(l) - median));
                        }
                        ticv += intenValues.getInt(l);
                    }
                }
                offset += i;
                tic.set(k, ticv);
                milist.add(new Tuple2D<>(massValues, intenValues));
            }

            Array massVals = new ArrayDouble.D1(offset);
            Array intenVals = new ArrayInt.D1(offset);
            offset = 0;
            log.info("Setting mass and intensity values");
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
    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration cfg) {
        this.snrthreshold = cfg.getDouble(getClass().getName() + ".snrthreshold", 5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
