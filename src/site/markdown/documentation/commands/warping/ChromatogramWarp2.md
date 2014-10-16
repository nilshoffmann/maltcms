<h1>ChromatogramWarp2</h1>
**Class**: `maltcms.commands.fragments.warp.ChromatogramWarp2`  
**Workflow Slot**: WARPING  
**Description**: Warps Chromatograms to a given reference, according to alignment paths.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.multiple_alignment
	var.multiple_alignment_names
	var.multiple_alignment_type
	var.multiple_alignment_creator

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `alignmentLocation`  
**Default Value**: ``  
**Description**:  
If set, defines the location of an alignment file.  

---

**Name**: `averageCompressions`  
**Default Value**: `false`  
**Description**:  
If true, compressed nominal mass spectra will be averaged.  

---

**Name**: `indexVar`  
**Default Value**: `scan_index`  
**Description**:  
  

---

**Name**: `indexedVars`  
**Default Value**: `[mass_values, intensity_values]`  
**Description**:  
  

---

**Name**: `plainVars`  
**Default Value**: `[total_intensity, scan_acquisition_time]`  
**Description**:  
  

---


