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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import maltcms.io.csv.CSVReader;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.ArrayInt;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class TICPeakListImporter extends AFragmentCommand {

    private final String description = "Imports tic peak data from csv files";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILEIO;
    @Configurable
    private List<String> filesToRead = Collections.emptyList();
    @Configurable
    private int scanIndexOffset = 0;
    @Configurable(value = "SCAN")
    private String scanIndexColumnName = "SCAN";
    @Configurable(value = "var.tic_peaks")
    private String ticPeakVarName = "tic_peaks";

    @Override
    public void configure(Configuration cfg) {
        super.configure(cfg);
        ticPeakVarName = cfg.getString("var.tic_peaks", "tic_peaks");
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.commands.ICommand#apply(java.lang.Object)
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> retf = new TupleND<IFileFragment>();
        //check for wildcard arguments
        if (this.filesToRead.size() == 1) {
            log.info("Parsing filesToRead as wildcard expression");
            String s = this.filesToRead.get(0);
            String fullpath = FilenameUtils.getFullPath(s);
            String wildcard = FilenameUtils.getName(s);
            WildcardFileFilter fileFilter = new WildcardFileFilter(wildcard);
            Collection<File> files = FileUtils.listFiles(new File(fullpath), fileFilter, TrueFileFilter.INSTANCE);
            this.filesToRead.clear();
            for (File f : files) {
                try {
                    filesToRead.add(f.getCanonicalPath());
                } catch (IOException ex) {
                    log.warn("{}", ex);
                }
            }

        }
        for (IFileFragment ff : t) {

            for (String s : this.filesToRead) {
                if (StringTools.removeFileExt(s).endsWith(
                        StringTools.removeFileExt(ff.getName()))) {
                    log.warn("Loading TIC peaks from file {}", s);
                    IFileFragment work = Factory.getInstance().
                            getFileFragmentFactory().create(
                            getWorkflow().getOutputDirectory(this),
                            ff.getName(), ff);
                    CSVReader csvr = Factory.getInstance().getObjectFactory().
                            instantiate(CSVReader.class);
                    Tuple2D<Vector<Vector<String>>, Vector<String>> table;
                    try {
                        table = csvr.read(new FileInputStream(s));
                        HashMap<String, Vector<String>> hm = csvr.getColumns(
                                table);
                        Vector<String> peakScanIndex = hm.get(
                                scanIndexColumnName);
                        ArrayInt.D1 extr = new ArrayInt.D1(peakScanIndex.size());
                        for (int i = 0; i < peakScanIndex.size(); i++) {
                            extr.set(i,
                                    Integer.parseInt(peakScanIndex.get(i)) + scanIndexOffset);
                        }
                        final IVariableFragment peaks = new VariableFragment(
                                work, this.ticPeakVarName);
                        final Dimension peak_number = new Dimension(
                                "peak_number", peakScanIndex.size(), true,
                                false, false);
                        peaks.setDimensions(new Dimension[]{peak_number});
                        peaks.setArray(extr);
                        retf.add(work);
                        work.save();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }

        }
        return retf;
    }
}
