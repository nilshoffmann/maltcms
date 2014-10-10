<h1>DTW2DTicVisualizer</h1>
Class: `maltcms.commands.fragments2d.warp.DTW2DTicVisualizer`
Description: Creates an image containing both chromatograms in different color channels of the image using the warp path. Optionaly it will create a serialized XYBPlot
Workflow Slot: VISUALIZATION

---

<h2>Variables</h2>
<h3>Required</h3>
	var.scan_acquisition_time_1d
	var.modulation_time
	var.scan_rate
	var.second_column_time
	var.second_column_scan_index
	var.total_intensity

<h3>Optional</h3>
	var.v_total_intensity

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
Name: `serializeJFreeChart`
Default Value: `true`
Description: 

Name: `filetype`
Default Value: `png`
Description: 

Name: `visualizerClass`
Default Value: `maltcms.commands.fragments2d.warp.visualization.Default2DTWVisualizer`
Description: 


