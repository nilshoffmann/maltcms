/*
 * 
 *
 * $Id$
 */

package cross.exception;

public class ResourceNotAvailableException extends RuntimeException {

	/**
     * 
     */
	private static final long serialVersionUID = -277507898414704248L;

	public ResourceNotAvailableException(final String arg0) {
		super(arg0);
	}

	public ResourceNotAvailableException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public ResourceNotAvailableException(final Throwable arg0) {
		super(arg0);
	}

}
