/*
 * Copyright (C) 2008-2010 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
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
 * $Id: ModulationExtractor.java 140 2010-07-12 11:13:35Z nilshoffmann $
 */
package maltcms.commands.fragments2d.preprocessing;

import java.util.logging.Level;


import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
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
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.IndexIterator;
import ucar.ma2.MAMath;
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
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
@Slf4j
@Data
@RequiresVariables(names = {"var.modulation_time",
    "var.scan_rate"})
@ProvidesVariables(names = {"var.scan_index", "var.scan_acquisition_time","var.mass_values","var.intensity_values","var.total_intensity","var.scan_rate","var.modulation_time"})
@ServiceProvider(service=AFragmentCommand.class)
public class ModulationExtractor extends AFragmentCommand {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.modulation_time", value = "modulation_time")
    private String modulationTimeVar = "modulation_time";
    @Configurable(name = "var.scan_rate", value = "scan_rate")
    private String scanRateVar = "scan_rate";
//    @Configurable(name = "var.scan_duration", value = "scan_duration")
//    private String scanDurationVar = "scan_duration";
//    @Configurable(name = "var.second_column_time", value = "second_column_time")
//    private String secondColumnTimeVar = "second_column_time";
//    @Configurable(name = "var.second_column_scan_index", value = "second_column_scan_index")
//    private String secondColumnScanIndexVar = "second_column_scan_index";
//    @Configurable(name = "var.total_intensity_1d", value = "total_intensity_1d")
//    private String totalIntensity1dVar = "total_intensity_1d";
    @Configurable(name = "var.scan_acquisition_time", value = "scan_acquisition_time")
    private String scanAcquisitionTimeVar = "scan_acquisition_time";
//    @Configurable(name = "var.scan_acquisition_1d", value = "scan_acquisition_1d")
//    private String scanAcquisitionTime1dVar = "scan_acquisition_1d";
    @Configurable(name = "var.scan_index")
    private String scanIndexVar = "scan_index";
    @Configurable(value = "-1", type = Integer.class)
    private int startModulation = -1;
    @Configurable(value = "-1", type = Integer.class)
    private int endModulation = -1;

    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);

        this.totalIntensityVar = cfg.getString("var.total_intensity",
                "total_intensity");
        this.modulationTimeVar = cfg.getString("var.modulation_time",
                "modulation_time");
        this.scanRateVar = cfg.getString("var.scan_rate", "scan_rate");
