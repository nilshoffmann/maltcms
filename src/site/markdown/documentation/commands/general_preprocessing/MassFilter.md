<h1>MassFilter</h1>
Class: `maltcms.commands.fragments.preprocessing.MassFilter`
Description: Removes defined masses and associated intensities from chromatogram.
Workflow Slot: GENERAL_PREPROCESSING

---

<h2>Variables</h2>
<h3>Required</h3>
	var.mass_values
	var.intensity_values
	var.scan_index
	var.total_intensity

<h3>Optional</h3>
	var.excluded_masses

<h3>Provided</h3>
	var.mass_values
	var.intensity_values
	var.total_intensity


---

<h2>Configurable Properties</h2>
Name: `excludeMasses`
Default Value: `[]`
Description: 


Description: 

Name: `invert`
Default Value: `false`
Description: 


