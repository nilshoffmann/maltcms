/*
 * $license$
 *
 * $Id$
 */

package cross.io.xml;

import org.jdom.Element;

/**
 * Interface allowing a Visitor like request to implementing objects to decorate
 * the passed in Element to their liking with their own xml structure.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public interface IXMLSerializable {

	public void appendXML(Element e);

}
