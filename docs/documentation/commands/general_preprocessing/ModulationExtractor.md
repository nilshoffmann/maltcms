<h1>ModulationExtractor</h1>

**Class**: `maltcms.commands.fragments2d.preprocessing.ModulationExtractor`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: Allows definition of a start and end modulation period to be extracted from a raw GCxGC-MS chromatogram.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.modulation_time
	var.scan_rate

<h3>Optional</h3>

<h3>Provided</h3>
	var.scan_index
	var.scan_acquisition_time
	var.mass_values
	var.intensity_values
	var.total_intensity
	var.mass_range_min
	var.mass_range_max


---

<h2>Configurable Properties</h2>
**Name**: `endModulation`  
**Default Value**: `-1`  
**Description**:  
The index of the last modulation to extract.  

---

**Name**: `startModulation`  
**Default Value**: `-1`  
**Description**:  
The index of the start modulation to extract.  

---


