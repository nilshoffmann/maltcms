<h1>GCGCToGCMSConverter</h1>

**Class**: `maltcms.commands.fragments2d.preprocessing.GCGCToGCMSConverter`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: GCxGC-MS data to GC-MS data by simple scan-wise summation.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.scan_acquisition_time
	var.total_intensity
	var.mass_values
	var.intensity_values
	var.scan_index
	var.mass_range_min
	var.mass_range_max
	var.modulation_time
	var.scan_rate

<h3>Optional</h3>

<h3>Provided</h3>
	var.scan_acquisition_time
	var.total_intensity
	var.mass_values
	var.intensity_values
	var.scan_index
	var.mass_range_min
	var.mass_range_max


---

<h2>Configurable Properties</h2>
**Name**: `snrthreshold`  
**Default Value**: `5`  
**Description**:  
The signal-to-noise threshold.Intensities below this value will be set to 0.  

---


