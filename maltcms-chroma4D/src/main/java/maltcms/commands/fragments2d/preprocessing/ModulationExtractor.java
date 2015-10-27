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
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.ArrayTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.MAMath;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * ModulationExtractor allows to subset the modulations contained in a GCxGC-MS
 * file, by setting the global scan_index variable and the
 * second_column_scan_index variables to their appropriate values. Therefor,
 * downstream commands should use those variables in order to read individual
 * modulations or use the Chromatogram2D class or one of the IScanLine
 * implementations.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.modulation_time",
    "var.scan_rate"})
@ProvidesVariables(names = {"var.scan_index", "var.scan_acquisition_time",
    "var.mass_values", "var.intensity_values", "var.total_intensity",
    "var.mass_range_min", "var.mass_range_max"})
@ServiceProvider(service = AFragmentCommand.class)
public class ModulationExtractor extends AFragmentCommand {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulationTimeVar = "modulation_time";
    @Configurable(name = "var.scan_rate", value = "scan_rate")
    private String scanRateVar = "scan_rate";
    @Configurable(name = "var.scan_acquisition_time", value = "scan_acquisition_time")
    private String scanAcquisitionTimeVar = "scan_acquisition_time";
    @Configurable(name = "var.scan_index")
    private String scanIndexVar = "scan_index";
    @Configurable(name = "var.mass_range_min")
    private String massRangeMinVar = "mass_range_min";
    @Configurable(name = "var.mass_range_max")
    private String massRangeMaxVar = "mass_range_max";
    @Configurable(name = "var.mass_values")
    private String massValuesVar = "mass_values";
    @Configurable(name = "var.intensity_values")
    private String intensityValuesVar = "intensity_values";
    @Configurable(value = "-1", description = "The index of the start modulation"
            + " to extract.")
    private int startModulation = -1;
    @Configurable(value = "-1", description = "The index of the last modulation"
            + " to extract.")
    private int endModulation = -1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Configuration cfg) {
        this.totalIntensityVar = cfg.getString("var.total_intensity",
                "total_intensity");
        this.modulationTimeVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
        this.scanAcquisitionTimeVar = cfg.getString("var.scan_acquisition_time", "scan_acquisition_time");
        this.massRangeMinVar = cfg.getString("var.mass_range_min", "mass_range_min");
        this.massRangeMaxVar = cfg.getString("var.mass_range_max", "mass_range_max");
        this.massValuesVar = cfg.getString("var.mass_values", "mass_values");
        this.intensityValuesVar = cfg.getString("var.intensity_values", "intensity_values");
    }

    /**
     * <p>
     * get1DArraySubset.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param modulationStart a int.
     * @param modulationEnd a int.
     * @param scansPerModulation a int.
     * @return a {@link ucar.ma2.Array} object.
     */
    protected Array get1DArraySubset(Array a, int modulationStart, int modulationEnd, int scansPerModulation) {
        return get1DArraySubset(a, modulationStart, modulationEnd, 0, scansPerModulation - 1, scansPerModulation);
    }

    /**
     * <p>
     * get1DArraySubset.</p>
     *
     * @param a a {@link ucar.ma2.Array} object.
     * @param modulationStart a int.
     * @param modulationEnd a int.
     * @param intraModulationStart a int.
     * @param intraModulationStop a int.
     * @param scansPerModulation a int.
     * @return a {@link ucar.ma2.Array} object.
     */
    protected Array get1DArraySubset(Array a, int modulationStart, int modulationEnd, int intraModulationStart, int intraModulationStop, int scansPerModulation) {
        int elementsPerModulation = intraModulationStop - intraModulationStart + 1;
        int modulations = modulationEnd - modulationStart + 1;
        int elements = modulations * elementsPerModulation;
        int copyRange = intraModulationStop - intraModulationStart + 1;
        Array ret = Array.factory(a.getElementType(), new int[]{elements});
        int targetOffset = 0;
        for (int i = 0; i < modulations; i++) {
            int currentModulation = modulationStart + i;
            try {
                Array slice = a.section(new int[]{currentModulation * scansPerModulation}, new int[]{copyRange});
                Array.arraycopy(slice, 0, ret, targetOffset, copyRange);
                targetOffset += (copyRange);
            } catch (InvalidRangeException ex) {
                log.warn("InvalidRangeException:", ex);
            }
        }
        return ret;
    }

    /**
     * <p>
     * modulationIndexToScanIndex.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param interModulationIndex a int.
     * @return a int.
     */
    protected int modulationIndexToScanIndex(IFileFragment ff, int interModulationIndex) {
        final double srv = ff.getChild(this.scanRateVar).getArray().getDouble(Index.scalarIndexImmutable);
        final double modT = ff.getChild(this.modulationTimeVar).getArray().getDouble(Index.scalarIndexImmutable);
        final int spm = (int) (srv * modT);
        return interModulationIndex * spm;
    }

    /**
     * <p>
     * intraModulationIndexToScanIndex.</p>
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param interModulationIndex a int.
     * @param intraModulationIndex a int.
     * @return a int.
     */
    protected int intraModulationIndexToScanIndex(IFileFragment ff, int interModulationIndex, int intraModulationIndex) {
        return modulationIndexToScanIndex(ff, interModulationIndex) + intraModulationIndex;
    }

    /**
     * int[] contains
     * modulationStart,modulationEnd,intraModulationStart,intraModulationStop
     *
     * @param ff a {@link cross.datastructures.fragments.IFileFragment} object.
     * @param modulationStart a int.
     * @param modulationEnd a int.
     * @param intraModulationStart a int.
     * @param intraModulationStop a int.
     * @return an array of int.
     */
    protected int[] checkRanges(IFileFragment ff, int modulationStart, int modulationEnd, int intraModulationStart, int intraModulationStop) {
        final double srv = ff.getChild(this.scanRateVar).getArray().getDouble(Index.scalarIndexImmutable);
        final double modT = ff.getChild(this.modulationTimeVar).getArray().getDouble(Index.scalarIndexImmutable);
        final int totalScans = ff.getChild(this.scanIndexVar).getDimensions()[0].getLength();
        final int spm = (int) (srv * modT);
        final int modulations = totalScans / spm;
        int[] ranges = new int[]{0, 0, 0, 0, 0};
        //inter modulation range (x axis)
        ranges[0] = Math.max(0, Math.min(modulations - 1, modulationStart));
        ranges[1] = Math.max(modulationStart, Math.min(modulations - 1, modulationEnd));
        //intra modulation range (y axis)
        ranges[2] = Math.max(0, Math.min(spm - 1, intraModulationStart));
        ranges[3] = Math.max(modulationStart, Math.min(intraModulationStart, spm - 1));
        ranges[4] = spm;
        return ranges;
    }

