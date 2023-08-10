<h1>ChromatogramVisualizer</h1>

**Class**: `maltcms.commands.fragments.visualization.ChromatogramVisualizer`  
**Workflow Slot**: VISUALIZATION  
**Description**: Creates two-dimensional heat map plots of chromatograms.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.scan_acquisition_time
	var.mass_values
	var.binned_intensity_values
	var.binned_scan_index

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `colorrampLocation`  
**Default Value**: `res/colorRamps/bcgyr.csv`  
**Description**:  
The location of the color ramp to use for plots.  

---

**Name**: `format`  
**Default Value**: `png`  
**Description**:  
The file format to save plots in.  

---

**Name**: `lowThreshold`  
**Default Value**: `0.0`  
**Description**:  
The minimum intensity value to include in plots.  

---

**Name**: `sampleSize`  
**Default Value**: `1024`  
**Description**:  
The number of color samples to use.  

---

**Name**: `substractStartTime`  
**Default Value**: `false`  
**Description**:  
If true, subtract start time.  

---

**Name**: `timeUnit`  
**Default Value**: `min`  
**Description**:  
The time unit used for plotting.  

---


