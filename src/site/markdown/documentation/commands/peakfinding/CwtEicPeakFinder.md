<h1>CwtEicPeakFinder</h1>
Class: `maltcms.commands.fragments.peakfinding.CwtEicPeakFinder`
Description: Finds EIC peaks using  Continuous Wavelet Transform.
Workflow Slot: PEAKFINDING

---

<h2>Variables</h2>
<h3>Required</h3>
	var.binned_mass_values
	var.binned_intensity_values
	var.binned_scan_index

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
Name: `minScale`
Default Value: `10`
Description: 

Name: `maxScale`
Default Value: `100`
Description: 

Name: `minPercentile`
Default Value: `5.0`
Description: 

Name: `integratePeaks`
Default Value: `true`
Description: 

Name: `saveGraphics`
Default Value: `false`
Description: 

Name: `massResolution`
Default Value: `1.0`
Description: 


