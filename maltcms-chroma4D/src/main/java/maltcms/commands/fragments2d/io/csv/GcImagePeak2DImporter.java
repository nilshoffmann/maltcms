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
package maltcms.commands.fragments2d.io.csv;

import cross.annotations.Configurable;
import cross.annotations.ProvidesVariables;
import cross.annotations.RequiresVariables;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import cross.exception.ConstraintViolationException;
import cross.tools.StringTools;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.datastructures.ms.Chromatogram1D;
import maltcms.datastructures.ms.IChromatogram1D;
import maltcms.datastructures.peak.Peak2D;
import maltcms.datastructures.peak.normalization.IPeakNormalizer;
import maltcms.io.csv.gcimage.GcImageBlobImporter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.LocaleUtils;
import org.openide.util.lookup.ServiceProvider;
import ucar.ma2.ArrayInt;
import ucar.ma2.IndexIterator;

/**
 *
 * Imports 2D chromatography peak data from Gc Image csv Blob reports.
 *
 * @author Nils Hoffmann
 *
 */
@RequiresVariables(names = {
    "var.scan_acquisition_time", "var.mass_values", "var.intensity_values",
    "var.scan_index"})
@ProvidesVariables(names = {"var.peak_index_list"})
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class GcImagePeak2DImporter extends AFragmentCommand {

    @Configurable(description="The locale to use for parsing of numbers.")
    private String localeString = Locale.US.toString();
    @Configurable(description="A list of report file paths. Report names must"
            + " match the chromatogram names, without file extension.")
    private List<String> reportFiles = Collections.emptyList();
    @Configurable(name = "var.peak_index_list", value = "peak_index_list")
    private String peakListVar = "peak_index_list";
    @Configurable(description="The quotation character used for parsing of text.")
    private String quotationCharacter = "\"";

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    private Map<IChromatogram1D, File> getChromatogramToReportFileMap(TupleND<IFileFragment> t, List<File> reportFiles) {
        Map<IChromatogram1D, File> map = new LinkedHashMap<>();
        Map<String, File> reportToFileMap = new LinkedHashMap<>();
        for (File reportFile : reportFiles) {
            String filename = FileTools.getFilename(reportFile.getName());
            String basename = StringTools.removeFileExt(filename);
            String ext = StringTools.getFileExtension(filename);
            if (ext.equals(filename)) {
                log.debug("Filename: {}", basename);
            } else {
                log.debug("Filename: {}", basename + "." + ext);
            }
            reportToFileMap.put(basename, reportFile);
        }
        for (IFileFragment f : t) {
            String basename = StringTools.removeFileExt(f.getName());
            File file = reportToFileMap.get(basename);
            if (file == null) {
                log.warn("Could not map report file: {} to file fragment {}!", file, f.getUri());
            } else {
                log.debug("Mapped report file: {} to file fragment {}!", file, f.getUri());
                map.put(new Chromatogram1D(f), file);
            }
        }
        return map;
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
    public TupleND<IFileFragment> apply(final TupleND<IFileFragment> t) {
        Locale locale = LocaleUtils.toLocale(localeString);
        List<File> location = new ArrayList<File>();
        if (reportFiles.size() == 1) {
            final String filename = reportFiles.get(0);
            if (filename.endsWith("*.csv")) {
                final File f = new File(filename);
                log.debug("Trying to load anchors from {}", f.
                        getAbsolutePath());
                if (f.isDirectory()) {
                    final File[] files = f.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(final File dir, final String name) {
                            if (name.equals("*.csv")) {
                                return true;
                            }
                            return false;
                        }
                    });
                    location = Arrays.asList(files);
                }
            } else {
                File reportFile = new File(filename);
                if (reportFile.isFile()) {
                    location.add(reportFile);
                } else {
                    log.warn("Report file {} is not a file!", reportFile);
                }
            }
        } else {
            location = new ArrayList<File>();
            for (String filename : reportFiles) {
                File reportFile = new File(filename);
                if (reportFile.isFile()) {
                    location.add(reportFile);
                } else {
                    log.warn("Report file {} is not a file!", reportFile);
                }
            }
        }
        if (location.isEmpty()) {
            throw new ConstraintViolationException("Could not initialize report files!");
        }
        GcImageBlobImporter gci = new GcImageBlobImporter(quotationCharacter, locale);
        final ArrayList<IFileFragment> ret = new ArrayList<>();
        Map<IChromatogram1D, File> chromToFileMap = getChromatogramToReportFileMap(t, location);
        if (chromToFileMap.keySet().size() != t.size()) {
            log.warn("Could only assign {} peak reports to input files!", chromToFileMap.keySet().size());
        }
        for (final IChromatogram1D iff : chromToFileMap.keySet()) {
            final IFileFragment fret = new FileFragment(
                    new File(getWorkflow().getOutputDirectory(this),
                            iff.getParent().getName()));
            List<Peak2D> peaks = gci.importPeaks(chromToFileMap.get(iff), iff);
            final ArrayInt.D1 peakindex = new ArrayInt.D1(peaks.size());
            final IndexIterator iter = peakindex.getIndexIterator();
            for (Peak2D p : peaks) {
                iter.setIntNext(p.getApexIndex());
            }
            final IVariableFragment var = new VariableFragment(fret,
                    this.peakListVar);
            var.setArray(peakindex);
            Peak2D.append2D(fret, new LinkedList<IPeakNormalizer>(), peaks, "tic_peaks");
            fret.save();
            ret.add(fret);
        }
        return new TupleND<>(ret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        super.configure(cfg);
    }

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
        return "Imports 2D chromatography peak data from Gc Image csv Blob reports.";
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
        return WorkflowSlot.FILECONVERSION;
    }
}
