<h1>CwtEicPeakFinder</h1>

**Class**: `maltcms.commands.fragments.peakfinding.CwtEicPeakFinder`  
**Workflow Slot**: PEAKFINDING  
**Description**: Finds EIC peaks using  Continuous Wavelet Transform.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.mass_values
	var.intensity_values
	var.scan_index

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `integratePeaks`  
**Default Value**: `true`  
**Description**:  
If true, peaks will be integrated in the original signal domain within the bounds as determined by the cwt.  

---

**Name**: `massResolution`  
**Default Value**: `1.0`  
**Description**:  
Mass resolution to use for generation of EICs. 1.0 means nominal mass accuracy. 10.0 results in ten times higher resolution, up to the first decimal point. High values maysignificantly increase both memory usage and runtime.  

---

**Name**: `maxScale`  
**Default Value**: `100`  
**Description**:  
Maximum cwt scale to calculate.  

---

**Name**: `minPercentile`  
**Default Value**: `5.0`  
**Description**:  
Deprecated. Is not used internally.  

---

**Name**: `minScale`  
**Default Value**: `10`  
**Description**:  
Minimum cwt scale to require a peak to reach.  

---

**Name**: `saveGraphics`  
**Default Value**: `false`  
**Description**:  
If true, save scaleogram details.  

---


