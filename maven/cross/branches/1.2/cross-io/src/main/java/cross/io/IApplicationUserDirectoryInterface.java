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

package cross.io;

import java.io.File;

/**
 *
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
public interface IApplicationUserDirectoryInterface {

    /**
     * Returns the user data directory for this application.
     * The path returned will usually be below ${user.home}/,
     * which should be the basedir of the currently active user in
     * both Windows and Unix/Linux/Mac OSX operating systems.
     * On Windows, the application data directory will be located within that folder.
     * On the other (UNIX) platforms, the application data directory will be located
     * in ${user.home}/.<APPLICATION_NAME>/
     * @return the application's user data directory.
     */
    File getApplicationUserDirectory();

    /**
     * Returns a directory specific to a service interface definition and its implementation.
     *
     * @param serviceInterface the class defining the service interface.
     * @param serviceName the class defining the service implementation.
     * @throws IllegalArgumentException if <code>serviceName</code> does not implement <code>serviceInterface</code>
     * @return the File object representing the subdirectory for the specific service implementation.
     */
    File getDirectoryForService(Class<?> serviceInterface,
            Class<?> serviceName) throws IllegalArgumentException;

    /**
     * Returns the directory named <emph>services</emph> below this UserDir's basedir.
     * @return the services directory.
     */
    File getServicesDirectory();

    /**
     * Returns the direct subdirectory of name <code>name</code> below this
     * UserDir's basedir. Creates the directory if it does not exist yet.
     * @param name the name of the subdirectory to return.
     * @return the subdirectory of name <code>name</code>.
     */
    File getSubdirOfAppUserDir(String name);

    /**
     * Returns a direct subdirectory of name <code>name</code> below <code>basedir</code>.
     * Creates the directory if it does not exist yet.
     * @param basedir
     * @param name
     * @return the subdirectory of name <code>name</code> below the given basedir.
     */
    File getSubdirectory(File basedir, String name);
    
}
