<h1>GcImagePeak2DImporter</h1>
**Class**: `maltcms.commands.fragments2d.io.csv.GcImagePeak2DImporter`  
**Workflow Slot**: FILECONVERSION  
**Description**: Imports 2D chromatography peak data from Gc Image csv Blob reports.  

---

<h2>Variables</h2>
<h3>Required</h3>
	var.scan_acquisition_time
	var.mass_values
	var.intensity_values
	var.scan_index

<h3>Optional</h3>

<h3>Provided</h3>
	var.peak_index_list


---

<h2>Configurable Properties</h2>
**Name**: `localeString`  
**Default Value**: `en_US`  
**Description**:  
The locale to use for parsing of numbers.  

---

**Name**: `quotationCharacter`  
**Default Value**: `"`  
**Description**:  
The quotation character used for parsing of text.  

---

**Name**: `reportFiles`  
**Default Value**: `[]`  
**Description**:  
A list of report file paths. Report names must match the chromatogram names, without file extension.  

---


