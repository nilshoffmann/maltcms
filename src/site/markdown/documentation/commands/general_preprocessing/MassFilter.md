# MassFilter
Class: maltcms.commands.fragments.preprocessing.MassFilter  
Description: Removes defined masses and associated intensities from chromatogram.  
Workflow Slot: GENERAL_PREPROCESSING  

---

## Variables
###Required
var.mass_values  
var.intensity_values  
var.scan_index  
var.total_intensity  

###Required (optional)
var.excluded_masses  

###Provided
var.mass_values  
var.intensity_values  
var.total_intensity  


---

## Configurable Properties
Name: excludeMasses  
Default Value: []  
Description:   
  

Description:   
  
Name: invert  
Default Value: false  
Description:   
  

