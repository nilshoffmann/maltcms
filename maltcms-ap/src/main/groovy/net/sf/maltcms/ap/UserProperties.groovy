/*
 * Maltcms, modular application toolkit for chromatography mass-spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
 * As a user/recipient of Maltcms, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package net.sf.maltcms.ap

import groovy.transform.Canonical
import groovy.beans.Bindable

/**
 *
 * @author Nils Hoffmann
 */
@Canonical
@Bindable
class UserProperties {
    File lastDirectory = new File(System.getProperty("user.home"))

    public void load() {
        def props = new Properties()
        File f = new File(new File(System.getProperty("user.home"),".maltcms-ap"),"user.properties")
        if(f.exists()) {
            f.withInputStream {
                stream -> props.load(stream)
            }
            lastDirectory = props["lastDirectory"] as File
        }else{
            println "${f} not found, using defaults!"
            lastDirectory = new File(System.getProperty("user.home"))
        }
    }

    public void save() {
        File dir = new File(System.getProperty("user.home"),".maltcms-ap")
        dir.mkdirs()
        File f = new File(dir,"user.properties")
        def props = new Properties()
        props["lastDirectory"] = lastDirectory.absolutePath
        f.withOutputStream {
            stream -> props.store(stream, "maltcms-ap user settings")
        }
    }

}

