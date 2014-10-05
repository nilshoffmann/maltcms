<h1>SeededRegionGrowing</h1>
Class: maltcms.commands.fragments2d.peakfinding.SeededRegionGrowing
Description: Will do an initial peak finding and computes the 'snakes'
Workflow Slot: PEAKFINDING

---

<h2>Variables</h2>
<h3>Required</h3>
var.total_intensity
var.scan_rate
var.modulation_time
var.second_column_scan_index
var.scan_acquisition_time_1d

<h3>Required (optional)</h3>
var.v_total_intensity
var.tic_peaks

<h3>Provided</h3>
var.peak_index_list
var.region_index_list
var.region_peak_index
var.boundary_index_list
var.boundary_peak_index
var.peak_mass_intensity


---

<h2>Configurable Properties</h2>
Name: filetype
Default Value: png
Description: 

Name: colorramp
Default Value: res/colorRamps/bcgyr.csv
Description: 

Name: fillValueDouble
Default Value: 9.9692099683868690e+36d
Description: 

Name: thresholdLow
Default Value: 0
Description: 

Name: separate
Default Value: true
Description: 

Name: doNormalization
Default Value: false
Description: 

Name: doIntegration
Default Value: false
Description: 

Name: peakPicking
Default Value: null
Description: 

Name: regionGrowing
Default Value: null
Description: 

Name: integration
Default Value: null
Description: 

Name: peakExporter
Default Value: null
Description: 

Name: peakSeparator
Default Value: PeakSeparator(minDist=0.995, separationSimilarity=null, similarity=null, useMeanMsForSeparation=false, rt1=null, rt2=null)
Description: 


