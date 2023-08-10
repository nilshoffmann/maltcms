<h1>Chromatogram2DVisualizer</h1>

**Class**: `maltcms.commands.fragments2d.visualization.Chromatogram2DVisualizer`  
**Workflow Slot**: VISUALIZATION  
**Description**: 2D chromatogram visualization  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.total_intensity
	var.scan_rate
	var.modulation_time
	var.second_column_scan_index
	var.second_column_time
	var.scan_acquisition_time

<h3>Optional</h3>
	var.v_total_intensity

<h3>Provided</h3>
	var.meanms_1d_vertical
	var.meanms_1d_vertical_index


---

<h2>Configurable Properties</h2>
**Name**: `colorramp`  
**Default Value**: `res/colorRamps/bcgyr.csv`  
**Description**:  
The location of the color ramp used for plotting.  

---

**Name**: `thresholdLow`  
**Default Value**: `0`  
**Description**:  
The minimum intensity value to be included in plots.  

---

**Name**: `substractMean`  
**Default Value**: `false`  
**Description**:  
If true, remove the mean intensity before plotting.  

---

**Name**: `filetype`  
**Default Value**: `png`  
**Description**:  
The file type used for saving plots.  

---

**Name**: `fillValueDouble`  
**Default Value**: `9.9692099683868690e+36d`  
**Description**:  
  

---


