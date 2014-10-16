<h1>PeakCliqueAlignment</h1>
**Class**: `maltcms.commands.fragments.alignment.PeakCliqueAlignment`  
**Workflow Slot**: ALIGNMENT  
**Description**: Assigns peak candidates as pairs and groups them into cliques of size k  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.scan_acquisition_time
	var.mass_values
	var.intensity_values
	var.scan_index

<h3>Optional</h3>
	var.binned_mass_values
	var.binned_intensity_values
	var.binned_scan_index
	var.tic_peaks
	var.eic_peaks
	var.first_column_elution_time
	var.second_column_elution_time
	var.peak_area
	var.peak_index_list
	var.anchors.retention_scans
	var.anchors.retention_index_names

<h3>Provided</h3>
	var.anchors.retention_index_names
	var.anchors.retention_times
	var.anchors.retention_indices
	var.anchors.retention_scans


---

<h2>Configurable Properties</h2>
**Name**: `maxBBHErrors`  
**Default Value**: `0`  
**Description**:  
Deprecated. Parameter will be ignored. Use minBbhFraction instead.  

---

**Name**: `minBbhFraction`  
**Default Value**: `1.0`  
**Description**:  
Fraction of peaks in a potential cluster required to be considered valid. 1.0 means, that all peaks must be bidirectional-best hits of each other. The clusters then correspond to true cliques. Lower values will allow incomplete bidirectional-best hits. Use lower values, if the group membership criterion is too strict and leads to exclusion of too many peaks. Inspect output for number of incompatible peaks, to observe the effect. May increase the number of false positives.  

---

**Name**: `minCliqueSize`  
**Default Value**: `-1`  
**Description**:  
Minimum clique size parameter. Controls the minimum number of peaks in a clique to be reported by the alignment. If set to -1, only complete cliques will be reported.  

---

**Name**: `peakFactory`  
**Default Value**: `Peak1DMSFactory(massesVar=mass_values, scanIndexVar=scan_index, intensitiesVar=intensity_values, binnedScanIndexVar=binned_scan_index, binnedMassesVar=binned_mass_values, binnedIntensitiesVar=binned_intensity_values, scanAcquisitionTimeVar=scan_acquisition_time)`  
**Description**:  
Use a custom peak factory, use Peak1DFactory for 1D data without MS, Peak1DMSFactory for 1D data with MS, or Peak2DMSFactory for 2D data with MS.  

---

**Name**: `postProcessCliques`  
**Default Value**: `false`  
**Description**:  
Deprecated. Use minBbhFraction instead.  

---

**Name**: `rtNormalizationFactor`  
**Default Value**: `0.016666666666666666`  
**Description**:  
Sets the retention time normalization factor for multiple peak alignment output of retention times. The factor is multiplied with the native retention time of each peak.  

---

**Name**: `rtOutputFormat`  
**Default Value**: `0.000`  
**Description**:  
Output format for retention times. Default has three trailing decimal places: "0.000" .  

---

**Name**: `saveIncompatiblePeaks`  
**Default Value**: `false`  
**Description**:  
If true, save incompatible peaks in msp compatible format.  

---

**Name**: `savePeakMatchAreaPercentTable`  
**Default Value**: `true`  
**Description**:  
If true, stores multiple alignment file with percentage of peak areas.  

---

**Name**: `savePeakMatchAreaTable`  
**Default Value**: `true`  
**Description**:  
If true, stores multiple alignment file with peak areas.  

---

**Name**: `savePeakMatchRTTable`  
**Default Value**: `true`  
**Description**:  
If true, stores multiple alignment file with 1D peak retention times.  

---

**Name**: `savePeakSimilarities`  
**Default Value**: `false`  
**Description**:  
If true, stores peak similarities for every pairwise similarity calculation. May create a large number of output files (quadratic in number of chromatograms).  

---

**Name**: `savePlots`  
**Default Value**: `false`  
**Description**:  
If true, save plots of cliques and of One-Way Anova and F-Test results.  

---

**Name**: `saveUnassignedPeaks`  
**Default Value**: `false`  
**Description**:  
If true, save unassigned peaks in msp compatible format.  

---

**Name**: `saveUnmatchedPeaks`  
**Default Value**: `false`  
**Description**:  
If true, save unmatched peaks in msp compatible format.  

---

**Name**: `saveXMLAlignment`  
**Default Value**: `true`  
**Description**:  
If true, stores the multiple alignment in Maltcms XML alignment format.  

---

**Name**: `use2DRetentionTimes`  
**Default Value**: `false`  
**Description**:  
Deprecated, use a Peak2DMSFactory to use 2D retention times for peaks  

---

**Name**: `useSparseArrays`  
**Default Value**: `false`  
**Description**:  
If true, use sparse mass spectra instead of binned ones. May reduce memory footprint, but will increase runtime.  

---

**Name**: `useUserSuppliedAnchors`  
**Default Value**: `false`  
**Description**:  
If true, will check for user supplied anchors and use them to augment other peak data, as provided by e.g. tic_peaks, eic_peaks, or peak_index_list.Requires upstream command to provide var.anchors.retention_scans and var.anchors.retention_names.If false, no user-supplied anchors will be used.  

---

**Name**: `workerFactory`  
**Default Value**: `WorkerFactory(maxRTDifference=60.0, similarityFunction=ProductSimilarity(scalarSimilarities=[GaussianDifferenceSimilarity(tolerance=5.0, threshold=0.0)], arraySimilarities=[ArrayCorr{}]), assumeSymmetricSimilarity=false, savePeakSimilarities=false)`  
**Description**:  
Use a custom worker factory, use WorkerFactory for 1D data, Worker2DFactory for 2D data.  

---


