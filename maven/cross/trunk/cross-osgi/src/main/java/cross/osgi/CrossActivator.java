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
package cross.osgi;

import cross.Factory;
import java.util.Dictionary;
import java.util.Properties;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.PropertyConfigurator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Nils Hoffmann
 */
public class CrossActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        setupLogging();
        Dictionary props = new Properties();
        Factory factory = Factory.getInstance();
        PropertiesConfiguration cfg = new PropertiesConfiguration();
        factory.configure(cfg);
        context.registerService(Factory.class.getName(), factory, props);
    }

    protected void setupLogging() {
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "INFO, A1");
        props.setProperty("log4j.appender.A1",
                "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.A1.layout.ConversionPattern",
                "%m%n");
        props.setProperty("log4j.category.cross", "WARN");
        props.setProperty("log4j.category.cross.datastructures.pipeline",
                "INFO");
        props.setProperty("log4j.category.maltcms.commands.fragments",
                "INFO");
        props.setProperty("log4j.category.maltcms.commands.fragments2d",
                "INFO");
        props.setProperty("log4j.category.maltcms", "WARN");
        props.setProperty("log4j.category.ucar", "WARN");
        props.setProperty("log4j.category.smueller", "WARN");
        props.setProperty("log4j.category.org.springframework.beans.factory", "WARN");
        PropertyConfigurator.configure(props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
