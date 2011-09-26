/*
 * 
 *
 * $Id$
 */

package cross.exception;

public class NotInitializedException extends RuntimeException {

	/**
     * 
     */
	private static final long serialVersionUID = -277507898414704248L;

	public NotInitializedException(final String arg0) {
		super(arg0);
	}

	public NotInitializedException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public NotInitializedException(final Throwable arg0) {
		super(arg0);
	}

}
