/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace;

import cross.datastructures.fragments.IFileFragment;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import maltcms.datastructures.peak.Peak;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class PeakList<T extends Peak> implements Serializable{
    
    private IFileFragment fragment;
    
    private List<T> peaks;
    
    private int index;
    
}