//        this.scanDurationVar = cfg.getString("var.scan_duration",
//                "scan_duration");
//        this.secondColumnTimeVar = cfg.getString("var.second_column_time",
//                "second_column_time");
//        this.secondColumnScanIndexVar = cfg.getString(
//                "var.second_column_scan_index", "second_column_scan_index");
//        this.totalIntensity1dVar = cfg.getString("var.total_intensity_1d",
//                "total_intensity_1d");
//        this.scanAcquisitionTimeVar = cfg.getString(
//                "var.scan_acquisition_time", "scan_acquisition_time");
//        this.scanAcquisitionTime1dVar = cfg.getString(
//                "var.scan_acquisition_time_1d", "scan_acquisition_time_1d");
        this.scanIndexVar = cfg.getString("var.scan_index", "scan_index");

        this.startModulation = cfg.getInt(getClass().getName()
                + ".startModulation", -1);
        this.endModulation = cfg.getInt(
                getClass().getName() + ".endModulation", -1);
    }

    protected Array get1DArraySubset(Array a, int modulationStart, int modulationEnd, int scansPerModulation) {
        return get1DArraySubset(a, modulationStart, modulationEnd, 0, scansPerModulation - 1, scansPerModulation);
    }

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
                java.util.logging.Logger.getLogger(ModulationExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            //int originalIdx = startIdx
            //ret.setDouble(i, a.get);
        }
        return ret;
    }

    protected int modulationIndexToScanIndex(IFileFragment ff, int interModulationIndex) {
        final double srv = ff.getChild(this.scanRateVar).getArray().getDouble(Index.scalarIndexImmutable);
        final double modT = ff.getChild(this.modulationTimeVar).getArray().getDouble(Index.scalarIndexImmutable);
//        final int totalScans = ff.getChild(this.scanIndexVar).getDimensions()[0].getLength();
        final int spm = (int) (srv * modT);
//        final int modulations = totalScans / spm;
        return interModulationIndex * spm;
    }

    protected int intraModulationIndexToScanIndex(IFileFragment ff, int interModulationIndex, int intraModulationIndex) {
        return modulationIndexToScanIndex(ff, interModulationIndex) + intraModulationIndex;
    }

    /**
     * int[] contains modulationStart,modulationEnd,intraModulationStart,intraModulationStop
     * @param ff
     * @param modulationStart
     * @param modulationEnd
     * @param intraModulationStart
     * @param intraModulationStop
     * @return
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
    @Override
    public String getDescription() {
        return "Allows definition of a start and end modulation period to be extracted from a raw GCxGC-MS chromatogram.";
    }

    private Dimension[] adaptDimensions(IVariableFragment source, int[] targetShape) {
        log.info("Adapting dimensions for {} with shape {}",source.getName(),Arrays.toString(targetShape));
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
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        final TupleND<IFileFragment> res = new TupleND<IFileFragment>();
        for (IFileFragment ff : t) {
            final IFileFragment work = createWorkFragment(ff);
            int[] ranges = checkRanges(ff, startModulation, endModulation, -1, -1);
            log.info("Reading 1D data from x: {} to {}; y: {} to {}, with {} scans per modulation",new Object[]{ranges[0],ranges[1],ranges[2],ranges[3],ranges[4]});
            Array sat = get1DArraySubset(ff.getChild(scanAcquisitionTimeVar).getArray(), ranges[0], ranges[1], ranges[4]);
            Array tic = get1DArraySubset(ff.getChild(totalIntensityVar).getArray(), ranges[0], ranges[1], ranges[4]);
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


//            final double srv = ff.getChild(this.scanRateVar).getArray().getDouble(Index.scalarIndexImmutable);
//            final double modT = ff.getChild(this.modulationTimeVar).getArray().getDouble(Index.scalarIndexImmutable);
//            final int globalNumberOfModulations = ff.getChild(this.secondColumnScanIndexVar).getArray().getShape()[0];
//            log.info("File contains {} modulations", globalNumberOfModulations);
//            final int globalStartIndex = (int) (Math.max(this.startModulation,
//                    0) * (int) srv * (int) modT);
//            log.info("Scan rate is {}", srv);
//            log.info("Modulation time is {}", modT);
//            int globalEndIndex = (int) (this.endModulation) * (int) srv
//                    * (int) modT;
//            int globalLastIndex = (int) (globalNumberOfModulations * (int) srv * (int) modT) - 1;
//            if (this.endModulation == -1) {
//                globalEndIndex = globalLastIndex;
//            }
//
//            globalEndIndex = Math.min(globalEndIndex, globalLastIndex);
//
//            int startMod = Math.max(this.startModulation, 0);
//            int endMod = this.endModulation == -1 ? globalNumberOfModulations - 1
//                    : Math.min(this.endModulation,
//                    globalNumberOfModulations - 1);
//            log.info("Reading from modulation: {} to {}", startMod, endMod);
//            log.info("Reading from global index {} to {}.", globalStartIndex,
//                    globalEndIndex);
//            final int numberOfModulations = endMod - startMod;
//            log.info("Reading {} modulations", numberOfModulations);
//            endMod = endMod - 1;
//
//            try {
//                log.debug("start mod: {}, end mod: {}", startMod, endMod);
//                final Range modRange = new Range(startMod, endMod);
//                final IVariableFragment origModulationIndex = ff.getChild(this.secondColumnScanIndexVar);
//                // origModulationIndex.setRange(new Range[] { modRange });
//                final Array sia = origModulationIndex.getArray().section(
//                        Arrays.asList(modRange));
//                // log.info("{}", sia);
//                final VariableFragment nScanIndex = new VariableFragment(work,
//                        this.secondColumnScanIndexVar);
//                AdditionFilter af = new AdditionFilter(-sia.getInt(0));
//                nScanIndex.setArray(af.apply(sia));
//                DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
//                        work.getAbsolutePath()), this, getWorkflowSlot(), work);
//                getWorkflow().append(dwr);
//            } catch (InvalidRangeException e) {
//                log.warn("{}", e.getLocalizedMessage());
//            }
//
//            try {
//                final Range r = new Range(globalStartIndex, globalEndIndex);
//                final IVariableFragment origScanIndex = ff.getChild(this.scanIndexVar);
//                origScanIndex.setRange(new Range[]{r});
//                final Array sia = origScanIndex.getArray();
//                final VariableFragment nScanIndex = new VariableFragment(work,
//                        this.scanIndexVar);
//                AdditionFilter af = new AdditionFilter(-sia.getInt(0));
//                nScanIndex.setArray(af.apply(sia));
//                VariableFragment scanAcquisitionTime = new VariableFragment(work, this.scanAcquisitionTimeVar);
//                IVariableFragment origSAT = ff.getChild(this.scanAcquisitionTimeVar);
//                origSAT.setRange(new Range[]{r});
//                scanAcquisitionTime.setArray(origSAT.getArray());
//
//                DefaultWorkflowResult dwr = new DefaultWorkflowResult(new File(
//                        work.getAbsolutePath()), this, getWorkflowSlot(), work);
//                getWorkflow().append(dwr);
//            } catch (InvalidRangeException ire) {
//                log.warn("{}", ire.getLocalizedMessage());
//            }
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
    @Override
    public WorkflowSlot getWorkflowSlot() {
        return WorkflowSlot.GENERAL_PREPROCESSING;
    }
}
