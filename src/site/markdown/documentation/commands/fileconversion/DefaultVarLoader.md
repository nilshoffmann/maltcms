<h1>DefaultVarLoader</h1>
**Class**: `maltcms.commands.fragments.preprocessing.DefaultVarLoader`  
**Workflow Slot**: FILECONVERSION  
**Description**: Loads default and additional variables as defined in the configuration.  

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Optional</h3>

<h3>Provided</h3>
	var.mass_values
	var.intensity_values
	var.scan_index
	var.scan_acquisition_time
	var.total_intensity


---

<h2>Configurable Properties</h2>
**Name**: `additionalVariables`  
**Default Value**: `[]`  
**Description**:  
A list of optional variable names to load. Currently, only non-namespaced variable names are supported, e.g. "total_intensity".  

---

**Name**: `defaultVariables`  
**Default Value**: `[]`  
**Description**:  
A list of required variable names to load. Currently, only non-namespaced variable names are supported, e.g. "total_intensity".  

---


