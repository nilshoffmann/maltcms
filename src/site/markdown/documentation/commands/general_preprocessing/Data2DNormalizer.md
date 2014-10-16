<h1>Data2DNormalizer</h1>
**Class**: `maltcms.commands.fragments2d.preprocessing.Data2DNormalizer`  
**Workflow Slot**: GENERAL_PREPROCESSING  
**Description**: Normalizes 2D / GCxGC-MS data  

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Optional</h3>

<h3>Provided</h3>
	var.total_intensity_filtered


---

<h2>Configurable Properties</h2>
**Name**: `applyMovingAverage`  
**Default Value**: `false`  
**Description**:  
If true, apply a moving average filter on the 2D TIC.  

---

**Name**: `applyMovingMedian`  
**Default Value**: `true`  
**Description**:  
If true, apply a moving median filter on the 2D TIC.  

---

**Name**: `applyTopHatFilter`  
**Default Value**: `true`  
**Description**:  
If true, apply a morphological top-hat filteron the 2D TIC.  

---

**Name**: `movingAverageWindow`  
**Default Value**: `3`  
**Description**:  
The width of the moving average window. Effective width is 2*w+1.  

---

**Name**: `movingMedianWindow`  
**Default Value**: `3`  
**Description**:  
The width of the moving median window. Effective width is 2*w+1.  

---

**Name**: `topHatFilterWindow`  
**Default Value**: `5`  
**Description**:  
The width of the top-hat filter window. Effective width is 2*w+1.  

---


