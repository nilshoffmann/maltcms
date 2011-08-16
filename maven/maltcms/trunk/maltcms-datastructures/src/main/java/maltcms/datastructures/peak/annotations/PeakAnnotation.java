/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.datastructures.peak.annotations;

import lombok.Data;
import maltcms.datastructures.ms.IMetabolite;

/**
 *
 * @author nils
 */
@Data
public class PeakAnnotation {
    
    private double score = Double.NaN;
    
    private String database = "";
    
    private IMetabolite metabolite;
    
}
