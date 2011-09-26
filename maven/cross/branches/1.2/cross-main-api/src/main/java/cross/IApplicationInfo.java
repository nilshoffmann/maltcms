/*
 * 
 *
 * $Id$
 */

package cross;

import java.io.File;

/**
 *
 * @author nilshoffmann
 */
public interface IApplicationInfo {
    public String getVersion();
    
    public String getName();
    
    public String getLicense();
    
    public File getApplicationUserDirectory();
}
