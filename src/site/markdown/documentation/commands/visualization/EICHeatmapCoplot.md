<h1>EICHeatmapCoplot</h1>
**Class**: `maltcms.commands.fragments.visualization.EICHeatmapCoplot`  
**Workflow Slot**: VISUALIZATION  
**Description**: Generates a stacked heatmap plot of EICs (bird's eye view) with shared time axis  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.total_intensity
	var.scan_acquisition_time
	var.total_intensity
	var.scan_acquisition_time

<h3>Optional</h3>
	var.anchors.retention_index_names
	var.anchors.retention_times
	var.anchors.retention_indices
	var.anchors.retention_scans
	var.anchors.retention_index_names
	var.anchors.retention_times
	var.anchors.retention_indices
	var.anchors.retention_scans

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>

**Description**:  
  

---

**Name**: `drawEICs`  
**Default Value**: `null`  
**Description**:  
A list of eics as doubles.  

---

**Name**: `eicBinSize`  
**Default Value**: `1.0`  
**Description**:  
The width of each eic. E.g. for eic=55, eicBinSize=1.0, the eic will have range [55,55+eicBinSize), where the latter bound is exclusive.  

---


