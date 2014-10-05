# DenseArrayProducer
Class: maltcms.commands.fragments.preprocessing.DenseArrayProducer  
Description: Creates a binned representation of a chromatogram.  
Workflow Slot: GENERAL_PREPROCESSING  

---

## Variables
###Required
var.mass_values  
var.intensity_values  
var.scan_index  
var.scan_acquisition_time  
var.total_intensity  

###Required (optional)

###Provided
var.binned_mass_values  
var.binned_intensity_values  
var.binned_scan_index  


---

## Configurable Properties
Name: normalizeScans  
Default Value: false  
Description:   
  
Name: maskedMasses  
Default Value: null  
Description:   
  
Name: invertMaskedMasses  
Default Value: false  
Description:   
  
Name: ignoreMinMaxMassArrays  
Default Value: false  
Description:   
  
Name: normalizeMeanVariance  
Default Value: false  
Description:   
  
Name: normalizeEicsToUnity  
Default Value: false  
Description:   
  
Name: massBinResolution  
Default Value: 1.0  
Description:   
  

