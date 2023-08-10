<h1>DenseArrayProducer</h1>

**Class**: `maltcms.commands.fragments.preprocessing.DenseArrayProducer`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: Creates a binned representation of a chromatogram.  

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
**Name**: `ignoreMinMaxMassArrays`  
**Default Value**: `false`  
**Description**:  
If true, ignore values in variables mass_range_min and mass_range_max. Will then determine mass ranges from the actual mass_values.  

---

**Name**: `invertMaskedMasses`  
**Default Value**: `false`  
**Description**:  
If true, invert the logic of masked masses, effectively only selecting intensities associated to the masked masses.  

---

**Name**: `maskedMasses`  
**Default Value**: `null`  
**Description**:  
List of masses that should be masked, removing their associated intensities from each scan they occur in.  

---

**Name**: `massBinResolution`  
**Default Value**: `1.0`  
**Description**:  
Mass resolution to use for generation of profile EICs. 1.0 means nominal mass accuracy. 10.0 results in ten times higher resolution, up to the first decimal point. High values may significantly increase both memory usage and runtime.  

---

**Name**: `normalizeEicsToUnity`  
**Default Value**: `false`  
**Description**:  
If true, normalizes all EIC intensities between 0 and 1,before other normalizations are applied. Mutually exclusive with normalizeMeanVariance.  

---

**Name**: `normalizeMeanVariance`  
**Default Value**: `false`  
**Description**:  
If true, normalize intensities per EIC, based on that EIC's mean intensity and variance. If false,normalized intensities to length one for each scan. Mutually exclusive with normalizeEicsToUnity.  

---

**Name**: `normalizeScans`  
**Default Value**: `false`  
**Description**:  
If true, scans will be normalized.  

---


