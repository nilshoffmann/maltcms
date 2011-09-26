/*
 * 
 *
 * $Id$
 */

package cross.exception;

/**
 * ConstraintViolationException is thrown, if constraints are not met, as
 * checked within {@link cross.tools.EvalTools}.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class ConstraintViolationException extends RuntimeException {

	private static final long serialVersionUID = 6583620499607920323L;

	public ConstraintViolationException(final String arg0) {
		super(arg0);
	}

	public ConstraintViolationException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

	public ConstraintViolationException(final Throwable arg0) {
		super(arg0);
	}

}
