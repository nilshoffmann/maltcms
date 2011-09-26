/*
 * $license$
 *
 * $Id$
 */

package cross.io;

import cross.IApplicationInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Obtain an instance of this class by calling 
 * Lookup.getDefault().lookup(IApplicationUserDirectoryProvider.class)
 * 
 * The ApplicationUserDirectory instance is created and returned on first access to 
 * getApplicationUserDirectory and simply returned on successive calls.
 * 
 * Note: This requires an IApplicationInfo implementation to be provided by 
 * a derived application!
 * 
 * @author nilshoffmann
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
            IApplicationInfo applicationInfo = Lookup.getDefault().lookup(
                    IApplicationInfo.class);
            applicationUserDirectory = new ApplicationUserDirectory(applicationInfo.
                    getName(), applicationInfo.getVersion());
        }
        return applicationUserDirectory;
    }
}
