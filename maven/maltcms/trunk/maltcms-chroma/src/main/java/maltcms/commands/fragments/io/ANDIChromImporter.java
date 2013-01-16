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
package maltcms.commands.fragments.io;

import cross.annotations.AnnotationInspector;
import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ExitVmException;
import cross.exception.ResourceNotAvailableException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.tools.ArrayTools;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;

/**
 *
 * Currently only generates scan_acquisition_time for plotting and alignment
 * purposes. Mapping of ordinate_values is performed by setting
 * var.total_intensity=ordinate_values.
 *
 * @author Nils Hoffmann
 *
 *
 */
@ProvidesVariables(names = {"var.scan_acquisition_time", "var.total_intensity", "var.mass_values", "var.intensity_values"})
@RequiresVariables(names = {"var.ordinate_values", "var.actual_sampling_interval", "var.actual_delay_time"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class ANDIChromImporter extends AFragmentCommand {

    @Configurable(name = "var.ordinate_values")
    private String ordinateValuesVariable = "ordinate_values";
    @Configurable(name = "var.scan_acquisition_time")
    private String scanAcquisitionTimeVariable = "scan_acquisition_time";
    @Configurable(name = "var.actual_sampling_interval")
    private String actualSamplingIntervalVariable = "actual_sampling_interval";
    @Configurable(name = "var.actual_delay_time")
    private String actualDelayTimeVariable = "actual_delay_time";
    private double rtStartTime = Double.NEGATIVE_INFINITY;
    private double rtStopTime = Double.POSITIVE_INFINITY;

    @Override
    public String toString() {
        return getClass().getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        final ArrayList<IFileFragment> ret = new ArrayList<IFileFragment>();
        for (final IFileFragment iff : t) {
            final IFileFragment fret = new FileFragment(
                    new File(getWorkflow().getOutputDirectory(this),
                    iff.getName()));
            final Array a = iff.getChild(this.ordinateValuesVariable).getArray();
            final ArrayDouble.D1 sat = new ArrayDouble.D1(a.getShape()[0]);
            final Array adt = iff.getChild(this.actualDelayTimeVariable).
                    getArray();
            final double rtStart = adt.getDouble(0);
            final Array sa = iff.getChild(this.actualSamplingIntervalVariable).
                    getArray();
            final double asi = sa.getDouble(0);
            int startIndex = 0;
            int stopIndex = sat.getShape()[0] - 1;
            for (int i = 0; i < sat.getShape()[0]; i++) {
                sat.set(i, rtStart + ((i) * asi));
            }
            //determine start and end index within rt bounds
            if (rtStartTime > Double.NEGATIVE_INFINITY || rtStartTime < Double.POSITIVE_INFINITY) {
                int start = -1;
                int stop = -1;
                for (int i = 0; i < sat.getShape()[0]; i++) {
                    double satVal = sat.getDouble(i);
                    if (start == -1 && satVal >= rtStartTime) {
                        start = i;
                    }
                    if (stop == -1 && satVal > rtStopTime) {
                        stop = i - 1;
                    }
                }
                if (start != -1) {
                    startIndex = start;
                }
                if (stop != -1) {
                    stopIndex = stop;
                }
            }
            int nscans = stopIndex - startIndex + 1;
            final VariableFragment tic = new VariableFragment(fret, "total_intensity");
            try {
                tic.setArray(a.section(new int[startIndex], new int[nscans]));
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ANDIChromImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            final ArrayInt.D1 scanIndex = ArrayTools.indexArray(nscans, 0);
            final VariableFragment siV = new VariableFragment(fret, "scan_index");
            siV.setArray(scanIndex);
            fret.addSourceFile(iff);
            final VariableFragment vf = new VariableFragment(fret,
                    this.scanAcquisitionTimeVariable);
            try {
                vf.setArray(sat.section(new int[startIndex], new int[nscans]));
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ANDIChromImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            final Array mass_values = new ArrayDouble.D1(a.getShape()[0]);
            final Array intensity_values = new ArrayDouble.D1(a.getShape()[0]);
            for (int i = 0; i < a.getShape()[0]; i++) {
                mass_values.setDouble(i, 0);
                intensity_values.setDouble(i, a.getDouble(i));
            }
            final VariableFragment massValuesV = new VariableFragment(fret, "mass_values");
            try {
                massValuesV.setArray(mass_values.section(new int[startIndex], new int[nscans]));
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ANDIChromImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            final VariableFragment intensityValuesV = new VariableFragment(fret, "intensity_values");
            try {
                intensityValuesV.setArray(intensity_values.section(new int[startIndex], new int[nscans]));
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ANDIChromImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            final Array mass_range_min = new ArrayDouble.D1(mass_values.getShape()[0]);
            final Array mass_range_max = new ArrayDouble.D1(mass_values.getShape()[0]);
            ArrayTools.fill(mass_range_max, 1.0d);
            final VariableFragment massRangeMin = new VariableFragment(fret, "mass_range_min");
            try {
                massRangeMin.setArray(mass_range_min.section(new int[startIndex], new int[nscans]));
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ANDIChromImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            final VariableFragment massRangeMax = new VariableFragment(fret, "mass_range_max");
            try {
                massRangeMax.setArray(mass_range_max.section(new int[startIndex], new int[nscans]));
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ANDIChromImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            fret.save();
            ret.add(fret);
        }
        return new TupleND<IFileFragment>(ret);
    }

    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
        this.ordinateValuesVariable = cfg.getString(getClass().getName()
                + ".ordinate_values", "ordinate_values");
        this.scanAcquisitionTimeVariable = cfg.getString(getClass().getName()
                + ".scan_acquisition_time", "scan_acquisition_time");
        this.actualSamplingIntervalVariable = cfg.getString(getClass().getName()
                + ".actual_sampling_interval", "actual_sampling_interval");
        this.actualDelayTimeVariable = cfg.getString(getClass().getName()
                + ".actual_delay_time", "actual_delay_time");
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Maps data in ANDIChrom format to Maltcms/ANDIMS compatible naming scheme.";
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.FILECONVERSION;
    }
}
