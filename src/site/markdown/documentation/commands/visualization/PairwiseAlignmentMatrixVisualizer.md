# PairwiseAlignmentMatrixVisualizer
Class: maltcms.commands.fragments.visualization.PairwiseAlignmentMatrixVisualizer  
Description: Creates a plot of the pairwise distance and alignment matrices created during alignment.  
Workflow Slot: VISUALIZATION  

---

## Variables
###Required
var.total_intensity  
var.pairwise_distance_matrix  
var.pairwise_distance_alignment_names  

###Required (optional)
var.anchors.retention_index_names  
var.anchors.retention_scans  

###Provided


---

## Configurable Properties
Name: format  
Default Value: png  
Description:   
  
Name: chromatogramHeight  
Default Value: 200  
Description:   
  
Name: left_chromatogram_var  
Default Value: total_intensity  
Description:   
  
Name: top_chromatogram_var  
Default Value: total_intensity  
Description:   
  
Name: path_i  
Default Value: null  
Description:   
  
Name: path_j  
Default Value: null  
Description:   
  
Name: full_spec  
Default Value: false  
Description:   
  
Name: colorramp_location  
Default Value: res/colorRamps/bw.csv  
Description:   
  
Name: sampleSize  
Default Value: 1024  
Description:   
  
Name: pairsWithFirstElement  
Default Value: false  
Description:   
  
Name: drawPath  
Default Value: true  
Description:   
  
Name: matrix_vars  
Default Value: [cumulative_distance, pairwise_distance]  
Description:   
  
Name: fontsize  
Default Value: 30  
Description:   
  

