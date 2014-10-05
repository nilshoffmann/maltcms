<h1>PeakCliqueAlignment</h1>
Class: maltcms.commands.fragments.alignment.PeakCliqueAlignment
Description: Assigns peak candidates as pairs and groups them into cliques of size k
Workflow Slot: ALIGNMENT

---

<h2>Variables</h2>
<h3>Required</h3>
var.scan_acquisition_time
var.mass_values
var.intensity_values
var.scan_index

<h3>Required (optional)</h3>
var.binned_mass_values
var.binned_intensity_values
var.binned_scan_index
var.tic_peaks
var.eic_peaks
var.first_column_elution_time
var.second_column_elution_time
var.peak_area
var.peak_index_list

<h3>Provided</h3>
var.anchors.retention_index_names
var.anchors.retention_times
var.anchors.retention_indices
var.anchors.retention_scans


---

<h2>Configurable Properties</h2>
Name: useUserSuppliedAnchors
Default Value: false
Description: 

Name: minCliqueSize
Default Value: -1
Description: 

Name: savePeakSimilarities
Default Value: false
Description: 

Name: savePeakMatchRTTable
Default Value: true
Description: 

Name: savePeakMatchAreaTable
Default Value: true
Description: 

Name: savePeakMatchAreaPercentTable
Default Value: true
Description: 

Name: saveXMLAlignment
Default Value: true
Description: 

Name: maxBBHErrors
Default Value: 0
Description: 

Name: minBbhFraction
Default Value: 1.0
Description: 

Name: savePlots
Default Value: false
Description: 

Name: saveUnmatchedPeaks
Default Value: false
Description: 

Name: saveIncompatiblePeaks
Default Value: false
Description: 

Name: saveUnassignedPeaks
Default Value: false
Description: 

Name: useSparseArrays
Default Value: false
Description: 

Name: use2DRetentionTimes
Default Value: false
Description: 

Name: workerFactory
Default Value: WorkerFactory(maxRTDifference=60.0, similarityFunction=ProductSimilarity(scalarSimilarities=[GaussianDifferenceSimilarity(tolerance=5.0, threshold=0.0)], arraySimilarities=[ArrayCorr{}]), assumeSymmetricSimilarity=false, savePeakSimilarities=false)
Description: 

Name: peakFactory
Default Value: Peak1DMSFactory(massesVar=mass_values, scanIndexVar=scan_index, intensitiesVar=intensity_values, binnedScanIndexVar=binned_scan_index, binnedMassesVar=binned_mass_values, binnedIntensitiesVar=binned_intensity_values, scanAcquisitionTimeVar=scan_acquisition_time)
Description: 

Name: rtNormalizationFactor
Default Value: 0.016666666666666666
Description: 

Name: rtOutputFormat
Default Value: 0.000
Description: 

Name: postProcessCliques
Default Value: false
Description: 


