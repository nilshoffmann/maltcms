/*
 * $license$
 *
 * $Id$
 */
package maltcms.experimental.bipace.datastructures.api;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

/**
 *
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 */
@Data
public class Result<T extends Serializable> implements Serializable{
    
    private T result;
    
    private UUID producerId;
    
}
