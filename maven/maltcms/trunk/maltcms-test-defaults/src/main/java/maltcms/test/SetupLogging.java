/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.test;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 *
 * @author nils
 */
public class SetupLogging extends TestWatcher {

    private final Properties config;

    public SetupLogging() {
        this(SetupLogging.class.getResource("/log4j.properties"));
    }
    
    public SetupLogging(Properties props) {
        this.config = props;
        PropertyConfigurator.configure(config);
    }

    public SetupLogging(URL props) {
        Properties config = new Properties();
        try {
            config.load(props.openStream());
        } catch (IOException ex) {
            Logger.getLogger(SetupLogging.class.getName()).log(Level.SEVERE, null, ex);
            config.setProperty("log4j.rootLogger", "INFO, A1");
            config.setProperty("log4j.appender.A1",
                    "org.apache.log4j.ConsoleAppender");
            config.setProperty("log4j.appender.A1.layout",
                    "org.apache.log4j.PatternLayout");
            config.setProperty("log4j.appender.A1.layout.ConversionPattern",
                    "%-4r [%t] %-5p %c %x - %m%n");
        }
        this.config = config;
        PropertyConfigurator.configure(config);
    }

    public Properties getConfig() {
        return config;
    }
    
}
