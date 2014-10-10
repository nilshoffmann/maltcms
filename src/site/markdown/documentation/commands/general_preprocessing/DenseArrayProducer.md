<h1>DenseArrayProducer</h1>
Class: `maltcms.commands.fragments.preprocessing.DenseArrayProducer`
Description: Creates a binned representation of a chromatogram.
Workflow Slot: GENERAL_PREPROCESSING

---

<h2>Variables</h2>
<h3>Required</h3>
	var.mass_values
	var.intensity_values
	var.scan_index
	var.scan_acquisition_time
	var.total_intensity

<h3>Optional</h3>

<h3>Provided</h3>
	var.binned_mass_values
	var.binned_intensity_values
	var.binned_scan_index


---

<h2>Configurable Properties</h2>
Name: `normalizeScans`
Default Value: `false`
Description: 

Name: `maskedMasses`
Default Value: `null`
Description: 

Name: `invertMaskedMasses`
Default Value: `false`
Description: 

Name: `ignoreMinMaxMassArrays`
Default Value: `false`
Description: 

Name: `normalizeMeanVariance`
Default Value: `false`
Description: 

Name: `normalizeEicsToUnity`
Default Value: `false`
Description: 

Name: `massBinResolution`
Default Value: `1.0`
Description: 


