<h1>MassFilter</h1>

**Class**: `maltcms.commands.fragments.preprocessing.MassFilter`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: Removes defined masses and associated intensities from chromatogram.  

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
**Name**: `epsilon`  
**Default Value**: `0.1`  
**Description**:  
The allowed deviation in m/z from the given excluded masses.  

---

**Name**: `excludeMasses`  
**Default Value**: `[]`  
**Description**:  
A list of (exact) masses to exclude.  

---

**Name**: `invert`  
**Default Value**: `false`  
**Description**:  
If true, inverts the meaning of excluded masses to include masses, to allow selective inclusion of EICs.  

---


