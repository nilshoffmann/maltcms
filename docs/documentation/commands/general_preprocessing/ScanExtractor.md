<h1>ScanExtractor</h1>

**Class**: `maltcms.commands.fragments.preprocessing.ScanExtractor`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: Allows definition of a start and end modulation period to be extracted from a raw chromatogram.  

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Optional</h3>

<h3>Provided</h3>
	var.scan_index
	var.scan_acquisition_time
	var.mass_values
	var.intensity_values
	var.total_intensity


---

<h2>Configurable Properties</h2>
**Name**: `endScan`  
**Default Value**: `-1`  
**Description**:  
The end index to include. A value of -1 will automtically select the last available index.  

---

**Name**: `endTime`  
**Default Value**: `+Inf`  
**Description**:  
The end time to include in scan extraction. If value is different from +Inf, endScan will be used instead.  

---

**Name**: `startScan`  
**Default Value**: `-1`  
**Description**:  
The start index to include. A value of -1 means to select the first start scan automatically.  

---

**Name**: `startTime`  
**Default Value**: `-Inf`  
**Description**:  
The start time to include in scan extraction. If value is different from -Inf, startScan will be used instead.  

---


