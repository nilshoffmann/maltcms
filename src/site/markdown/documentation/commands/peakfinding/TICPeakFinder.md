<h1>TICPeakFinder</h1>
Class: maltcms.commands.fragments.peakfinding.TICPeakFinder
Description: Finds peaks based on total ion current (TIC), using a simple extremum search within a window, combined with a signal-to-noise parameter to select peaks.
Workflow Slot: PEAKFINDING

---

<h2>Variables</h2>
<h3>Required</h3>
	var.total_intensity

<h3>Required (optional)</h3>
	var.scan_acquisition_time

<h3>Provided</h3>
	var.tic_peaks
	var.tic_filtered
	andichrom.var.peak_name
	andichrom.dimension.peak_number
	andichrom.var.peak_retention_time
	andichrom.var.peak_start_time
	andichrom.var.peak_end_time
	andichrom.var.peak_area
	andichrom.var.baseline_start_time
	andichrom.var.baseline_stop_time
	andichrom.var.baseline_start_value
	andichrom.var.baseline_stop_value


---

<h2>Configurable Properties</h2>
Name: peakThreshold
Default Value: 0.01
Description: 

Name: saveGraphics
Default Value: false
Description: 

Name: integratePeaks
Default Value: false
Description: 

Name: integrateTICPeaks
Default Value: true
Description: 

Name: snrWindow
Default Value: 50
Description: 

Name: ticPeakVarName
Default Value: tic_peaks
Description: 

Name: ticFilteredVarName
Default Value: tic_filtered
Description: 

Name: integrateRawTic
Default Value: true
Description: 

Name: peakSeparationWindow
Default Value: 10
Description: 

Name: removeOverlappingPeaks
Default Value: true
Description: 

Name: subtractBaseline
Default Value: false
Description: 

Name: baselineEstimator
Default Value: LoessMinimaBaselineEstimator(bandwidth=0.3, accuracy=1.0E-12, robustnessIterations=2, minimaWindow=100)
Description: 

Name: filter
Default Value: [MultiplicationFilter(factor=1.0)]
Description: 

Name: peakNormalizers
Default Value: []
Description: 


