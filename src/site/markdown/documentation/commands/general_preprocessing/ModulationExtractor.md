<h1>ModulationExtractor</h1>
Class: maltcms.commands.fragments2d.preprocessing.ModulationExtractor
Description: Allows definition of a start and end modulation period to be extracted from a raw GCxGC-MS chromatogram.
Workflow Slot: GENERAL_PREPROCESSING

---

<h2>Variables</h2>
<h3>Required</h3>
var.modulation_time
var.scan_rate

<h3>Required (optional)</h3>

<h3>Provided</h3>
var.scan_index
var.scan_acquisition_time
var.mass_values
var.intensity_values
var.total_intensity
var.scan_rate
var.modulation_time


---

<h2>Configurable Properties</h2>
Name: startModulation
Default Value: -1
Description: 

Name: endModulation
Default Value: -1
Description: 


