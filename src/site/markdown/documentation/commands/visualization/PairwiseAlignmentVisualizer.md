<h1>PairwiseAlignmentVisualizer</h1>
Class: maltcms.commands.fragments.visualization.PairwiseAlignmentVisualizer
Description: Creates different plots for pairwise alignments of chromatograms.
Workflow Slot: VISUALIZATION

---

<h2>Variables</h2>
<h3>Required</h3>
var.total_intensity
var.scan_acquisition_time
var.pairwise_distance_matrix
var.pairwise_distance_alignment_names

<h3>Required (optional)</h3>
var.anchors.retention_index_names
var.anchors.retention_scans

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
Name: normalize
Default Value: false
Description: 

Name: normalize_global
Default Value: false
Description: 

Name: mapheight
Default Value: 100
Description: 

Name: substract_start_time
Default Value: true
Description: 

Name: pairsWithFirstElement
Default Value: false
Description: 

Name: showChromatogramHeatmap
Default Value: false
Description: 

Name: chromheight
Default Value: 100
Description: 

Name: timeUnit
Default Value: min
Description: 

Name: createMapTICChart
Default Value: true
Description: 

Name: createComparativeTICChart
Default Value: true
Description: 

Name: createDifferentialTICChart
Default Value: true
Description: 

Name: createRatioTICChart
Default Value: true
Description: 

Name: createSuperimposedTICChart
Default Value: true
Description: 

Name: y_axis_label
Default Value: TIC
Description: 


