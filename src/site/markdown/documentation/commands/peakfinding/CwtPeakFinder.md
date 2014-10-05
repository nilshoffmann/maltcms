<h1>CwtPeakFinder</h1>
Class: maltcms.commands.fragments2d.peakfinding.cwt.CwtPeakFinder
Description: Finds peak locations in intensity profiles using the continuous wavelet transform.
Workflow Slot: PEAKFINDING

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Required (optional)</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
Name: minScale
Default Value: 5
Description: 
The minimum required scale for a ridge.
Name: maxScale
Default Value: 20
Description: 
The maximum scale to calculate the Continuous Wavelet Transform for.
Name: radius
Default Value: 10.0d
Description: 
The maximum radius around a peak to search for neighboring peaks.
Name: maxNeighbors
Default Value: 15
Description: 
The maxmimum number of neighbors expected in the given radius.
Name: saveScaleogramImage
Default Value: false
Description: 
Whether the scaleogram image should be saved.
Name: saveQuadTreeImage
Default Value: false
Description: 
Whether the quad tree image should be saved.
Name: saveRidgeOverlayImages
Default Value: false
Description: 
Whether the 2D TIC ridge overlay images should be saved, before and after filtering.
Name: maxRidges
Default Value: 5000
Description: 
The maximum number of ridges to report. Actual number of ridges reported may be lower, depending on the other parameters.
Name: minPercentile
Default Value: 95
Description: 
Percentile of the intensity value distribution to use as minimum intensity for peaks.

