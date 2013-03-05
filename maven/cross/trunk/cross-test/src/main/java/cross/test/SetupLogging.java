/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
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
package cross.test;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.rules.TestWatcher;

/**
 *
 * @author Nils Hoffmann
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

    public void setLogLevel(Class clazz, String level) {
        getConfig().put("log4j.category." + clazz.getName(), level);
        update();
    }

    public void setLogLevel(String packageOrClass, String level) {
        if (packageOrClass.startsWith("log4j.category.")) {
            getConfig().put(packageOrClass, level);
        } else {
            getConfig().put("log4j.category." + packageOrClass, level);
        }
        update();
    }

    public void update() {
        PropertyConfigurator.configure(config);
    }
}
