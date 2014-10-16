<h1>Default2DVarLoader</h1>
**Class**: `maltcms.commands.fragments2d.preprocessing.Default2DVarLoader`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: Default var loader for 2d data. Will create different variables.  

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Optional</h3>

<h3>Provided</h3>
	var.total_intensity
	var.modulation_time
	var.scan_rate
	var.scan_duration
	var.second_column_time
	var.second_column_scan_index
	var.total_intensity_1d
	var.scan_acquisition_time
	var.scan_acquisition_time_1d
	var.total_intensity_2d
	var.first_column_elution_time
	var.second_column_elution_time


---

<h2>Configurable Properties</h2>
**Name**: `estimateModulationTime`  
**Default Value**: `false`  
**Description**:  
(Experimental) If true, estimate modulation time from 1D TIC.  

---

**Name**: `maxScanAcquisitionTimeDelta`  
**Default Value**: `1.0E-4`  
**Description**:  
Maximum allowed difference in scan acquisition times for scan rate estimation.  

---


