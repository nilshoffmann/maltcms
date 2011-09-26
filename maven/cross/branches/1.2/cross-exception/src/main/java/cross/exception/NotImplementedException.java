/*
 * 
 *
 * $Id$
 */

package cross.exception;

/**
 * Custom Exception to allow for a given message string.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class NotImplementedException extends RuntimeException {

	/**
     * 
     */
	private static final long serialVersionUID = 8040922236426369927L;

	public NotImplementedException() {
		this("This method has not yet been implemented!");
	}

	public NotImplementedException(final String arg0) {
		super(arg0);
	}

	public NotImplementedException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public NotImplementedException(final Throwable arg0) {
		super(arg0);
	}
}
