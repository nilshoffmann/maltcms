<h1>DTW2DPeakAreaVisualizer</h1>
**Class**: `maltcms.commands.fragments2d.warp.DTW2DPeakAreaVisualizer`  
**Workflow Slot**: VISUALIZATION  
**Description**: Creates an image containing both chromatograms in different color channels of the image using the warp path. Optionaly it will create a serialized XYBPlot  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.scan_acquisition_time_1d
	var.modulation_time
	var.scan_rate
	var.second_column_time
	var.region_index_list
	var.boundary_index_list
	var.scan_acquisition_time_1d
	var.modulation_time
	var.scan_rate
	var.second_column_time
	var.second_column_scan_index
	var.total_intensity

<h3>Optional</h3>
	var.v_total_intensity
	var.v_total_intensity

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `fillPeakArea`  
**Default Value**: `false`  
**Description**:  
If true, fill the peak areas.  

---

**Name**: `visualizer`  
**Default Value**: `Default2DTWVisualizer(peakListVar=peak_index_list, holdi=true, holdj=true, globalmax=false, black=true, filter=true, normalize=true, threshold=6.0, horizontal=true, currentrasterline=-1, binSize=256)`  
**Description**:  
  

---


**Description**:  
  

---


**Description**:  
  

---


