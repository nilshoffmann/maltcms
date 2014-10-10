<h1>MeanVarProducer</h1>
Class: `maltcms.commands.fragments2d.preprocessing.MeanVarProducer`
Description: Produces mean and variance of a chromatogram
Workflow Slot: GENERAL_PREPROCESSING

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
	var.v_mass_values
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
Name: `minStandardDeviationQuantil`
Default Value: `0.01d`
Description: 

Name: `minStandardDeviation`
Default Value: `-1.0d`
Description: 


