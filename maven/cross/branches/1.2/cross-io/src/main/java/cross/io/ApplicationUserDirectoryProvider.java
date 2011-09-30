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

import cross.lookup.GlobalContext;
import org.openide.util.lookup.ServiceProvider;

/**
 * Obtain an instance of this class by calling 
 * GlobalContext.getContext().getBean(IApplicationUserDirectoryProvider.class)
 * 
 * The ApplicationUserDirectory instance is created and returned on first access to 
 * getApplicationUserDirectory and simply returned on successive calls.
 * 
 * Note: This requires an IApplicationInfo implementation to be provided by 
 * a derived application!
 * 
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 */
@ServiceProvider(service = IApplicationUserDirectoryProvider.class)
public class ApplicationUserDirectoryProvider implements
        IApplicationUserDirectoryProvider {

    private ApplicationUserDirectory applicationUserDirectory = null;

    /**
     * May throw a NullPointerException if no implementation of IApplicationInfo
     * is present on the classpath.
     * @return 
     */
    @Override
    public IApplicationUserDirectoryInterface getApplicationUserDirectory() {
        if (applicationUserDirectory == null) {
            IApplicationInfo applicationInfo = GlobalContext.getContext().getBean(
                    IApplicationInfo.class);
            applicationUserDirectory = new ApplicationUserDirectory(applicationInfo.
                    getName(), applicationInfo.getVersion());
        }
        return applicationUserDirectory;
    }
}
