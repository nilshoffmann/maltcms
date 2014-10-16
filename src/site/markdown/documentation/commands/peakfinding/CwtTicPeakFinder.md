<h1>CwtTicPeakFinder</h1>
**Class**: `maltcms.commands.fragments.peakfinding.CwtTicPeakFinder`  
**Workflow Slot**: PEAKFINDING  
**Description**: Finds TIC peaks using Continuous Wavelet Transform.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.total_intensity

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `integratePeaks`  
**Default Value**: `true`  
**Description**:  
If true, peaks will be integrated in the original signal domain within the bounds as determined by the cwt.  

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

**Name**: `peakNormalizers`  
**Default Value**: `[]`  
**Description**:  
A list of peak normalizers. Each normalizer is invoked and its result multiplied to the intermediate result with the original peak's area.  

---

**Name**: `saveGraphics`  
**Default Value**: `false`  
**Description**:  
If true, save scaleogram details.  

---


