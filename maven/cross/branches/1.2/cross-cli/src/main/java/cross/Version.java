/*
 * 
 *
 * $Id$
 */

package cross;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Nils Hoffmann
 */
public class Version {
    
    public String getVersion() throws IOException {
        Properties props = new Properties();
        props.load(Version.class.getResourceAsStream("/cross/version.properties"));
        return props.getProperty("application.version");
    }
    
    public String getAppName() throws IOException {
        Properties props = new Properties();
        props.load(Version.class.getResourceAsStream("/cross/version.properties"));
        return props.getProperty("application.name");
    }
}
