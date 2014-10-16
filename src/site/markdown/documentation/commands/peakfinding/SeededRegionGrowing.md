<h1>SeededRegionGrowing</h1>
**Class**: `maltcms.commands.fragments2d.peakfinding.SeededRegionGrowing`  
**Workflow Slot**: PEAKFINDING  
**Description**: Will do an initial peak finding and computes the 'snakes'  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.total_intensity
	var.scan_rate
	var.modulation_time
	var.second_column_scan_index
	var.first_column_elution_time
	var.second_column_elution_time

<h3>Optional</h3>
	var.v_total_intensity
	var.tic_peaks

<h3>Provided</h3>
	var.peak_index_list
	var.region_index_list
	var.region_peak_index
	var.boundary_index_list
	var.boundary_peak_index
	var.peak_mass_intensity


---

<h2>Configurable Properties</h2>
**Name**: `colorramp`  
**Default Value**: `res/colorRamps/bcgyr.csv`  
**Description**:  
The location of the color ramp to use  

---

**Name**: `thresholdLow`  
**Default Value**: `0`  
**Description**:  
The minimum intensity value to include in images.  

---

**Name**: `doIntegration`  
**Default Value**: `false`  
**Description**:  
If true,peaks will be integrated.  

---

**Name**: `doNormalization`  
**Default Value**: `false`  
**Description**:  
Deprecated.  

---

**Name**: `integration`  
**Default Value**: `null`  
**Description**:  
The peak integration implementation to use.  

---

**Name**: `peakExporter`  
**Default Value**: `null`  
**Description**:  
The peak exporter implementation to use.  

---

**Name**: `peakPicking`  
**Default Value**: `null`  
**Description**:  
The peak picking implementation to use. Use SimplePeakPicking for standalone operation, or TicPeakPicking, if another peak finder, such as CWTPeakFinder has already detected peaks.  

---

**Name**: `peakSeparator`  
**Default Value**: `PeakSeparator(minDist=0.995, separationSimilarity=null, similarity=null, useMeanMsForSeparation=false, rt1=null, rt2=null)`  
**Description**:  
The peak separator implementation to use.  

---

**Name**: `regionGrowing`  
**Default Value**: `null`  
**Description**:  
The region growing implementation to use.  

---

**Name**: `separate`  
**Default Value**: `true`  
**Description**:  
  

---

**Name**: `filetype`  
**Default Value**: `png`  
**Description**:  
The format for saved plots.  

---

**Name**: `fillValueDouble`  
**Default Value**: `9.9692099683868690e+36d`  
**Description**:  
  

---


