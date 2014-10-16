<h1>PairwiseAlignmentVisualizer</h1>
**Class**: `maltcms.commands.fragments.visualization.PairwiseAlignmentVisualizer`  
**Workflow Slot**: VISUALIZATION  
**Description**: Creates different plots for pairwise alignments of chromatograms.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.total_intensity
	var.scan_acquisition_time
	var.pairwise_distance_matrix
	var.pairwise_distance_alignment_names

<h3>Optional</h3>
	var.anchors.retention_index_names
	var.anchors.retention_scans

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `chromheight`  
**Default Value**: `100`  
**Description**:  
Height in pixels of the plotted chromatogram profiles.  

---

**Name**: `createComparativeTICChart`  
**Default Value**: `true`  
**Description**:  
If true, create a comparative pairwise chart for alignments.  

---

**Name**: `createDifferentialTICChart`  
**Default Value**: `true`  
**Description**:  
If true, create a differential intensity chart of aligned chromatograms.  

---

**Name**: `createMapTICChart`  
**Default Value**: `true`  
**Description**:  
If true, create a pairwise chart showing the alignment map.  

---

**Name**: `createRatioTICChart`  
**Default Value**: `true`  
**Description**:  
If true, create a ratio intenstiy chart of aligned chromatograms.  

---

**Name**: `createSuperimposedTICChart`  
**Default Value**: `true`  
**Description**:  
If true, create a superimposed chart of aligned chromatograms.  

---

**Name**: `mapheight`  
**Default Value**: `100`  
**Description**:  
Height in pixels of the pairwise alignment map.  

---

**Name**: `normalize`  
**Default Value**: `false`  
**Description**:  
Deprecated  

---

**Name**: `normalize_global`  
**Default Value**: `false`  
**Description**:  
Deprecated  

---

**Name**: `pairsWithFirstElement`  
**Default Value**: `false`  
**Description**:  
If true, plot pairs with first element only  

---

**Name**: `showChromatogramHeatmap`  
**Default Value**: `false`  
**Description**:  
Deprecated  

---

**Name**: `substract_start_time`  
**Default Value**: `true`  
**Description**:  
If true, subtract chromatogram start times.  

---

**Name**: `timeUnit`  
**Default Value**: `min`  
**Description**:  
The time unit used for plotting.  

---

**Name**: `y_axis_label`  
**Default Value**: `TIC`  
**Description**:  
The y axis label on the plots.  

---


