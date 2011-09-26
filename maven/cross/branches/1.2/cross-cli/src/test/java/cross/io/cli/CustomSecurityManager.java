/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * 
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
