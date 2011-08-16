/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.datastructures.peak.annotations;

import lombok.Data;

/**
 *
 * @author nils
 */
@Data
public class RetentionIndexPeakAnnotation extends PeakAnnotation {
    private double retentionIndex = Double.NaN;
}
