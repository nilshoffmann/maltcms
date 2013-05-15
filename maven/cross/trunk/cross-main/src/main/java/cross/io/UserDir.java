/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.io;

import java.io.File;

/**
 * Abstraction of a user dir for an application given an application name and 
 * a version.
 * 
 * @author Nils Hoffmann
 */
public class UserDir {

    private final String appname;
    private final String version;

    /**
     * Creates a new UserDir object, which can be branded for a specific
     * application name and version. Use this object, if you want to retrieve a
     * common location for application configuration data storage.
     *
     * <p>The parameter
     * <code>appname</code> should be rather short and should not contain any
     * characters, which are incompatible with the underlying filesystem
     * implementation. Usually, all alphanumeric symbols plus
     * <code>_- </code> should be fine.</p>
     *
     * @param appname a short name describing the application.
     * @param version a version string for the application.
     */
    public UserDir(final String appname, final String version) {
        this.appname = appname;
        this.version = version;
    }

    /**
     * Returns the user data directory for this application. The path returned
     * will usually be below ${user.home}/, which should be the basedir of the
     * currently active user in both Windows and Unix/Linux/Mac OSX operating
     * systems. On Windows, the application data directory will be located
     * within that folder. On the other (UNIX) platforms, the application data
     * directory will be located in ${user.home}/.<APPLICATION_NAME>/
     *
     * @return the application's user data directory.
     */
    public File getApplicationUserDirectory() {
        File userHome = new File(System.getProperty("user.home"));
        String os = System.getProperty("os.name").toLowerCase();
        String prefix = "";
        if (os.indexOf("wind") != -1) {
            prefix = "";
        } else if (os.indexOf("nix") != -1 || os.indexOf("mac") != -1 || os.indexOf("sol") != -1 || os.indexOf("sun") != -1 || os.indexOf("ux") != -1 || os.indexOf("ix") != -1 || os.indexOf("bsd") != -1) {
            prefix = ".";
        }
        File appDir = new File(userHome, prefix + this.appname);
        File versionDir = new File(appDir, this.version);
        if (!versionDir.exists()) {
            versionDir.mkdirs();
        }
        return versionDir;
    }

    /**
     * Returns the direct subdirectory of name
     * <code>name</code> below this UserDir's basedir. Creates the directory if
     * it does not exist yet.
     *
     * @param name the name of the subdirectory to return.
     * @return the subdirectory of name <code>name</code>.
     */
    public File getSubdirOfAppUserDir(String name) {
        return getSubdirectory(getApplicationUserDirectory(), name);
    }

    /**
     * Returns a direct subdirectory of name
     * <code>name</code> below
     * <code>basedir</code>. Creates the directory if it does not exist yet.
     *
     * @param basedir
     * @param name
     * @return the subdirectory of name <code>name</code> below the given
     * basedir.
     */
    public File getSubdirectory(File basedir, String name) {
        File sd = new File(basedir, name);
        if (!sd.exists()) {
            sd.mkdirs();
        }
        return sd;
    }

    /**
     * Returns the directory named <emph>services</emph> below this UserDir's
     * basedir.
     *
     * @return the services directory.
     */
    public File getServicesDirectory() {
        return getSubdirOfAppUserDir("services");
    }

    /**
     * Returns a directory specific to a service interface definition and its
     * implementation.
     *
     * @param serviceInterface the class defining the service interface.
     * @param serviceName the class defining the service implementation.
     * @throws IllegalArgumentException if <code>serviceName</code> does not
     * implement <code>serviceInterface</code>
     * @return the File object representing the subdirectory for the specific
     * service implementation.
     */
    public File getDirectoryForService(Class<?> serviceInterface, Class<?> serviceName) throws IllegalArgumentException {
        if (serviceInterface.isAssignableFrom(serviceName)) {
            return getSubdirectory(getSubdirectory(getServicesDirectory(), serviceInterface.getCanonicalName()), serviceName.getCanonicalName());
        }
        throw new IllegalArgumentException("Class " + serviceName.getCanonicalName() + " does not implement " + serviceInterface.getCanonicalName());
    }
}
