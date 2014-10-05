# Chromatogram2DVisualizer
Class: maltcms.commands.fragments2d.visualization.Chromatogram2DVisualizer  
Description: 2D chromatogram visualization  
Workflow Slot: VISUALIZATION  

---

## Variables
###Required
var.total_intensity  
var.scan_rate  
var.modulation_time  
var.second_column_scan_index  
var.second_column_time  
var.scan_acquisition_time  

###Required (optional)
var.v_total_intensity  

###Provided
var.meanms_1d_vertical  
var.meanms_1d_vertical_index  


---

## Configurable Properties
Name: colorramp  
Default Value: res/colorRamps/bcgyr.csv  
Description:   
  
Name: filetype  
Default Value: png  
Description:   
  
Name: fillValueDouble  
Default Value: 9.9692099683868690e+36d  
Description:   
  
Name: thresholdLow  
Default Value: 0  
Description:   
  
Name: substractMean  
Default Value: false  
Description:   
  

