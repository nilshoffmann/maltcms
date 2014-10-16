<h1>CwtPeakFinder</h1>
**Class**: `maltcms.commands.fragments2d.peakfinding.cwt.CwtPeakFinder`  
**Workflow Slot**: PEAKFINDING  
**Description**: Finds peak locations in intensity profiles using the continuous wavelet transform.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.modulation_time
	var.scan_rate

<h3>Optional</h3>

<h3>Provided</h3>
	var.peak_index_list


---

<h2>Configurable Properties</h2>
**Name**: `maxNeighbors`  
**Default Value**: `15`  
**Description**:  
The maxmimum number of neighbors expected in the given radius.  

---

**Name**: `maxRidges`  
**Default Value**: `5000`  
**Description**:  
The maximum number of ridges to report. Actual number of ridges reported may be lower, depending on the other parameters.  

---

**Name**: `maxScale`  
**Default Value**: `20`  
**Description**:  
The maximum scale to calculate the Continuous Wavelet Transform for.  

---

**Name**: `minPercentile`  
**Default Value**: `95`  
**Description**:  
Percentile of the intensity value distribution to use as minimum intensity for peaks.  

---

**Name**: `minScale`  
**Default Value**: `5`  
**Description**:  
The minimum required scale for a ridge.  

---

**Name**: `radius`  
**Default Value**: `10.0d`  
**Description**:  
The maximum radius around a peak to search for neighboring peaks.  

---

**Name**: `saveQuadTreeImage`  
**Default Value**: `false`  
**Description**:  
If true, the quad tree image will be saved.  

---

**Name**: `saveRidgeOverlayImages`  
**Default Value**: `false`  
**Description**:  
If true, the 2D TIC ridge overlay images will be saved, before and after filtering.  

---

**Name**: `saveScaleogramImage`  
**Default Value**: `false`  
**Description**:  
If true, the scaleogram image will be saved.  

---


