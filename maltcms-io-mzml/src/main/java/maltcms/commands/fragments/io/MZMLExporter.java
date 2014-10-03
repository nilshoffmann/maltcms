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
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.WorkflowSlot;
import java.net.URI;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ICompletionService;
import org.openide.util.lookup.ServiceProvider;

/**
 * Exports chromatographic and mass spectrometric data from the common data
 * model representation to mzML.
 *
 * Supports 1D and 2D chromatography.
 *
 * @author Nils Hoffmann
 * 
 */
@Slf4j
@Data
@ServiceProvider(service = AFragmentCommand.class)
public class MZMLExporter extends AFragmentCommand {

    private final String description = "Exports chromatographic and mass spectrometry data to mzML format.";
    private final WorkflowSlot workflowSlot = WorkflowSlot.FILECONVERSION;

    @Configurable(description = "The psi ms controlled vocabulary version to use.")
    private String psiMsVersion = "3.60.0";
    @Configurable(description = "The unit ontology controlled vocabulary version to use.")
    private String unitOntologyVersion = "12:10:2011";
    @Configurable(description = "Whether spectral data should be zlib compressed or not.")
    private boolean compressSpectra = true;
    @Configurable(description = "Whether chromatogram data should be zlib compressed or not.")
    private boolean compressChromatograms = true;
    @Configurable(description = "Whether the result mzML file should be validated or not.")
    private boolean validate = false;
    @Configurable(description = "The mzML version to use.")
    private String mzMLVersion = "1.1.0";
    @Configurable(description = "Maximum number of spectra to keep in memory during file creation.")
    private int spectrumCacheSize = 2000;
    private String scanIndexVariable = "scan_index";
    private String massValuesVariable = "mass_values";
    private String intensityValuesVariable = "intensity_values";
    private String totalIntensityVariable = "total_intensity";
    private String scanAcquisitionTimeVariable = "scan_acquisition_time";
    private String firstColumnElutionTimeVariable = "first_column_elution_time";
    private String secondColumnElutionTimeVariable = "second_column_elution_time";
    private String msLevelVariable = "ms_level";

    /** {@inheritDoc} */
    @Override
    public TupleND<IFileFragment> apply(TupleND<IFileFragment> in) {
        log.info("Starting mzML export");
        ICompletionService<URI> ics = createCompletionService(URI.class);
        for (IFileFragment f : in) {
            log.info("Exporting " + f.getUri());
            MZMLExporterWorker worker = new MZMLExporterWorker();
            worker.setInputFile(f.getUri());
            worker.setOutputFile(createWorkFragment(f).getUri());
            worker.setCompressSpectra(compressSpectra);
            worker.setCompressChromatograms(compressChromatograms);
            worker.setIntensityValuesVariable(intensityValuesVariable);
            worker.setMassValuesVariable(massValuesVariable);
            worker.setScanAcquisitionTimeVariable(scanAcquisitionTimeVariable);
            worker.setScanIndexVariable(scanIndexVariable);
            worker.setTotalIntensityVariable(totalIntensityVariable);
            worker.setFirstColumnElutionTimeVariable(firstColumnElutionTimeVariable);
            worker.setSecondColumnElutionTimeVariable(secondColumnElutionTimeVariable);
            worker.setMsLevelVariable(msLevelVariable);
            worker.setSpectrumCacheSize(spectrumCacheSize);
            worker.setMzMLVersion(mzMLVersion);
            worker.setPsiMsVersion(psiMsVersion);
            worker.setUnitOntologyVersion(unitOntologyVersion);
            worker.setValidate(validate);
            ics.submit(worker);
        }
        log.info("Waiting for workers to return!");
        //processed files are stored by the worker, so no need to
        //call save() here.
        TupleND<IFileFragment> ret = postProcessUri(ics, in);
        return ret;
    }

}
