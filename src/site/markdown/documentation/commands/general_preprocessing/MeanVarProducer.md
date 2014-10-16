<h1>MeanVarProducer</h1>
**Class**: `maltcms.commands.fragments2d.preprocessing.MeanVarProducer`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: Produces mean and variance of a chromatogram  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.modulation_time
	var.scan_rate

<h3>Optional</h3>

<h3>Provided</h3>
	var.mean_ms_intensity
	var.var_ms_intensity
	var.sd_ms_intensity
	var.v_total_intensity
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


---

<h2>Configurable Properties</h2>
**Name**: `massResolution`  
**Default Value**: `1.0`  
**Description**:  
Mass resolution to use for generation of profile EICs. 1.0 means nominal mass accuracy. 10.0 results in ten times higher resolution, up to the first decimal point. High values may significantly increase both memory usage and runtime.  

---

**Name**: `minStandardDeviation`  
**Default Value**: `-1.0d`  
**Description**:  
The minimum standard deviation of the intensity values to use. This fixed value is used,if "minStandardDeviationQuantil" is 0.  

---

**Name**: `minStandardDeviationQuantil`  
**Default Value**: `0.01d`  
**Description**:  
The quantile of the minimum standard deviation of the intensity values to use. If value is > 0, the given quantile will be used to calculate the minimumstandard deviation.  

---


