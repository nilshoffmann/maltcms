/*
 * $license$
 *
 * $Id$
 */

package cross.io.cli;

import java.security.Permission;

/**
 *
 * @author nilshoffmann
 */
public class CustomSecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {
        // allow anything.
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        // allow anything.
    }

    @Override
    public void checkExit(int status) {
        super.checkExit(status);
        throw new ExitException(status);
    }

    public class ExitException extends SecurityException {

        private static final long serialVersionUID = -1982617086752946683L;
        public final int status;

        public ExitException(int status) {
            super("Tried to exit vm with status "+status);
            this.status = status;
        }
    }
}
