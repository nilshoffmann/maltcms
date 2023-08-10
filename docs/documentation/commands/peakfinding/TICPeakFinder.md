<h1>TICPeakFinder</h1>

**Class**: `maltcms.commands.fragments.peakfinding.TICPeakFinder`  
**Workflow Slot**: PEAKFINDING  
**Description**: Finds peaks based on total ion current (TIC), using a simple extremum search within a window, combined with a signal-to-noise parameter to select peaks.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.total_intensity

<h3>Optional</h3>
	var.scan_acquisition_time

<h3>Provided</h3>
	var.tic_peaks
	var.tic_filtered
	var.peak_name
	var.peak_retention_time
	var.peak_start_time
	var.peak_end_time
	var.peak_area
	var.baseline_start_time
	var.baseline_stop_time
	var.baseline_start_value
	var.baseline_stop_value


---

<h2>Configurable Properties</h2>
**Name**: `baselineEstimator`  
**Default Value**: `LoessMinimaBaselineEstimator(bandwidth=0.3, accuracy=1.0E-12, robustnessIterations=2, minimaWindow=100)`  
**Description**:  
The baseline estimator to use.  

---

**Name**: `filter`  
**Default Value**: `[MultiplicationFilter(factor=1.0)]`  
**Description**:  
The filters to use for smoothing and filtering of the tic.  

---

**Name**: `integratePeaks`  
**Default Value**: `false`  
**Description**:  
If true, peak areas will be integrated.  

---

**Name**: `integrateRawTic`  
**Default Value**: `true`  
**Description**:  
If true, the raw tic will be used for integration. If false, the smoothed and filtered tic will be used.  

---

**Name**: `integrateTICPeaks`  
**Default Value**: `true`  
**Description**:  
If true, the tic will be used to integrate peaks.  

---

**Name**: `peakNormalizers`  
**Default Value**: `[]`  
**Description**:  
A list of peak normalizers. Each normalizer is invoked and its result multiplied to the intermediate result with the original peak's area.  

---

**Name**: `peakSeparationWindow`  
**Default Value**: `10`  
**Description**:  
The minimum number of scans between two peak apices.The second peak will be omitted, if it is closer to the first peak than allowed by the parameter.  

---

**Name**: `peakThreshold`  
**Default Value**: `0.01`  
**Description**:  
The minimal local signal-to-noise threshold required for a peak to be reported.  

---

**Name**: `removeOverlappingPeaks`  
**Default Value**: `true`  
**Description**:  
The removal of overlapping peaks is currently unavailable.  

---

**Name**: `saveGraphics`  
**Default Value**: `false`  
**Description**:  
If true, a plot of the local estimated signal-to-noise and of peak locations will be created.  

---

**Name**: `snrWindow`  
**Default Value**: `50`  
**Description**:  
Width of the sliding window for local snr estimation.   

---

**Name**: `subtractBaseline`  
**Default Value**: `false`  
**Description**:  
If true, the estimated baseline is subtracted from the smoothed and filtered tic.  

---

**Name**: `ticFilteredVarName`  
**Default Value**: `tic_filtered`  
**Description**:  
  

---

**Name**: `ticPeakVarName`  
**Default Value**: `tic_peaks`  
**Description**:  
  

---


