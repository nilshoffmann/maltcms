/*
 * $license$
 *
 * $Id$
 */

package cross;

import java.io.Serializable;
import org.apache.commons.configuration.Configuration;

/**
 * Interface for objects which are configurable.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IConfigurable extends Serializable {

	public void configure(Configuration cfg);

}
