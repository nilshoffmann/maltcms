<h1>TICPeakListImporter</h1>

**Class**: `maltcms.commands.fragments.io.TICPeakListImporter`  
**Workflow Slot**: FILEIO  
**Description**: Imports tic peak data from tab separated value (tsv) files with column header  

---

<h2>Variables</h2>
<h3>Required</h3>

<h3>Optional</h3>

<h3>Provided</h3>


---

<h2>Configurable Properties</h2>
**Name**: `filesToRead`  
**Default Value**: `[]`  
**Description**:  
The list of files to read as tic peak lists.  

---

**Name**: `scanIndexColumnName`  
**Default Value**: `SCAN`  
**Description**:  
Value of the scan index column name in the csv peak file.  

---

**Name**: `scanIndexOffset`  
**Default Value**: `0`  
**Description**:  
The scan index offset. Value is added to peak index from file. E.g. if scan index is 1 in the file and scanIndexOffset=-1, the resulting scan index will be 0.  

---

**Name**: `ticPeakVarName`  
**Default Value**: `var.tic_peaks`  
**Description**:  
  

---