//    public Array getIndexedArraySubset(Array a, Array index, int modulationStart, int modulationEnd, int intraModulationStart, int intraModulationStop, int scansPerModulation) {
//
//    }
    /*
     * (non-Javadoc)
     *
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Allows definition of a start and end modulation period to be extracted from a raw GCxGC-MS chromatogram.";
    }

    private Dimension[] adaptDimensions(IVariableFragment source, int[] targetShape) {
        log.info("Adapting dimensions for {} with shape {}", source.getName(), Arrays.toString(targetShape));
        Dimension[] dimsSource = source.getDimensions();
        if (dimsSource.length != targetShape.length) {
            throw new ConstraintViolationException("Number of dimensions and target shape differ!");
        }
        Dimension[] dimsTarget = new Dimension[dimsSource.length];
        for (int i = 0; i < dimsSource.length; i++) {
            dimsTarget[i] = new Dimension(dimsSource[i].getName(), targetShape[i]);
        }
        return dimsTarget;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        final TupleND<IFileFragment> res = new TupleND<>();
        for (IFileFragment ff : t) {
            final IFileFragment work = createWorkFragment(ff);
            int[] ranges = checkRanges(ff, startModulation, endModulation, -1, -1);
            log.info("Reading 1D data from x: {} to {}; y: {} to {}, with {} scans per modulation", new Object[]{ranges[0], ranges[1], ranges[2], ranges[3], ranges[4]});
            Array sat = get1DArraySubset(ff.getChild(scanAcquisitionTimeVar).getArray(), ranges[0], ranges[1], ranges[4]);
            Array tic = get1DArraySubset(ff.getChild(totalIntensityVar).getArray(), ranges[0], ranges[1], ranges[4]);
            Array mass_range_min = get1DArraySubset(ff.getChild(massRangeMinVar).getArray(), ranges[0], ranges[1], ranges[4]);
            Array mass_range_max = get1DArraySubset(ff.getChild(massRangeMaxVar).getArray(), ranges[0], ranges[1], ranges[4]);
            Array indexSubset = get1DArraySubset(ff.getChild(scanIndexVar).getArray(), ranges[0], ranges[1], ranges[4]);
            IVariableFragment indexSubsetVar = new VariableFragment(work, "scan_index");
            indexSubsetVar.setArray(indexSubset);
            IVariableFragment massesVar = ff.getChild("mass_values");
            massesVar.setIndex(indexSubsetVar);
            List<Array> massSubset = massesVar.getIndexedArray();
            int massDim = ArrayTools.getSizeForFlattenedArrays(massSubset);
            IVariableFragment intensVar = ff.getChild("intensity_values");
            intensVar.setIndex(indexSubsetVar);
            List<Array> intensSubset = intensVar.getIndexedArray();

            //correct the index array for new offset
            int minIndex = (int) MAMath.getMinimum(indexSubset);
            IndexIterator iter = indexSubset.getIndexIterator();
            while (iter.hasNext()) {
                int current = iter.getIntNext();
                if (minIndex > current) {
                    throw new ConstraintViolationException("MinIndex " + minIndex + " is larger than current index " + current + "!");
                }
                iter.setIntCurrent(current - minIndex);
            }

            IVariableFragment targetIndex = new VariableFragment(work, scanIndexVar);
            targetIndex.setArray(indexSubset);
            IVariableFragment targetMasses = new VariableFragment(work, massesVar.getName());
            targetMasses.setIndexedArray(massSubset);
            targetMasses.setDimensions(adaptDimensions(massesVar, new int[]{massDim}));
            IVariableFragment targetIntensities = new VariableFragment(work, intensVar.getName());
            targetIntensities.setIndexedArray(intensSubset);
            targetIntensities.setDimensions(adaptDimensions(intensVar, new int[]{massDim}));

            IVariableFragment ticVar = new VariableFragment(work, totalIntensityVar);
            ticVar.setDimensions(new Dimension[]{new Dimension(ff.getChild(totalIntensityVar).getDimensions()[0].getName(), tic.getShape()[0])});
            ticVar.setArray(tic);

            IVariableFragment satVar = new VariableFragment(work, scanAcquisitionTimeVar);
            satVar.setDimensions(new Dimension[]{new Dimension(ff.getChild(scanAcquisitionTimeVar).getDimensions()[0].getName(), tic.getShape()[0])});
            satVar.setArray(sat);

            IVariableFragment mrminVar = new VariableFragment(work, massRangeMinVar);
            mrminVar.setDimensions(new Dimension[]{new Dimension(ff.getChild(totalIntensityVar).getDimensions()[0].getName(), tic.getShape()[0])});
            mrminVar.setArray(mass_range_min);
            IVariableFragment mrmaxVar = new VariableFragment(work, massRangeMaxVar);
            mrmaxVar.setDimensions(new Dimension[]{new Dimension(ff.getChild(totalIntensityVar).getDimensions()[0].getName(), tic.getShape()[0])});
            mrmaxVar.setArray(mass_range_max);

            work.setAttributes(ff.getAttributes().toArray(new Attribute[ff.getAttributes().size()]));
            work.save();
            res.add(work);
        }
        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflowElement#getWorkflowSlot()
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
