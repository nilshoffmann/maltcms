# DTW2DTicVisualizer
Class: maltcms.commands.fragments2d.warp.DTW2DTicVisualizer  
Description: Creates an image containing both chromatograms in different color channels of the image using the warp path. Optionaly it will create a serialized XYBPlot  
Workflow Slot: VISUALIZATION  

---

## Variables
###Required
var.scan_acquisition_time_1d  
var.modulation_time  
var.scan_rate  
var.second_column_time  
var.second_column_scan_index  
var.total_intensity  

###Required (optional)
var.v_total_intensity  

###Provided


---

## Configurable Properties
Name: serializeJFreeChart  
Default Value: true  
Description:   
  
Name: filetype  
Default Value: png  
Description:   
  
Name: visualizerClass  
Default Value: maltcms.commands.fragments2d.warp.visualization.Default2DTWVisualizer  
Description:   
  

