<h1>CenterStarAlignment</h1>

**Class**: `maltcms.commands.fragments.alignment.CenterStarAlignment`  
**Workflow Slot**: ALIGNMENT  
**Description**: Creates a multiple alignment by selecting a reference chromatogram based on highest overall similarity or lowest overall distance of reference to other chromatograms.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.minimizing_array_comp
	var.pairwise_distance_matrix
	var.pairwise_distance_names

<h3>Optional</h3>

<h3>Provided</h3>
	var.multiple_alignment
	var.multiple_alignment_names
	var.multiple_alignment_type
	var.multiple_alignment_creator


---

<h2>Configurable Properties</h2>
**Name**: `alignToFirst`  
**Default Value**: `false`  
**Description**:  
If true, align all chromatograms to the first one. If false, select reference automatically from pairwise similarities provided by upstream command.  

---

**Name**: `centerSequence`  
**Default Value**: ``  
**Description**:  
Base name (without file extension) of the chromatogram to use as the center to align to. Overrides automatic selection.  

---


