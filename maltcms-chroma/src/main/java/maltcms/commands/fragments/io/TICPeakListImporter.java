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

import cross.Factory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.tools.StringTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import lombok.Data;

import maltcms.io.csv.CSVReader;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayInt;
import ucar.nc2.Dimension;

/**
 * <p>
 * TICPeakListImporter class.</p>
 *
 * @author Nils Hoffmann
 *
 */

@Data
@ServiceProvider(service = AFragmentCommand.class)
public class TICPeakListImporter extends AFragmentCommand {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TICPeakListImporter.class);

    private final String description = "Imports tic peak data from tab "
            + "separated value (tsv) files with column header";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILEIO;
    @Configurable(description = "The list of files to read as tic peak lists.")
    private List<String> filesToRead = Collections.emptyList();
    @Configurable(description = "The scan index offset. Value is added to peak "
            + "index from file. E.g. if scan index is 1 in the file and "
            + "scanIndexOffset=-1, the resulting scan index will be 0.")
    private int scanIndexOffset = 0;
    @Configurable(value = "SCAN", description = "Value of the scan index column"
            + " name in the csv peak file.")
    private String scanIndexColumnName = "SCAN";
    @Configurable(value = "var.tic_peaks")
    private String ticPeakVarName = "tic_peaks";

    /**
     * {@inheritDoc}
     */
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
    /**
     * {@inheritDoc}
     */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> t) {
        TupleND<IFileFragment> retf = new TupleND<>();
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
                    IFileFragment work = new FileFragment(new File(
                            getWorkflow().getOutputDirectory(this),
                            ff.getName()));
                    work.addSourceFile(ff);
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
                        log.warn(e.getLocalizedMessage());
                    }

                }
            }

        }
        return retf;
    }
}
