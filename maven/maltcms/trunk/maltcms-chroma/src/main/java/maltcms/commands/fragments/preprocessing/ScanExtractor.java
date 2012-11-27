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
package maltcms.commands.fragments.preprocessing;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.ArrayTools;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * ScanExtractor allows to extract a contiguous number of mass spectral scans
 * from a GC/LC-MS file. This is useful for skipping lead-in scans that should
 * be removed prior to processing or to skip data that was written incorrectly.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@Data
@ProvidesVariables(names = {"var.scan_index", "var.scan_acquisition_time", "var.mass_values", "var.intensity_values", "var.total_intensity"})
@ServiceProvider(service = AFragmentCommand.class)
public class ScanExtractor extends AFragmentCommand {

    private final String description = "Allows definition of a start and end modulation period to be extracted from a raw GCxGC-MS chromatogram.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.GENERAL_PREPROCESSING;
    @Configurable(name = "var.total_intensity", value = "total_intensity")
    private String totalIntensityVar = "total_intensity";
    @Configurable(name = "var.scan_acquisition_time", value = "scan_acquisition_time")
    private String scanAcquisitionTimeVar = "scan_acquisition_time";
    @Configurable(name = "var.scan_index")
    private String scanIndexVar = "scan_index";
    @Configurable(value = "-1")
    private int startScan = -1;
    @Configurable(value = "-1")
    private int endScan = -1;

    protected Array get1DArraySubset(Array a, int startIndex, int endIndex) {
        int scans = endIndex - startIndex + 1;
        try {
            Range r = new Range(startIndex, endIndex);
            EvalTools.eqI(scans,r.length(),this);
            return a.section(Arrays.asList(r));
        } catch (InvalidRangeException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected void correctIndex(Array indexArray, int startIndex) {
        IndexIterator iter = indexArray.getIndexIterator();
        while (iter.hasNext()) {
            int idx = iter.getIntNext();
            iter.setIntCurrent(idx - startIndex);
        }
    }

    @Override
    public void configure(Configuration config) {
        this.totalIntensityVar = config.getString("var.total_intensity", "total_intensity");
        this.scanAcquisitionTimeVar = config.getString("var.scan_acquisition_time", "scan_acquisition_time");
        this.scanIndexVar = config.getString("var.scan_index", "scan_index");
    }

    /**
     * int[] contains startScan, endScan
     *
     * @param ff
     * @param startScan
     * @param endScan
     * @return
     */
    protected int[] checkRanges(IFileFragment ff, int startScan, int endScan) {
        log.debug("startScan: {}, endScan: {}", startScan, endScan);
        final int totalScans = ff.getChild(this.scanIndexVar).getArray().getShape()[0];
        log.debug("total scans: " + totalScans);
        int[] ranges = new int[]{0, 0};
        ranges[0] = Math.max(0, startScan);
        if (endScan < 0) {
            ranges[1] = totalScans - 1;
        } else {
            ranges[1] = Math.max(startScan + 1, Math.min(totalScans - 1, endScan));
        }
        if (ranges[0] >= ranges[1]) {
            throw new IllegalArgumentException("startScan must not be greater than or equal to endScan: " + ranges[0] + ">=" + ranges[1]);
        }
        return ranges;
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
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        final TupleND<IFileFragment> res = new TupleND<IFileFragment>();
        for (IFileFragment ff : t) {
            try {
                final IFileFragment work = createWorkFragment(ff);
                int[] ranges = checkRanges(ff, startScan, endScan);
                int nscans = (ranges[1] - ranges[0] + 1);
                log.info("Reading {} scans, from index {} to index {} (inclusive) for {}", new Object[]{nscans, ranges[0], ranges[1], ff.getName()});
                Array sat = get1DArraySubset(ff.getChild(scanAcquisitionTimeVar).getArray(), ranges[0], ranges[1]);
                Array tic = get1DArraySubset(ff.getChild(totalIntensityVar).getArray(), ranges[0], ranges[1]);

                IVariableFragment massesVar = ff.getChild("mass_values");
                IVariableFragment originalScanIndexVar = ff.getChild("scan_index");
                originalScanIndexVar.clear();
                originalScanIndexVar.setRange(new Range[]{new Range(ranges[0], ranges[1])});

                massesVar.setIndex(originalScanIndexVar);
                List<Array> massSubset = new ArrayList<Array>(nscans);
                EvalTools.eqI(nscans, sat.getShape()[0], this);
                List<Array> originalMasses = massesVar.getIndexedArray();
                for (int i = 0; i < nscans; i++) {
                    massSubset.add(i, originalMasses.get(i));
                }
                log.info("Retrieved {} mass value arrays", massSubset.size());
                int massDim = ArrayTools.getSizeForFlattenedArrays(massSubset);
                IVariableFragment intensVar = ff.getChild("intensity_values");
                intensVar.setIndex(originalScanIndexVar);
                List<Array> intensSubset = new ArrayList<Array>(nscans);
                List<Array> originalIntensities = intensVar.getIndexedArray();
                for (int i = 0; i < nscans; i++) {
                    intensSubset.add(i, originalIntensities.get(i));
                }
                int scanDim = intensSubset.size();
                log.info("Subset list has {} scans!", scanDim);
                EvalTools.eqI(scanDim, tic.getShape()[0], this);

                //correct the index array for new offset
                ArrayInt.D1 scanIndexArray = new ArrayInt.D1(intensSubset.size());
                int offset = 0;
                for (int i = 0; i < intensSubset.size(); i++) {
                    scanIndexArray.set(i, offset);
                    offset += intensSubset.get(i).getShape()[0];
                }

                IVariableFragment indexSubsetVar = new VariableFragment(work, "scan_index");
                indexSubsetVar.setArray(scanIndexArray);
                indexSubsetVar.setDimensions(adaptDimensions(originalScanIndexVar, new int[]{scanDim}));

                IVariableFragment targetMasses = new VariableFragment(work, massesVar.getName());
                targetMasses.setIndex(indexSubsetVar);
                targetMasses.setIndexedArray(massSubset);
                targetMasses.setDimensions(adaptDimensions(massesVar, new int[]{massDim}));
                IVariableFragment targetIntensities = new VariableFragment(work, intensVar.getName());
                targetIntensities.setIndex(indexSubsetVar);
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
            } catch (InvalidRangeException ex) {
                log.error("Invalid range:",ex);
            }
        }
        return res;
    }
}
