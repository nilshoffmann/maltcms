<h1>MZMLExporter</h1>
Class: `maltcms.commands.fragments.io.MZMLExporter`
Description: Exports chromatographic and mass spectrometry data to mzML format.
Workflow Slot: FILECONVERSION

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
Name: `psiMsVersion`
Default Value: `3.60.0`
Description: 
The psi ms controlled vocabulary version to use.
Name: `unitOntologyVersion`
Default Value: `12:10:2011`
Description: 
The unit ontology controlled vocabulary version to use.
Name: `compressSpectra`
Default Value: `true`
Description: 
Whether spectral data should be zlib compressed or not.
Name: `compressChromatograms`
Default Value: `true`
Description: 
Whether chromatogram data should be zlib compressed or not.
Name: `validate`
Default Value: `false`
Description: 
Whether the result mzML file should be validated or not.
Name: `mzMLVersion`
Default Value: `1.1.0`
Description: 
The mzML version to use.
Name: `spectrumCacheSize`
Default Value: `2000`
Description: 
Maximum number of spectra to keep in memory during file creation.

