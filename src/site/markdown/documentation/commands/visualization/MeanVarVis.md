<h1>MeanVarVis</h1>
**Class**: `maltcms.commands.fragments2d.preprocessing.MeanVarVis`  
**Workflow Slot**: VISUALIZATION  
**Description**: Visualization of mean, variance, standard deviation and more.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.mean_ms_intensity
	var.var_ms_intensity
	var.sd_ms_intensity
	var.v_total_intensity_1d
	var.meanms_1d_horizontal_index
	var.meanms_1d_horizontal
	var.meanms_1d_vertical_index
	var.meanms_1d_vertical
	var.maxms_1d_horizontal_index
	var.maxms_1d_horizontal
	var.used_mass_values
	var.maxms_1d_vertical_index
	var.maxms_1d_vertical
	var.total_intensity_1d
	var.scan_acquisition_time_1d

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `colorramp`  
**Default Value**: `res/colorRamps/bcgyr.csv`  
**Description**:  
The color ramp location.  

---

**Name**: `thresholdLow`  
**Default Value**: `0`  
**Description**:  
Minimum intensity to be included in images.  

---

**Name**: `differentVisualizations`  
**Default Value**: `false`  
**Description**:  
If true, will create additional visualizations.  

---

**Name**: `useLogScale`  
**Default Value**: `false`  
**Description**:  
If true, will use a log scale for plotting of intensities.  

---

**Name**: `filetype`  
**Default Value**: `png`  
**Description**:  
The file format to use for saving plots.  

---


