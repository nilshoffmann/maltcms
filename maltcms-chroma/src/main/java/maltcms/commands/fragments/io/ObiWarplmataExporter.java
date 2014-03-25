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
package maltcms.commands.fragments.io;

import cross.annotations.Configurable;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.io.csv.CSVWriter;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

/**
 * @author Nils Hoffmann
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

    private final String description = "Creates compatible lmata matrix files for use with Obi-Warp (http://obi-warp.sourceforge.net/)";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILECONVERSION;
    @Configurable
    private String scanAcquisitionTimeVariableName = "scan_acquisition_time";
    @Configurable
    private String binnedIntensitiesVariableName = "binned_intensity_values";
    @Configurable
    private String binnedScanIndexVariableName = "binned_scan_index";
    @Configurable
    private String binnedMassesVariableName = "binned_mass_values";

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
            final List<String> times = new ArrayList<>(nscans);
            final IndexIterator sati = sat.getIndexIterator();
            while (sati.hasNext()) {
                times.add("" + sati.getDoubleNext());
            }
            final int nbins = ms.getIndexedArray().get(0).getShape()[0];
            final List<String> bins = new ArrayList<>(nbins);
            final IndexIterator msi = ms.getIndexedArray().get(0).
                    getIndexIterator();
            while (msi.hasNext()) {
                bins.add("" + msi.getDoubleNext());
            }
            final List<List<String>> lines = new ArrayList<>();
            lines.add(Arrays.asList(nscans + ""));
            lines.add(times);
            lines.add(Arrays.asList(nbins + ""));
            lines.add(bins);
            log.info("Setting data...");
            for (final Array a : ins.getIndexedArray()) {
                final List<String> v = new ArrayList<>(a.getShape()[0]);
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
}
