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
package maltcms.commands.fragments.preprocessing;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 * 
 * ScanExtractor allows to extract a contiguous number of mass spectral scans 
 * from a GC/LC-MS file. This is useful for skipping lead-in scans that should be removed 
 * prior to processing or to skip data that was written incorrectly.
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 */
@Slf4j
@Data
@ProvidesVariables(names = {"var.scan_index", "var.scan_acquisition_time","var.mass_values","var.intensity_values","var.total_intensity"})
@ServiceProvider(service=AFragmentCommand.class)
public class ScanExtractor extends AFragmentCommand {

    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time", value = "scan_acquisition_time")
    private String scanAcquisitionTimeVar = "scan_acquisition_time";
    @Configurable(name = "var.scan_index")
    private String scanIndexVar = "scan_index";
    @Configurable(value = "-1", type = Integer.class)
    private int startScan = -1;
    @Configurable(value = "-1", type = Integer.class)
    private int endScan = -1;

    @Override
    public String toString() {
        return getClass().getName();
    }

    protected Array get1DArraySubset(Array a, int startIndex, int endIndex) {
        int scans = endIndex-startIndex+1;
        try {
            return a.section(new int[]{startIndex},new int[]{scans});
        } catch (InvalidRangeException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    protected void correctIndex(Array indexArray, int startIndex) {
        IndexIterator iter = indexArray.getIndexIterator();
        while(iter.hasNext()) {
            int idx = iter.getIntNext();
            iter.setIntCurrent(idx-startIndex);
        }
    }

    /**
     * int[] contains startScan, endScan
     * @param ff
     * @param startScan
     * @param endScan
     * @return
     */
    protected int[] checkRanges(IFileFragment ff, int startScan, int endScan) {
        log.debug("startScan: {}, endScan: {}",startScan,endScan);
        final int totalScans = ff.getChild(this.scanIndexVar).getArray().getShape()[0];
        log.debug("total scans: "+totalScans);
        int[] ranges = new int[]{0, 0};
        ranges[0] = Math.max(0, startScan);
        if(endScan<0) {
            ranges[1] = totalScans-1;
        }else{
            ranges[1] = Math.max(startScan+1, Math.min(totalScans - 1, endScan));
        }
        if(ranges[0]>=ranges[1]) {
            throw new IllegalArgumentException("startScan must not be greater than or equal to endScan: "+ranges[0]+">="+ranges[1]);
        }
        return ranges;
    }

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
            int[] ranges = checkRanges(ff, startScan, endScan);
            log.info("Reading 1D data from x: {} to {} for {}",new Object[]{ranges[0],ranges[1],ff.getName()});
            Array sat = get1DArraySubset(ff.getChild(scanAcquisitionTimeVar).getArray(), ranges[0], ranges[1]);
            Array tic = get1DArraySubset(ff.getChild(totalIntensityVar).getArray(), ranges[0], ranges[1]);
            Array indexSubset = get1DArraySubset(ff.getChild(scanIndexVar).getArray(), ranges[0], ranges[1]);
            log.debug("scan_index: {}",indexSubset);
            IVariableFragment indexSubsetVar = new VariableFragment(work, "scan_index");
            indexSubsetVar.setArray(indexSubset);
            IVariableFragment massesVar = ff.getChild("mass_values");
            IVariableFragment originalScanIndexVar = ff.getChild("scan_index");
            try {
                originalScanIndexVar.setRange(new Range[]{new Range(ranges[0],ranges[1])});
            } catch (InvalidRangeException ex) {
                Logger.getLogger(ScanExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            massesVar.setIndex(originalScanIndexVar);
            List<Array> massSubset = massesVar.getIndexedArray();
            log.debug("Retrieved {} mass value arrays",massSubset.size());
            int massDim = ArrayTools.getSizeForFlattenedArrays(massSubset);
            IVariableFragment intensVar = ff.getChild("intensity_values");
            intensVar.setIndex(originalScanIndexVar);
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

