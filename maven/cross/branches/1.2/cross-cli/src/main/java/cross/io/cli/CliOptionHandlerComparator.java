/*
 * $license$
 *
 * $Id$
 */

package cross.io.cli;

import java.util.Comparator;

/**
 *
 * @author nilshoffmann
 */
public class CliOptionHandlerComparator implements Comparator<ICliOptionHandler>{

    @Override
    public int compare(ICliOptionHandler t, ICliOptionHandler t1) {
        return t.getPriority()-t1.getPriority();
    }
    
}
