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
package maltcms.commands.fragments.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maltcms.io.csv.CSVWriter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import cross.Logging;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 * 
 * 
 */
@RequiresVariables(names = {"var.binned_mass_values",
    "var.binned_intensity_values", "var.binned_scan_index",
    "var.scan_acquisition_time"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class ObiWarplmataExporter extends AFragmentCommand {

    private String scanAcquisitionTimeVariableName = "scan_acquisition_time";
    private String binnedIntensitiesVariableName = "binned_intensity_values";
    private String binnedScanIndexVariableName = "binned_scan_index";
    private String binnedMassesVariableName = "binned_mass_values";

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
        final CSVWriter csvw = new CSVWriter();
        csvw.setWorkflow(getWorkflow());
        csvw.setFieldSeparator(" ");
        for (final IFileFragment iff : t) {
            log.info("Exporting file {}", iff.getName());
            final Array sat = iff.getChild(this.scanAcquisitionTimeVariableName).
                    getArray();
            final IVariableFragment bidx = iff.getChild(
                    this.binnedScanIndexVariableName);
            final IVariableFragment ms = iff.getChild(
                    this.binnedMassesVariableName);
            final IVariableFragment ins = iff.getChild(
                    this.binnedIntensitiesVariableName);
            ms.setIndex(bidx);
            ins.setIndex(bidx);
            log.info("Creating header...");
            final int nscans = sat.getShape()[0];
            final List<String> times = new ArrayList<String>(nscans);
            final IndexIterator sati = sat.getIndexIterator();
            while (sati.hasNext()) {
                times.add("" + sati.getDoubleNext());
            }
            final int nbins = ms.getIndexedArray().get(0).getShape()[0];
            final List<String> bins = new ArrayList<String>(nbins);
            final IndexIterator msi = ms.getIndexedArray().get(0).
                    getIndexIterator();
            while (msi.hasNext()) {
                bins.add("" + msi.getDoubleNext());
            }
            final List<List<String>> lines = new ArrayList<List<String>>();
            lines.add(Arrays.asList(nscans + ""));
            lines.add(times);
            lines.add(Arrays.asList(nbins + ""));
            lines.add(bins);
            log.info("Setting data...");
            for (final Array a : ins.getIndexedArray()) {
                final List<String> v = new ArrayList<String>(a.getShape()[0]);
                final IndexIterator ii = a.getIndexIterator();
                while (ii.hasNext()) {
                    v.add(ii.getDoubleNext() + "");
                }
                lines.add(v);
            }
            final File path = getWorkflow().getOutputDirectory(this);
            log.info("Writing data...");
            csvw.writeTableByRows(path.getAbsolutePath(), StringTools.
                    removeFileExt(iff.getName())
                    + ".lmata", lines, WorkflowSlot.FILEIO);
            lines.clear();
        }
        return t;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.fragments.AFragmentCommand#getDescription()
     */
    @Override
    public String getDescription() {
        return "Creates compatible lmata matrix files for use with Obi-Warp (http://obi-warp.sourceforge.net/)";
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
